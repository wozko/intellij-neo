/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.neoexpress.service;

import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.neodapps.plugin.MessageBundle;
import org.neodapps.plugin.Notifier;
import org.neodapps.plugin.neoexpress.model.ExpressCommand;
import org.neodapps.plugin.neoexpress.model.ExpressCommandNotifier;
import org.neodapps.plugin.settings.SettingsState;
import org.neodapps.plugin.toolwindow.topics.NodeListNotifier;

/**
 * Neo express task runner.
 */
public final class NeoExpressService {

  private final Project neoProject;
  private final String neoExecutable;

  /**
   * Runs express commands.
   *
   * @param project Working project
   */
  public NeoExpressService(Project project) {
    neoProject = project;

    // set neo-express executable path
    var settings = SettingsState.getInstance();
    var specifiedPath = settings.neoExpressLocation;
    if (specifiedPath.isEmpty()) {
      this.neoExecutable = "neoxp";
    } else {
      this.neoExecutable = specifiedPath;
    }
  }

  /**
   * Creates a private net configuration.
   *
   * @param numberOfNodes  Number of consensus nodes to create, Allowed values are: 1, 4, 7
   * @param addressVersion Version to use for addresses in this blockchain instance
   * @param name           Name of the configuration
   */
  public void createPrivateNet(int numberOfNodes, long addressVersion, String name) {
    if (numberOfNodes != 1 && numberOfNodes != 4 && numberOfNodes != 7) {
      numberOfNodes = 1;
    }

    runCommand(ExpressCommand.CREATE, Arrays.asList("-c", String.valueOf(numberOfNodes),
        "-a", String.valueOf(addressVersion), "-f", name),
        MessageBundle.message("terminal.tab.name", ExpressCommand.CREATE, "")
    );

    var bus = neoProject.getMessageBus();
    // subscribe to command
    bus.connect().subscribe(ExpressCommandNotifier.CREATE_COMMAND, (command) -> {

      // wait a second for newly created .neo-express file to index
      // without this the refresh is not including the newly created file
      // TODO: look for a better approach
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      // send an event to refresh the ui
      var publisher = bus.syncPublisher(NodeListNotifier.CHAIN_ADDED);
      publisher.afterAction();

      Notifier.notifySuccess(neoProject,
          MessageBundle.message("notifications.private.net.created", name));

    });
  }

  /**
   * Runs a neo-express command.
   *
   * @param expressCommand command to run
   * @param options        command options
   */
  public void runCommand(ExpressCommand expressCommand, List<String> options, String tabName) {
    // get the terminal window
    var shell = new ShellTerminalRunner(neoProject);
    if (!shell.isAvailable(neoProject)) {
      Notifier.notifyError(neoProject, MessageBundle.message("terminal.window.not.available"));
      return;
    }

    // run command from base path
    var command = createCommand(expressCommand, options);
    try {
      shell.run(command, Objects.requireNonNull(neoProject.getBasePath()), tabName);
    } catch (IOException e) {
      Notifier.notifyError(neoProject, e.getMessage());
    }

    var bus = neoProject.getMessageBus();
    // wait for command to run
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    var future = executor.scheduleAtFixedRate(() -> {
      if (!shell.isProcessRunning()) {
        // publish an event when command is no longer running
        var publisher = bus.syncPublisher(ExpressCommandNotifier.ANY_COMMAND);
        publisher.afterCompletion(expressCommand);
      }
    }, 0, 500, TimeUnit.MILLISECONDS);

    // cancel the scheduler and publish command completion event
    neoProject.getMessageBus().connect().subscribe(ExpressCommandNotifier.ANY_COMMAND,
        (completedExpressCommand) -> {
          future.cancel(false);

          var publisher = bus.syncPublisher(completedExpressCommand.getTopic());
          publisher.afterCompletion(completedExpressCommand);
        });
  }

  private String createCommand(ExpressCommand command, List<String> options) {
    List<String> params = new ArrayList<>();
    params.add(neoExecutable);
    params.add(command.toString());
    params.addAll(options);
    return String.join(" ", params);
  }
}
