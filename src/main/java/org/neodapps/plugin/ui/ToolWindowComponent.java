/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui;

import com.intellij.openapi.Disposable;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.panels.Wrapper;
import org.neodapps.plugin.ui.details.DetailsComponent;
import org.neodapps.plugin.ui.toolbar.ToolBarComponent;

/**
 * Represents the tool window component.
 */
class ToolWindowComponent extends Wrapper implements Disposable {

  private JBSplitter panel;

  /**
   * Creates the tool window component.
   */
  ToolWindowComponent(ToolBarComponent toolBarComponent, DetailsComponent detailsComponent) {

    // split window into two panels
    panel = new JBSplitter(true, 0.2f);

    // set toolbar
    panel.setFirstComponent(toolBarComponent);

    // set data panel
    panel.setSecondComponent(detailsComponent);

    setContent(panel);
  }

  @Override
  public void dispose() {
    panel = null;
  }
}
