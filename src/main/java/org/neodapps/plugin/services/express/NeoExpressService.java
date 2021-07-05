/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.express;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
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
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.blockchain.express.ExpressCommand;
import org.neodapps.plugin.persistance.SettingsState;
import org.neodapps.plugin.topics.ExpressCommandNotifier;
import org.neodapps.plugin.topics.PrivateChainCreatorNotifier;

/**
 * Neo express task runner.
 */
public final class NeoExpressService {

  private final Project neoProject;
  private final MessageBus bus;
  private static final long ADDRESS_VERSION = 53;

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
   * @param numberOfNodes Number of consensus nodes to create, Allowed values are: 1, 4, 7
   * @param name          Name of the configuration
   */
  public void createPrivateNet(int numberOfNodes, String name) {
    var id = UUID.randomUUID();
    if (numberOfNodes != 1 && numberOfNodes != 4 && numberOfNodes != 7) {
      numberOfNodes = 1;
    }

    runCommandAsync(ExpressCommand.CREATE,
        Arrays.asList("-c", String.valueOf(numberOfNodes),
            "-a", String.valueOf(ADDRESS_VERSION), "-f", name),
        id,
        (output) -> {
          if (StringUtils.isNotEmpty(output.getStderr())) {
            // an error has occurred
            NeoNotifier.notifyError(neoProject, NeoMessageBundle
                .message("notifications.private.net.creation.failed", output.getStderr()));
          } else {
            NeoNotifier.notifySuccess(neoProject,
                NeoMessageBundle.message("notifications.private.net.created", name));
            var publisher = bus.syncPublisher(PrivateChainCreatorNotifier.NEW_PRIVATE_NET_CREATED);
            publisher.privateNetCreated(name);
          }
        }
    );
  }

  /**
   * Runs a private net.
   *
   * @param chain chain to run.
   */
  public void runPrivateNet(PrivateChain chain, int nodeIndex) {
    var id = UUID.randomUUID();
    // -s, -d options ---> more future features
    runCommandInTerminalWindow(ExpressCommand.RUN,
        Arrays.asList("-i", chain.toString(), String.valueOf(nodeIndex)),
        NeoMessageBundle
            .message("terminal.tab.name", ExpressCommand.RUN.toString(),
                String.format("%s (node %d)", chain, chain.getSelectedIndex())), id,
        () -> {
          // running private net process has stopped
          // not required to handle this
        });
  }

  /**
   * Create a wallet.
   *
   * @param chain chain to run.
   */
  public void createWallet(String name, PrivateChain chain) {
    var id = UUID.randomUUID();
    runCommandAsync(ExpressCommand.WALLET,
        Arrays.asList("create", "-i", chain.toString(), name), id,
        (output) -> {
          if (StringUtils.isNotEmpty(output.getStderr())) {
            // an error has occurred
            NeoNotifier.notifyError(neoProject, NeoMessageBundle
                .message("notifications.wallet.creation.failed", output.getStderr()));
          } else {
            NeoNotifier.notifySuccess(neoProject,
                NeoMessageBundle.message("notifications.wallet.created", name));
          }
        });
  }

  /**
   * Transfer an asset.
   *
   * @param amount quantity of asset
   * @param from   from account
   * @param to     to account
   * @param chain  selected chain
   */
  public void transferAsset(String amount, String asset, String from, String to, ChainLike chain) {
    var id = UUID.randomUUID();
    runCommandAsync(ExpressCommand.TRANSFER,
        Arrays.asList(amount, asset, from, to, "-i", chain.toString()), id,
        (output) -> {
          if (StringUtils.isNotEmpty(output.getStderr())) {
            // an error has occurred
            NeoNotifier.notifyError(neoProject, NeoMessageBundle
                .message("notifications.wallet.transfer.failed", output.getStderr()));
          } else {
            NeoNotifier.notifySuccess(neoProject,
                NeoMessageBundle.message("notifications.wallet.transfer.done"));
          }
        });
  }

  /**
   * Runs a neo-express command in a terminal window.
   *
   * @param expressCommand command to run
   * @param options        command options
   */
  public void runCommandInTerminalWindow(ExpressCommand expressCommand, List<String> options,
                                         String tabName,
                                         UUID id, ShellTerminalCompletionAction completionAction) {
    var execPath = getExecPath();
    if (execPath == null) {
      // notification sent
      // exiting
      return;
    }
    // get the terminal window
    var shell = new ShellTerminalRunner(neoProject);
    if (!shell.isAvailable(neoProject)) {
      NeoNotifier
          .notifyError(neoProject, NeoMessageBundle.message("terminal.window.not.available"));
      return;
    }

    // create command
    List<String> params = new ArrayList<>();
    params.add(execPath);
    params.add(expressCommand.toString());
    params.addAll(options);
    var command = String.join(" ", params);

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
              future.cancel(true);
              completionAction.perform();
            }
          });
    } catch (IOException e) {
      NeoNotifier.notifyError(neoProject, e.getMessage());
    }
  }

  /**
   * Runs express command asynchronously and perform action
   * on complete.
   *
   * @param expressCommand   command to run
   * @param options          command options
   * @param id               an identifier, typically a uuid
   * @param completionAction an action to do on completion
   */
  private void runCommandAsync(ExpressCommand expressCommand, List<String> options,
                               UUID id, CommandLineCompleteAction completionAction) {
    var expressPath = getExecPath();
    var dotNetPath = getDotNetPath();

    if (expressPath == null || dotNetPath == null) {
      // have sent notifications
      // exiting
      return;
    }

    var commandLine = new GeneralCommandLine().withEnvironment("DOTNET_ROOT", dotNetPath);
    commandLine.setExePath(expressPath);
    commandLine.addParameter(expressCommand.toString());
    commandLine.addParameters(options);
    commandLine.setWorkDirectory(neoProject.getBasePath());
    AtomicReference<ProcessOutput> processOutput = new AtomicReference<>();

    try {
      processOutput.set(new CapturingProcessHandler(commandLine).runProcess());
      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
      var future = executor.scheduleAtFixedRate(() -> {
        if (processOutput.get().isExitCodeSet()) {
          // publish an event when command is no longer running
          var publisher = bus.syncPublisher(ExpressCommandNotifier.RUNNER);
          publisher.afterCompletion(id);
        }
      }, 0, 500, TimeUnit.MILLISECONDS);

      // cancel the scheduler and publish command completion event
      neoProject.getMessageBus().connect().subscribe(ExpressCommandNotifier.RUNNER,
          (completedProcess) -> {
            if (completedProcess.equals(id)) {
              future.cancel(true);
              completionAction.perform(processOutput.get());
            }
          });
    } catch (ExecutionException e) {
      NeoNotifier.notifyError(neoProject, e.getMessage());
    }

  }

  /**
   * Return neo-express path from settings.
   *
   * @return neo-express path
   */
  private String getExecPath() {
    var path = SettingsState.getInstance().neoExpressLocation;
    if (StringUtils.isEmpty(path)) {
      NeoNotifier.notifyError(neoProject,
          NeoMessageBundle.message("notifications.settings.neo.path.not.set"));
      return null;
    }
    return path;
  }

  private String getDotNetPath() {
    var path = SettingsState.getInstance().dotNetRoot;
    if (StringUtils.isEmpty(path)) {
      NeoNotifier.notifyError(neoProject,
          NeoMessageBundle.message("notifications.settings.net.path.not.set"));
      return null;
    }
    return path;
  }

  interface ShellTerminalCompletionAction {
    void perform();
  }

  interface CommandLineCompleteAction {
    void perform(ProcessOutput output);
  }
}
