/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.wallets;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.services.chain.WalletService;
import org.neodapps.plugin.services.express.NeoExpressService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents transfer asset component.
 */
public class TransferAssetPopupComponent implements Disposable {

  private final Project project;
  private final PrivateChain chain;
  private final List<NEP6Wallet> wallets;

  private JBPopup popup;
  private ToolWindowButton actionButton;
  private JBTextField amountField;
  private JBTextField assetField;

  /**
   * Creates transfer asset popup component.
   */
  public TransferAssetPopupComponent(Project project, PrivateChain chain,
                                     List<NEP6Wallet> wallets) {
    this.project = project;
    this.chain = chain;
    this.wallets = wallets;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    var builder = JBPopupFactory
        .getInstance().createComponentPopupBuilder(getComponent(), amountField);
    builder.setTitle(NeoMessageBundle.message("toolwindow.transfer.wallet.asset"));
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

  private JPanel getComponent() {
    final var builder = new FormBuilder();

    var walletNames = new ArrayList<String>();
    walletNames.add(WalletService.GENESIS);
    wallets.forEach(w -> walletNames.add(w.getName()));

    // amount field
    this.amountField = new JBTextField();
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.transfer.wallet.asset.amount")),
        amountField, true);

    // from dropdown
    var fromList = new JBList<>(walletNames);
    fromList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fromList.setSelectedValue(WalletService.GENESIS, true);

    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.transfer.wallet.asset.from")), fromList,
        true);

    // to dropdown
    var toList = new JBList<>(walletNames);
    toList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    if (walletNames.size() > 1) {
      toList.setSelectedIndex(1);
    } else {
      toList.setSelectedIndex(0);
    }
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.transfer.wallet.asset.to")), toList,
        true);

    // amount field
    this.assetField =
        new JBTextField(NeoMessageBundle.message("toolwindow.transfer.wallet.asset.neo"));
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.transfer.wallet.asset.name")),
        assetField, true);

    // transfer asset
    this.actionButton =
        new ToolWindowButton(NeoMessageBundle.message("toolwindow.transfer.wallet.asset.action"),
            AllIcons.Actions.Execute,
            e -> {
              closePopup();
              transferAsset(amountField.getText(), assetField.getText(),
                  fromList.getSelectedValue(), toList.getSelectedValue(), chain);
            });

    builder.addComponent(actionButton);

    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(4, 10));
    return content;
  }

  private void transferAsset(String amount, String asset, String from, String to, ChainLike chain) {
    var worker = new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
        project.getService(NeoExpressService.class)
            .transferAsset(amount, asset, from, to, chain);
        return null;
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
    this.popup = null;
    this.actionButton = null;
    this.assetField = null;
  }
}
