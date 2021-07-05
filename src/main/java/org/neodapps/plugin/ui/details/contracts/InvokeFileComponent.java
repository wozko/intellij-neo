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
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.services.chain.InvokeFile;
import org.neodapps.plugin.services.chain.InvokeFileItem;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows an invoke file.
 */
public class InvokeFileComponent extends Wrapper implements Disposable {
  private final Project project;
  private final InvokeFile invokeFile;
  private final List<NEP6Wallet> wallets;
  private final List<NeoGetContractState.ContractState> contracts;

  private Wrapper itemListWrapper;
  private JPanel itemListPanel;
  private Map<String, JPanel> invokeItemComponentMap;

  /**
   * Creates the component that shows an invoke file.
   *
   * @param project    intellij project
   * @param invokeFile invoke file representation
   */
  public InvokeFileComponent(Project project, InvokeFile invokeFile,
                             List<NEP6Wallet> wallets,
                             List<NeoGetContractState.ContractState> contractList) {
    this.project = project;
    this.invokeFile = invokeFile;
    this.wallets = wallets;
    this.contracts = contractList;

    this.invokeItemComponentMap = new HashMap<>();
    setContent(itemListWrapper);

    this.itemListWrapper = new Wrapper();
    itemListWrapper.setContent(getLoadingComponent());
    verifyInvokeFileAndSetComponent();
  }

  private void verifyInvokeFileAndSetComponent() {
    var worker = new SwingWorker<Boolean, Void>() {
      @Override
      protected Boolean doInBackground() {
        List<InvokeFileItem> items;
        try {
          items = invokeFile.getItems();
        } catch (IOException e) {
          NeoNotifier.notifyError(project, e.getMessage());
          return false;
        }

        // verify contract and method names with deployed contract
        for (InvokeFileItem item : items) {
          NeoGetContractState.ContractState matchingContract = null;
          for (NeoGetContractState.ContractState contract : contracts) {
            if (item.getContract().equals(contract.getManifest().getName())) {
              matchingContract = contract;
              break;
            }
          }
          if (matchingContract == null) {
            NeoNotifier.notifyError(project, NeoMessageBundle
                .message("contracts.invoke.file.contract.not.found", item.getContract()));
            return false;
          }

          var matchingMethodFound =
              matchingContract.getManifest().getAbi().getMethods().stream().map(
                  ContractManifest.ContractABI.ContractMethod::getName)
                  .anyMatch(m -> m.equals(item.getOperation()));

          if (!matchingMethodFound) {
            NeoNotifier.notifyError(project, NeoMessageBundle
                .message("contracts.invoke.file.contract.method.not.found", item.getOperation(),
                    matchingContract.getManifest().getName()));
            return false;
          }
        }
        return true;
      }

      @Override
      protected void done() {
        boolean isVerified;
        try {
          isVerified = get();
          if (isVerified) {
            itemListWrapper.setContent(getInvokeItemListComponent(invokeFile.getItems()));
          }
        } catch (InterruptedException | ExecutionException | IOException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };
    worker.execute();
  }

  private JPanel getInvokeItemListComponent(List<InvokeFileItem> items) {
    itemListPanel = new JPanel();
    itemListPanel.setLayout(new BoxLayout(this.itemListPanel, BoxLayout.Y_AXIS));

    for (InvokeFileItem item : items) {
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
    return itemListPanel;
  }

  private JPanel getLoadingComponent() {
    var panel = new JPanel();
    panel.setBorder(JBUI.Borders.empty(5));
    panel.add(new JBLabel(NeoMessageBundle.message("toolwindow.loading")));
    return panel;
  }

  @Override
  public void dispose() {
    this.itemListWrapper = null;
    this.itemListPanel = null;
    this.invokeItemComponentMap = null;
  }
}
