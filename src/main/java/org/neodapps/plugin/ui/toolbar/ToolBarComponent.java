/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.toolbar;

import com.intellij.openapi.Disposable;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import org.neodapps.plugin.ui.toolbar.action.NodeActionComponent;

/**
 * Represents the tool bar component.
 */
public class ToolBarComponent extends Wrapper implements Disposable {

  private JPanel toolbar;

  /**
   * Creates the toolbar component.
   *
   * @param nodePickerComponent component that picks nodes
   * @param nodeActionComponent component that has node actions
   */
  public ToolBarComponent(NodePickerComponent nodePickerComponent,
                          NodeActionComponent nodeActionComponent) {

    toolbar = new JPanel(new FlowLayout(FlowLayout.LEADING));
    toolbar.setBorder(JBUI.Borders.customLineBottom(JBColor.border()));
    toolbar.add(nodePickerComponent);
    toolbar.add(nodeActionComponent);
    setContent(toolbar);
  }

  @Override
  public void dispose() {
    toolbar = null;
  }
}
