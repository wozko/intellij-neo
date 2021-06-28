/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.services.ChainListService;
import org.neodapps.plugin.topics.RefreshActionNotifier;

/**
 * Represents the interface used to create/refresh nodes.
 */
public class ActionComponent extends Wrapper {

  private final Project project;

  /**
   * Creates the interface used to create/refresh nodes.
   */
  public ActionComponent(@NotNull Project project) {
    this.project = project;
    setContent(getContent());
  }

  private JComponent getContent() {
    JPanel buttonPanel = JBUI.Panels.simplePanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    // create private net button
    JButton createPrivateNetButton =
        new ToolWindowButton(
            NeoMessageBundle.message("toolwindow.create.private.net"),
            AllIcons.General.Add, actionEvent -> {
          CreatePrivateNetPopup popup = new CreatePrivateNetPopup(project);
          popup.showPopup();
        });
    buttonPanel.add(createPrivateNetButton);

    // refresh icon
    JButton refreshButton =
        new ToolWindowButton("", AllIcons.Javaee.UpdateRunningApplication,
            actionEvent -> {
              // clear selection on refresh
              project.getService(ChainListService.class).setSelectedValues(null);

              // publish refresh event so the ui get updated
              var publisher =
                  project.getMessageBus().syncPublisher(RefreshActionNotifier.REFRESH);
              publisher.refreshActionCalled();
            });
    buttonPanel.add(refreshButton);
    return buttonPanel;
  }
}
