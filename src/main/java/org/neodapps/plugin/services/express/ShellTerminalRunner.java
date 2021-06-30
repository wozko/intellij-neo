/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.express;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory;
import org.jetbrains.plugins.terminal.TerminalView;
import org.jetbrains.plugins.terminal.arrangement.TerminalWorkingDirectoryManager;

/**
 * Runs command in terminal window.
 */
public class ShellTerminalRunner {

  private final Project myProject;

  protected ShellTerminalRunner(@NotNull Project project) {
    this.myProject = project;
  }

  @Nullable
  private static Pair<Content, ShellTerminalWidget> getSuitableProcess(
      @NotNull ContentManager contentManager, @NotNull String workingDirectory) {
    Content selectedContent = contentManager.getSelectedContent();
    if (selectedContent != null) {
      Pair<Content, ShellTerminalWidget> pair =
          getSuitableProcess(selectedContent, workingDirectory);
      if (pair != null) {
        return pair;
      }
    }

    return Arrays.stream(contentManager.getContents())
        .map(content -> getSuitableProcess(content, workingDirectory))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  @Nullable
  private static Pair<Content, ShellTerminalWidget> getSuitableProcess(
      @NotNull Content content, @NotNull String workingDirectory) {
    JBTerminalWidget widget = TerminalView.getWidgetByContent(content);
    if (!(widget instanceof ShellTerminalWidget)) {
      return null;
    }
    ShellTerminalWidget shellTerminalWidget = (ShellTerminalWidget) widget;
    if (!shellTerminalWidget.getTypedShellCommand().isEmpty()
        || shellTerminalWidget.hasRunningCommands()) {
      return null;
    }
    String currentWorkingDirectory =
        TerminalWorkingDirectoryManager.getWorkingDirectory(shellTerminalWidget, null);
    if (currentWorkingDirectory == null || !currentWorkingDirectory.equals(workingDirectory)) {
      return null;
    }
    return Pair.create(content, shellTerminalWidget);
  }

  /**
   * Runs a command in terminal.
   *
   * @param command          command to run
   * @param workingDirectory directory to run from
   * @param tabName          name of the terminal tab
   */
  public ShellTerminalWidget run(@NotNull String command, @NotNull String workingDirectory,
                                 @NotNull String tabName) throws IOException {
    TerminalView terminalView = TerminalView.getInstance(myProject);
    ToolWindow window = ToolWindowManager.getInstance(myProject)
        .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
    if (window == null) {
      return null;
    }

    ContentManager contentManager = window.getContentManager();
    Pair<Content, ShellTerminalWidget> pair = getSuitableProcess(contentManager, workingDirectory);

    if (pair == null) {
      var terminalWidget = terminalView
          .createLocalShellWidget(workingDirectory, tabName, true);
      terminalWidget.executeCommand(command);
      return terminalWidget;
    }
    window.activate(null);
    contentManager.setSelectedContent(pair.first);
    var terminalWidget = pair.second;
    terminalWidget.executeCommand(command);
    return terminalWidget;
  }

  /**
   * Check if terminal window is available.
   *
   * @param project neo project
   * @return returns true if terminal window is available
   */
  public boolean isAvailable(@NotNull Project project) {
    ToolWindow window = ToolWindowManager.getInstance(project)
        .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
    return window != null && window.isAvailable();
  }
}