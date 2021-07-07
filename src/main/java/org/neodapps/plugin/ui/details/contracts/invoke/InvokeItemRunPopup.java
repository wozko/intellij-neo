package org.neodapps.plugin.ui.details.contracts.invoke;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.types.ContractParameter;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.services.chain.BlockchainService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the popup shown when the uses clicks run step.
 */
public class InvokeItemRunPopup implements Disposable {
  private final Project project;
  private final ChainLike chain;
  private final List<NEP6Wallet> wallets;
  NeoGetContractState.ContractState contractState;
  ContractManifest.ContractABI.ContractMethod method;
  List<ContractParameter> parameters;

  private JBPopup popup;
  private ToolWindowButton actionButton;

  /**
   * Creates the popup shown when the uses clicks run step.
   */
  public InvokeItemRunPopup(Project project, ChainLike chain,
                            List<NEP6Wallet> wallets,
                            NeoGetContractState.ContractState contractState,
                            ContractManifest.ContractABI.ContractMethod method,
                            List<ContractParameter> parameters) {
    this.project = project;
    this.chain = chain;
    this.wallets = wallets;
    this.contractState = contractState;
    this.method = method;
    this.parameters = parameters;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    var builder = JBPopupFactory
        .getInstance().createComponentPopupBuilder(getComponent(), null);
    builder.setTitle(NeoMessageBundle.message("contracts.invoke.action.title"));
    builder.setFocusable(true);
    builder.setMovable(true);
    builder.setResizable(true);
    builder.setRequestFocus(true);
    builder.setCancelOnOtherWindowOpen(false);
    builder.setCancelOnClickOutside(false);
    builder.setCancelOnWindowDeactivation(false);
    popup = builder.createPopup();
    popup.showInCenterOf(
        Objects.requireNonNull(ToolWindowManager.getInstance(project).getToolWindow("Neo"))
            .getComponent());
  }

  private JComponent getComponent() {
    final var builder = new FormBuilder();
    var walletList = new JBList<>(wallets);
    walletList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    walletList
        .setCellRenderer((wallets, wallet, i, b, b1) -> new JBLabel(wallet.getName()));
    boolean showError = false;
    if (wallets.size() > 0) {
      walletList.setSelectedIndex(0);
    } else {
      showError = true;
    }
    builder
        .addLabeledComponent(new JBLabel(NeoMessageBundle.message("contracts.invoke.pick.wallet")),
            walletList, true);

    // transfer asset
    this.actionButton =
        new ToolWindowButton(NeoMessageBundle.message("contracts.invoke.action"),
            AllIcons.Actions.Execute,
            e -> {
              closePopup();
              runSteps(walletList.getSelectedValue());
            });
    builder.addComponent(actionButton);

    if (showError) {
      builder.addComponent(new JBLabel(NeoMessageBundle.message("contracts.invoke.error")));
      this.actionButton.setEnabled(false);
    }

    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(4, 10));
    return content;
  }

  private void runSteps(NEP6Wallet wallet) {
    var worker = new SwingWorker<String, Void>() {

      @Override
      protected String doInBackground() {
        return project.getService(BlockchainService.class)
            .invokeContractMethod(chain, contractState, method, parameters, wallet);
      }

      @Override
      protected void done() {
        try {
          var rawResponse = get();
          var size = rawResponse.length();
        } catch (InterruptedException | ExecutionException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };

    worker.execute();
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
    popup = null;
    actionButton = null;
  }
}
