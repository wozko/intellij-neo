/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.wallets;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.wallet.nep6.NEP6Account;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.Chain;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.services.chain.BlockchainService;
import org.neodapps.plugin.services.chain.TokenBalance;
import org.neodapps.plugin.services.chain.WalletService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows wallet details.
 */
public class WalletComponent extends Wrapper implements Disposable {

  private final Project project;
  private final ChainLike chain;

  private Wrapper walletWrapper;
  private Wrapper toolBarWrapper;


  /**
   * Creates the wallet list component.
   *
   * @param project intellij project
   * @param chain   selected chain
   */
  public WalletComponent(Project project, ChainLike chain) {
    this.project = project;
    this.chain = chain;
    this.toolBarWrapper = new Wrapper();
    this.walletWrapper = new Wrapper();

    toolBarWrapper.setContent(new JPanel());
    walletWrapper.setContent(new JPanel());

    var panel = JBUI.Panels.simplePanel();
    panel.addToTop(toolBarWrapper);
    panel.addToCenter(walletWrapper);
    setWalletLoading();

    setContent(panel);
    loadWalletDetails(chain);
  }

  private void loadWalletDetails(ChainLike selected) {
    var worker = new SwingWorker<List<NEP6Wallet>, Void>() {
      @Override
      protected List<NEP6Wallet> doInBackground() {
        return project.getService(WalletService.class).getWallets(selected);
      }

      @Override
      protected void done() {
        List<NEP6Wallet> list;
        try {
          list = get();
          toolBarWrapper.setContent(getToolBar(list));
          loadBalances(list);

        } catch (InterruptedException | ExecutionException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };
    worker.execute();
  }

  private void loadBalances(List<NEP6Wallet> wallets) {
    var worker = new SwingWorker<Map<NEP6Wallet, List<TokenBalance>>, Void>() {
      @Override
      protected Map<NEP6Wallet, List<TokenBalance>> doInBackground() {
        return project.getService(BlockchainService.class).getTokenBalances(wallets, chain);
      }

      @Override
      protected void done() {
        Map<NEP6Wallet, List<TokenBalance>> balances;
        try {
          balances = get();
          walletWrapper.setContent(getWalletListComponent(balances));
        } catch (InterruptedException | ExecutionException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };
    worker.execute();
  }


  private void setWalletLoading() {
    var panel = new JPanel();
    panel.setBorder(JBUI.Borders.empty(5));
    panel.add(new JBLabel(NeoMessageBundle.message("toolwindow.loading")));
    this.walletWrapper.setContent(panel);
    this.toolBarWrapper.setContent(panel);
  }


  private JComponent getToolBar(List<NEP6Wallet> wallets) {
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

      var transferAssetButton = new ToolWindowButton(
          NeoMessageBundle.message("toolwindow.transfer.wallet.asset"),
          AllIcons.Nodes.Alias,
          actionEvent -> {
            var popup = new TransferAssetPopupComponent(project, (PrivateChain) chain, wallets);
            popup.showPopup();
          }
      );

      buttonPanel.add(transferAssetButton);
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
              setWalletLoading();
              loadWalletDetails(chain);
            });
    buttonPanel.add(refreshButton);
    return buttonPanel;
  }

  private JComponent getWalletListComponent(Map<NEP6Wallet, List<TokenBalance>> balances) {
    final var panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    balances.forEach(
        (wallet, tokenBalances) -> panel.add(getWalletComponent(wallet, balances.get(wallet))));

    return new JBScrollPane(panel);
  }

  private JComponent getWalletComponent(NEP6Wallet wallet, List<TokenBalance> balances) {
    var panel = new JPanel(new GridBagLayout());
    final var gbc = new GridBagConstraints();
    panel.setBorder(
        new CompoundBorder(JBUI.Borders.customLine(JBColor.border()), JBUI.Borders.empty(5, 2)));

    // name and address
    var namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    var nameField = new JBLabel(wallet.getName());
    nameField.setIcon(AllIcons.Ide.HectorSyntax);
    namePanel.add(nameField);
    for (NEP6Account account : wallet.getAccounts()) {
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
    field.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent focusEvent) {
        field.selectAll();
      }

      @Override
      public void focusLost(FocusEvent focusEvent) {
      }
    });
    field.setEditable(false);
    return field;
  }

  @Override
  public void dispose() {
    this.walletWrapper = null;
    this.toolBarWrapper = null;
  }
}
