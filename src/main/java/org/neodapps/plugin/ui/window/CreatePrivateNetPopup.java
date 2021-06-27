/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.services.NeoExpressService;

/**
 * Asks params for private net creation.
 */
public class CreatePrivateNetPopup implements Disposable, ActionListener {
  private final Project project;
  private JBPopup popup;
  private JBLoadingPanel panel;
  private JTextField nameField;
  private JBList<Integer> nodeList;

  public CreatePrivateNetPopup(Project project) {
    this.project = project;
  }

  private JBLoadingPanel getComponent() {
    panel = new JBLoadingPanel(new BorderLayout(), this, 300);
    FormBuilder builder = new FormBuilder();

    nameField = new JTextField(NeoMessageBundle.message("toolwindow.private.net.default.name"));
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.private.net.prompt.name")), nameField,
        true);

    nodeList = new JBList<>(Arrays.asList(1, 4, 7));
    nodeList.setSelectedIndex(ListSelectionModel.SINGLE_SELECTION);
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.private.net.prompt.nodes")),
        new JBScrollPane(nodeList), true);

    JButton ok = new JButton();
    ok.setText(NeoMessageBundle.message("toolwindow.private.net.prompt.action"));
    ok.addActionListener(this);
    builder.addComponent(ok);
    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(0, 10));
    panel.add(content);
    return panel;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    ComponentPopupBuilder builder = JBPopupFactory
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

  @Override
  public void dispose() {
    if (popup != null) {
      popup.dispose();
    }
    nameField = null;
    panel = null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String name = nameField.getText();
    Integer choice = nodeList.getSelectedValue();

    // press okay
    dispose();

    // create
    final int addressVersion = 53;
    project.getService(NeoExpressService.class).createPrivateNet(choice, addressVersion, name);
  }
}