/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services;

import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.blockchain.express.ExpressCommand;
import org.neodapps.plugin.topics.ExpressCommandNotifier;
import org.neodapps.plugin.topics.PrivateChainCreatorNotifier;

/**
 * Neo express task runner.
 */
public final class NeoExpressService {

  private final Project neoProject;

  /**
   * Runs express commands.
   *
   * @param project Working project
   */
  public NeoExpressService(Project project) {
    neoProject = project;
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
        NeoMessageBundle.message("terminal.tab.name", ExpressCommand.CREATE, "")
    );

    var bus = neoProject.getMessageBus();
    // subscribe to command
    bus.connect().subscribe(ExpressCommandNotifier.CREATE_COMMAND, (command) -> {

      // send an event to refresh the ui
      var publisher = bus.syncPublisher(PrivateChainCreatorNotifier.NEW_PRIVATE_NET_CREATED);
      publisher.privateNetCreated(name);

      NeoNotifier.notifySuccess(neoProject,
          NeoMessageBundle.message("notifications.private.net.created", name));

    });
  }

  /**
   * Runs a private net.
   *
   * @param chain chain to run.
   */
  public void runPrivateNet(PrivateChain chain) {
    // -s, -d options ---> more future features
    runCommand(ExpressCommand.RUN,
        Arrays.asList("-i", chain.toString(), String.valueOf(chain.getSelectedIndex())),
        NeoMessageBundle
            .message("terminal.tab.name", ExpressCommand.RUN.toString(),
                String.format("%s (node %d)", chain, chain.getSelectedIndex())));
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
      NeoNotifier
          .notifyError(neoProject, NeoMessageBundle.message("terminal.window.not.available"));
      return;
    }

    // run command from base path
    var command = createCommand(expressCommand, options);
    try {
      shell.run(command, Objects.requireNonNull(neoProject.getBasePath()), tabName);
    } catch (IOException e) {
      NeoNotifier.notifyError(neoProject, e.getMessage());
    }

    var bus = neoProject.getMessageBus();
    // wait for command to run
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    var future = executor.scheduleAtFixedRate(() -> {
      if (!shell.getTerminalWidget().hasRunningCommands()) {
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
    params.add("neoxp");
    params.add(command.toString());
    params.addAll(options);
    return String.join(" ", params);
  }
}
