/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.contracts.list;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ExpressContractState;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.neodapps.plugin.NeoMessageBundle;

/**
 * Represents the component that shows deployed contracts.
 */
public class DeployedContractListComponent extends Wrapper implements Disposable {

  private final List<ExpressContractState> contracts;

  private JBSplitter panel;
  private Wrapper details;

  /**
   * Creates the component that shows deployed contracts.
   */
  public DeployedContractListComponent(List<ExpressContractState> deployedContracts) {
    this.contracts = deployedContracts;

    this.panel = new JBSplitter(0.2f);
    panel.setFirstComponent(getContractList());

    this.details = new Wrapper();
    this.details.setContent(new JPanel());
    panel.setSecondComponent(details);
    setContent(this.panel);
  }

  private JPanel getContractList() {
    final var panel = JBUI.Panels.simplePanel();

    var title = new JBLabel(NeoMessageBundle.message("contracts.deployed"));
    title.setBorder(JBUI.Borders.empty(3, 2));
    panel.addToTop(title);

    var contractList = new JBList<>(contracts);

    contractList.setCellRenderer(
        (items, contractState, i, b, b1) -> {
          var name = new JBLabel(contractState.getManifest().getName());
          name.setBorder(JBUI.Borders.empty(3, 2));
          return name;
        });
    contractList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    contractList.getSelectionModel().addListSelectionListener(
        listSelectionEvent -> showDetails(contractList.getSelectedValue()));
    panel.addToCenter(contractList);
    return panel;
  }

  private void showDetails(ExpressContractState selected) {
    final var panel = JBUI.Panels.simplePanel();
    var builder = new FormBuilder();
    var contractName = new JBLabel(selected.getManifest().getName());
    contractName.setIcon(AllIcons.FileTypes.UiForm);

    var contractHash = new JBTextField(selected.getHash().toAddress());
    contractHash.setEditable(false);
    builder.addLabeledComponent(contractName, contractHash);

    // list of operations
    var methods = selected.getManifest().getAbi().getMethods();
    if (methods.size() > 0) {
      var operationsLabel = new JBLabel(NeoMessageBundle.message("contracts.list.operations"));
      operationsLabel.setIcon(AllIcons.Debugger.VariablesTab);
      builder.addLabeledComponent(operationsLabel, getMethodDetails(methods));
    }
    var content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(5, 10));
    panel.add(content, BorderLayout.NORTH);
    this.details.setContent(panel);
  }

  private JPanel getMethodDetails(List<ContractManifest.ContractABI.ContractMethod> methods) {
    var methodDetails = new JPanel(new GridLayout(methods.size(), 1));
    methodDetails.setBorder(JBUI.Borders.empty(1, 10));
    for (ContractManifest.ContractABI.ContractMethod method : methods) {
      var params =
          method.getParameters().stream()
              .map(c -> String.format("%s:%s", c.getParamName(), c.getParamType().name()))
              .collect(Collectors.joining(", "));
      var methodName = String.format("%s(%s)", method.getName(), params);
      var methodNameLabel = new JBLabel(methodName);
      methodNameLabel.setBorder(JBUI.Borders.empty(2, 1));
      methodDetails.add(methodNameLabel);
    }
    methodDetails.setBorder(JBUI.Borders.empty(2, 1));
    return methodDetails;
  }

  @Override
  public void dispose() {
    this.panel = null;
    this.details = null;
  }
}
