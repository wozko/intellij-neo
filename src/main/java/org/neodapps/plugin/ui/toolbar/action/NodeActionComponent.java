/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.toolbar.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.services.chain.ChainListService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the node action component.
 */
public class NodeActionComponent extends Wrapper implements Disposable {

  private ToolWindowButton createPrivateNetButton;
  private ToolWindowButton refreshButton;

  private final Project project;

  /**
   * Creates the node action component.
   */
  public NodeActionComponent(Project project) {
    this.project = project;
    createPrivateNetButton =
        new ToolWindowButton(NeoMessageBundle.message("toolwindow.create.private.net"),
            AllIcons.General.Add,
            e -> {
              var popup = new CreatePrivateNetPopupComponent(project);
              popup.showPopup();
            });

    refreshButton =
        new ToolWindowButton("", AllIcons.Javaee.UpdateRunningApplication, e -> refresh());

    var panel = new JPanel(new FlowLayout());
    panel.setBorder(JBUI.Borders.customLine(JBColor.border()));
    panel.add(createPrivateNetButton);
    panel.add(refreshButton);
    setContent(panel);
  }

  private void refresh() {
    var worker = new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
        project.getService(ChainListService.class).setAppliedChain(null);
        return null;
      }
    };
    worker.execute();
  }

  @Override
  public void dispose() {
    createPrivateNetButton = null;
    refreshButton = null;
  }
}
