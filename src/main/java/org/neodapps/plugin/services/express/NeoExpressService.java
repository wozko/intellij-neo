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
import org.apache.commons.lang.StringUtils;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
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

    runCommandAsync(ExpressCommand.CREATE,
        Arrays.asList("-c", String.valueOf(numberOfNodes),
            "-a", String.valueOf(addressVersion), "-f", name),
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
  public void runPrivateNet(PrivateChain chain) {
    var id = UUID.randomUUID();
    // -s, -d options ---> more future features
    runCommandInTerminalWindow(ExpressCommand.RUN,
        Arrays.asList("-i", chain.toString(), String.valueOf(chain.getSelectedIndex())),
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
            var publisher = bus.syncPublisher(PrivateChainCreatorNotifier.NEW_PRIVATE_NET_CREATED);
            publisher.privateNetCreated(name);
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

    var commandLine = new GeneralCommandLine();
    commandLine.setExePath(expressPath);
    commandLine.addParameter(expressCommand.toString());
    commandLine.addParameters(options);
    commandLine.setWorkDirectory(neoProject.getBasePath());
    commandLine = commandLine.withEnvironment("DOTNET_ROOT", dotNetPath);
    try {
      final var processOutput = new CapturingProcessHandler(commandLine).runProcess();

      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
      var future = executor.scheduleAtFixedRate(() -> {
        if (processOutput.isExitCodeSet()) {
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
              completionAction.perform(processOutput);
            }
          });
    } catch (ExecutionException e) {
      NeoNotifier.notifyError(neoProject, e.getMessage());
    }
  }

  /**
   * Runs express command, wait for exit code and return output.
   * This is used only when output is needed to be parsed (ex: show balance).
   *
   * @param expressCommand command to run
   * @param options        command options
   * @return process result after completion
   */
  @SuppressWarnings("unused") // implemented for completion, not used
  private ProcessOutput runCommandSync(ExpressCommand expressCommand, List<String> options) {
    var expressPath = getExecPath();
    var dotNetPath = getDotNetPath();

    if (expressPath == null || dotNetPath == null) {
      // have sent notifications
      // exiting
      return null;
    }

    var commandLine = new GeneralCommandLine();
    commandLine.setExePath(expressPath);
    commandLine.addParameter(expressCommand.toString());
    commandLine.addParameters(options);
    commandLine.setWorkDirectory(neoProject.getBasePath());
    commandLine = commandLine.withEnvironment("DOTNET_ROOT", dotNetPath);

    try {
      final var processOutput = new CapturingProcessHandler(commandLine).runProcess();
      while (processOutput.isExitCodeSet() || processOutput.isCancelled()) {
        TimeUnit.MILLISECONDS.sleep(100);
      }
      return processOutput;
    } catch (ExecutionException | InterruptedException e) {
      NeoNotifier.notifyError(neoProject, e.getMessage());
      return null;
    }
  }

  /**
   * Return neo-express path from settings.
   *
   * @return neo-express path
   */
  private String getExecPath() {
    var path = neoProject.getService(SettingsState.class).neoExpressLocation;
    if (StringUtils.isEmpty(path)) {
      NeoNotifier.notifyError(neoProject, "notifications.settings.neo.path.not.set");
      return null;
    }
    return path;
  }

  private String getDotNetPath() {
    var path = neoProject.getService(SettingsState.class).dotNetRoot;
    if (StringUtils.isEmpty(path)) {
      NeoNotifier.notifyError(neoProject, "notifications.settings.net.path.not.set");
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
