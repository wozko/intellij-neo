/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.neodapps.plugin.ui.details.DetailsComponent;
import org.neodapps.plugin.ui.toolbar.NodePickerComponent;
import org.neodapps.plugin.ui.toolbar.SelectedNodeStateComponent;
import org.neodapps.plugin.ui.toolbar.ToolBarComponent;
import org.neodapps.plugin.ui.toolbar.action.NodeActionComponent;

/**
 * Represents the controller of the tool window.
 */
public class ToolWindowController implements Disposable {
  Project project;
  ToolWindowComponent component;

  public ToolWindowController(Project project) {
    this.project = project;
  }

  /**
   * Creates and returns the tool window component.
   *
   * @return the tool window component
   */
  public ToolWindowComponent getContent() {
    this.component = new ToolWindowComponent(
        new ToolBarComponent(new NodePickerComponent(project),
            new NodeActionComponent(project),
            new SelectedNodeStateComponent(project)
        ),
        new DetailsComponent(project));
    return this.component;
  }

  @Override
  public void dispose() {
    component = null;
  }
}
