/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.toolbar.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.services.express.NeoExpressService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the create private net popup component.
 */
public class CreatePrivateNetPopupComponent implements Disposable {

  private final Project project;
  private JBPopup popup;
  private JTextField nameField;
  private JBLabel nameFieldError;
  private ToolWindowButton actionButton;

  private int nodeCount;

  public CreatePrivateNetPopupComponent(Project project) {
    this.project = project;
  }

  private JPanel getComponent() {
    var builder = new FormBuilder();

    // name field
    nameField = new JTextField(NeoMessageBundle.message("toolwindow.private.net.default.name"));
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.private.net.prompt.name")), nameField,
        true);

    nameFieldError = new JBLabel(NeoMessageBundle.message("toolwindow.private.net.name.error"));
    nameFieldError.setFontColor(UIUtil.FontColor.BRIGHTER);
    nameFieldError.setVisible(false);
    builder.addComponent(nameFieldError);

    // node list
    var nodeList = new JBList<>(Arrays.asList(1, 4, 7));
    nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.private.net.prompt.nodes")),
        new JBScrollPane(nodeList), true);

    nodeList.addListSelectionListener(e -> {
      nodeCount = nodeList.getSelectedValue();
    });
    nodeList.setSelectedIndex(0);

    // create button
    actionButton =
        new ToolWindowButton(NeoMessageBundle.message("toolwindow.private.net.prompt.action"),
            AllIcons.Actions.Execute,
            e -> {
              var name = nameField.getText();
              if (name.isEmpty()) {
                nameFieldError.setVisible(true);
              } else {
                closePopup();
                project.getService(NeoExpressService.class)
                    .createPrivateNet(nodeCount, 53, name);
              }
            });

    builder.addComponent(actionButton);
    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(0, 10));
    return content;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    var builder = JBPopupFactory
        .getInstance().createComponentPopupBuilder(getComponent(), nameField);
    builder.setTitle(NeoMessageBundle.message("toolwindow.private.net.prompt.title"));
    builder.setFocusable(true);
    builder.setMovable(true);
    builder.setResizable(true);
    builder.setRequestFocus(true);
    popup = builder.createPopup();
    popup.showInCenterOf(
        Objects.requireNonNull(ToolWindowManager.getInstance(project).getToolWindow("Neo"))
            .getComponent());
  }

  /**
   * Closes the popup.
   */
  public void closePopup() {
    if (popup != null && !popup.isDisposed()) {
      popup.dispose();
      popup = null;
    }
  }


  @Override
  public void dispose() {
    if (popup != null && !popup.isDisposed()) {
      popup.dispose();
    }
    popup = null;
    nameField = null;
    actionButton = null;
  }
}
