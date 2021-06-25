/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the content shown in tool window.
 */
public class ToolWindowComponent implements Disposable {

  private final Project project;
  private SimpleToolWindowPanel panel;

  public ToolWindowComponent(@NotNull Project project) {
    this.project = project;
  }

  /**
   * Returns the content required for tool window.
   *
   * @return the content that will be used by {@link NeoToolWindowFactory}
   */
  public SimpleToolWindowPanel getContent() {
    if (panel == null) {
      panel = new SimpleToolWindowPanel(true, false);
    }
    return panel;
  }

  @Override
  public void dispose() {
    panel = null;
  }
}
