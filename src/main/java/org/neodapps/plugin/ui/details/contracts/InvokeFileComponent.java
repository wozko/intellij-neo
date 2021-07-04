/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.contracts;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.services.chain.InvokeFile;
import org.neodapps.plugin.services.chain.InvokeFileItem;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows an invoke file.
 */
public class InvokeFileComponent extends Wrapper implements Disposable {
  private final Project project;
  private final InvokeFile invokeFile;

  private JPanel itemListPanel;
  private Map<String, JPanel> invokeItemComponentMap;

  /**
   * Creates the component that shows an invoke file.
   *
   * @param project        intellij project
   * @param invokeFilePath path to invoke file
   */
  public InvokeFileComponent(Project project, String invokeFilePath) {
    this.project = project;
    this.invokeFile = new InvokeFile(invokeFilePath);

    this.itemListPanel = new JPanel();
    this.itemListPanel.setLayout(new BoxLayout(this.itemListPanel, BoxLayout.Y_AXIS));

    this.invokeItemComponentMap = new HashMap<>();
    setComponent();
  }

  private void setComponent() {
    for (InvokeFileItem item : invokeFile.getItems()) {
      var itemPanel = new JPanel();
      invokeItemComponentMap.put(item.getId(), itemPanel);
      itemListPanel.add(itemPanel);
      var builder = new FormBuilder();
      builder
          .addLabeledComponent(
              new JBLabel(NeoMessageBundle.message("contracts.invoke.file.contract")),
              new JBTextField(item.getContract()), true);
      builder
          .addLabeledComponent(
              new JBLabel(NeoMessageBundle.message("contracts.invoke.file.operation")),
              new JBTextField(item.getOperation()), true);

      // argument list
      var argumentBuilder = new FormBuilder();
      var args = item.getArgs();
      for (int i = 0; i < args.size(); i++) {
        var arg = args.get(i);
        argumentBuilder.addLabeledComponent(
            new JBLabel(NeoMessageBundle.message("contracts.invoke.file.argument", i + 1)),
            new JBTextField(arg.toString()));
      }
      var argsPanel = argumentBuilder.getPanel();
      argsPanel.setBorder(JBUI.Borders.empty(2, 5));
      builder.addLabeledComponent(
          new JBLabel(NeoMessageBundle.message("contracts.invoke.file.arguments")),
          argsPanel, true);

      // actions
      var actionsPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      // delete button
      var deleteButton =
          new ToolWindowButton(NeoMessageBundle.message("contracts.invoke.file.delete.step"),
              AllIcons.Hierarchy.MethodNotDefined, e -> {
            var toDelete = invokeItemComponentMap.get(item.getId());
            if (toDelete != null) {
              itemListPanel.remove(toDelete);
            }
          });
      actionsPanel.add(deleteButton);
      // run step button
      var runStepButton =
          new ToolWindowButton(NeoMessageBundle.message("contracts.invoke.file.run.step"),
              AllIcons.Duplicates.SendToTheRight, e -> {
            // todo: run step
          });
      actionsPanel.add(runStepButton);

      builder.addComponent(actionsPanel);
      itemPanel.add(builder.getPanel());
    }
  }

  @Override
  public void dispose() {
    this.itemListPanel = null;
    this.invokeItemComponentMap = null;
  }
}
