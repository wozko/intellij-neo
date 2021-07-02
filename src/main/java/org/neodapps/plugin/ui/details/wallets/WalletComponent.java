/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.wallets;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.Chain;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.services.chain.BlockchainService;
import org.neodapps.plugin.services.chain.TokenBalance;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows wallet details.
 */
public class WalletComponent extends Wrapper {

  final Project project;
  final ChainLike chain;
  final List<Wallet> wallets;

  final Wrapper walletComponent;

  /**
   * Creates the wallet list component.
   *
   * @param project intellij project
   * @param chain   selected chain
   */
  public WalletComponent(Project project, ChainLike chain, List<Wallet> wallets) {
    this.project = project;
    this.chain = chain;
    this.wallets = wallets;

    walletComponent = new Wrapper();
    var panel = JBUI.Panels.simplePanel();
    panel.addToTop(getToolBar());
    panel.addToCenter(walletComponent);
    walletComponent.setContent(getWalletListComponent());
    setContent(panel);
  }

  private JComponent getToolBar() {
    // toolbar has two buttons
    var buttonPanel = JBUI.Panels.simplePanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    if (chain.getType().equals(BlockChainType.PRIVATE)) {
      // create wallet
      // use neo-express wallets
      var createWalletButton =
          new ToolWindowButton(
              NeoMessageBundle.message("toolwindow.create.wallet"),
              AllIcons.General.Add, actionEvent -> {
            var popup = new CreateWalletPopupComponent(project, (PrivateChain) chain);
            popup.showPopup();
          });
      buttonPanel.add(createWalletButton);
    } else {
      // users can import
      var importWalletButton = new ToolWindowButton(
          NeoMessageBundle.message("toolwindow.import.wallet"),
          AllIcons.FileTypes.AddAny,
          actionEvent -> {
            var popup = new ImportNep6WalletPopupComponent(project, (Chain) chain);
            popup.showPopup();
          }
      );
      buttonPanel.add(importWalletButton);
    }

    // refresh wallet balances
    // refresh icon
    var refreshButton =
        new ToolWindowButton("",
            AllIcons.Javaee.UpdateRunningApplication,
            actionEvent -> {
              walletComponent.setContent(getWalletListComponent());
            });
    buttonPanel.add(refreshButton);
    return buttonPanel;
  }

  private JComponent getWalletListComponent() {
    var balances = project.getService(BlockchainService.class)
        .getTokenBalances(wallets, chain);
    var panel = new JPanel(new GridLayout(balances.size(), 1));

    var walletList = new JBList<Wallet>(balances.keySet());
    walletList.setCellRenderer(
        (wallList, wallet, i, b, b1) -> getWalletComponent(wallet, balances.get(wallet)));
    panel.add(walletList);
    return new JBScrollPane(panel);
  }

  private JComponent getWalletComponent(Wallet wallet, List<TokenBalance> balances) {
    var panel = new JPanel(new GridBagLayout());
    final var gbc = new GridBagConstraints();
    panel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    // name and address
    var namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    var nameField = new JBLabel(wallet.getName());
    nameField.setIcon(AllIcons.Ide.HectorSyntax);
    namePanel.add(nameField);
    for (Account account : wallet.getAccounts()) {
      namePanel.add(getTextField(account.getAddress()));
    }
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(namePanel, gbc);

    gbc.gridy = 1;
    if (balances.size() > 0) {
      final var builder = new FormBuilder();
      // asset balances
      for (var balance : balances) {
        var singleAssetName = new JPanel(new FlowLayout(FlowLayout.LEADING));
        singleAssetName.add(new JBLabel(balance.getSymbol()));
        singleAssetName.add(getTextField(balance.getHash().toAddress()));
        builder
            .addLabeledComponent(singleAssetName,
                getTextField(new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US))
                    .format(balance.getBalanceWithoutDecimals())));
      }
      gbc.gridheight = balances.size();
      panel.add(builder.getPanel(), gbc);
    } else {
      panel.add(new JBLabel(NeoMessageBundle.message("wallet.no.assets")), gbc);
    }

    return panel;
  }

  private JBTextField getTextField(String content) {
    var field = new JBTextField(content);
    field.setEditable(false);
    return field;
  }
}
