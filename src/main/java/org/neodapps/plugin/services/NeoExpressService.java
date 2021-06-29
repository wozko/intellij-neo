/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
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
  private final MessageBus bus;

  /**
   * Runs express commands.
   *
   * @param project Working project
   */
  public NeoExpressService(Project project) {
    neoProject = project;
    bus = neoProject.getMessageBus();
  }

  /**
   * Creates a private net configuration.
   *
   * @param numberOfNodes  Number of consensus nodes to create, Allowed values are: 1, 4, 7
   * @param addressVersion Version to use for addresses in this blockchain instance
   * @param name           Name of the configuration
   */
  public void createPrivateNet(int numberOfNodes, long addressVersion, String name) {
    var id = UUID.randomUUID();
    if (numberOfNodes != 1 && numberOfNodes != 4 && numberOfNodes != 7) {
      numberOfNodes = 1;
    }

    runCommand(ExpressCommand.CREATE, Arrays.asList("-c", String.valueOf(numberOfNodes),
        "-a", String.valueOf(addressVersion), "-f", name),
        NeoMessageBundle.message("terminal.tab.name", ExpressCommand.CREATE, ""),
        id,
        () -> {
          var publisher = bus.syncPublisher(PrivateChainCreatorNotifier.NEW_PRIVATE_NET_CREATED);
          publisher.privateNetCreated(name);

          NeoNotifier.notifySuccess(neoProject,
              NeoMessageBundle.message("notifications.private.net.created", name));
        }
    );
  }

  /**
   * Runs a private net.
   *
   * @param chain chain to run.
   */
  public void runPrivateNet(PrivateChain chain) {
    var id = UUID.randomUUID();
    // -s, -d options ---> more future features
    runCommand(ExpressCommand.RUN,
        Arrays.asList("-i", chain.toString(), String.valueOf(chain.getSelectedIndex())),
        NeoMessageBundle
            .message("terminal.tab.name", ExpressCommand.RUN.toString(),
                String.format("%s (node %d)", chain, chain.getSelectedIndex())), id,
        () -> {
          // private net process has stopped
          // handle this
        });
  }

  /**
   * Create a wallet.
   *
   * @param chain chain to run.
   */
  public void createWallet(String name, PrivateChain chain) {
    var id = UUID.randomUUID();
    runCommand(ExpressCommand.WALLET,
        Arrays.asList("create", "-i", chain.toString(), name),
        NeoMessageBundle
            .message("terminal.tab.name", ExpressCommand.WALLET.toString(),
                String.format("%s (%s)", "Create", name)), id,
        () -> {
          // wallet created
          NeoNotifier.notifySuccess(neoProject,
              NeoMessageBundle.message("notifications.wallet.created", name));
          // todo: refresh wallet list
        });
  }

  /**
   * Runs a neo-express command.
   *
   * @param expressCommand command to run
   * @param options        command options
   */
  public void runCommand(ExpressCommand expressCommand, List<String> options, String tabName,
                         UUID id, CompletionAction completionAction) {
    // get the terminal window
    var shell = new ShellTerminalRunner(neoProject);
    if (!shell.isAvailable(neoProject)) {
      NeoNotifier
          .notifyError(neoProject, NeoMessageBundle.message("terminal.window.not.available"));
      return;
    }

    // run command from base path
    var command = createCommand(expressCommand, options);
    final ShellTerminalWidget widget;
    try {
      widget = shell.run(command, Objects.requireNonNull(neoProject.getBasePath()), tabName);
      // wait for command to run
      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
      var future = executor.scheduleAtFixedRate(() -> {
        if (!widget.hasRunningCommands()) {
          // publish an event when command is no longer running
          var publisher = bus.syncPublisher(ExpressCommandNotifier.RUNNER);
          publisher.afterCompletion(id);
        }
      }, 0, 500, TimeUnit.MILLISECONDS);

      // cancel the scheduler and publish command completion event
      neoProject.getMessageBus().connect().subscribe(ExpressCommandNotifier.RUNNER,
          (completedProcess) -> {
            if (completedProcess.equals(id)) {
              future.cancel(false);
              completionAction.perform();
            }
          });

    } catch (IOException e) {
      NeoNotifier.notifyError(neoProject, e.getMessage());
    }
  }

  private String createCommand(ExpressCommand command, List<String> options) {
    List<String> params = new ArrayList<>();
    params.add("neoxp");
    params.add(command.toString());
    params.addAll(options);
    return String.join(" ", params);
  }

  interface CompletionAction {
    void perform();
  }
}
