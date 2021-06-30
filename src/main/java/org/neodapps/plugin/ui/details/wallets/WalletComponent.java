/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.wallets;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.blockchain.express.ExpressWallet;
import org.neodapps.plugin.services.chain.AssetBalance;
import org.neodapps.plugin.services.chain.BlockchainService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows wallet details.
 */
public class WalletComponent extends Wrapper {

  final Project project;
  final Wrapper walletComponent;

  /**
   * Creates the wallet list component.
   *
   * @param project intellij project
   * @param chain   selected chain
   */
  public WalletComponent(Project project, PrivateChain chain) {
    this.project = project;
    walletComponent = new Wrapper();

    var panel = JBUI.Panels.simplePanel();
    panel.addToTop(getToolBar(chain));
    panel.addToCenter(walletComponent);

    walletComponent.setContent(getWalletListComponent(chain));
    setContent(panel);
  }

  private JComponent getToolBar(PrivateChain chain) {
    // toolbar has two buttons
    var buttonPanel = JBUI.Panels.simplePanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    // create wallet
    var createWalletButton =
        new ToolWindowButton(
            NeoMessageBundle.message("toolwindow.create.wallet"),
            AllIcons.General.Add, actionEvent -> {
          var popup = new CreateWalletPopupComponent(project, chain);
          popup.showPopup();
        });
    buttonPanel.add(createWalletButton);

    // refresh wallet balances
    // refresh icon
    var refreshButton =
        new ToolWindowButton("",
            AllIcons.Javaee.UpdateRunningApplication,
            actionEvent -> {
              walletComponent.setContent(getWalletListComponent(chain));
            });
    buttonPanel.add(refreshButton);
    return buttonPanel;
  }

  private JComponent getWalletListComponent(PrivateChain chain) {
    var config = chain.getConfig();

    var addresses = config.getWallets()
        .stream().filter(a -> a.getAccounts().size() > 0)
        .collect(Collectors
            .toMap(item -> item.getAccounts().get(0).getScriptHash(), ExpressWallet::getName));

    var balances = project.getService(BlockchainService.class)
        .getAssetBalances(addresses.keySet(), chain);

    var panel = new JPanel(new GridLayout(balances.size(), 1));
    balances.forEach((address, listBalances) -> {
      panel.add(getWalletComponent(addresses.get(address), address, listBalances));
    });

    return new JBScrollPane(panel);
  }

  private JComponent getWalletComponent(String name, String address, List<AssetBalance> balances) {
    var panel = new JPanel(new GridBagLayout());
    final var gbc = new GridBagConstraints();
    panel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    // name and address
    var namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    var nameField = new JBLabel(name);
    nameField.setIcon(AllIcons.Ide.HectorSyntax);
    namePanel.add(nameField);
    namePanel.add(getTextField(address));
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(namePanel, gbc);

    gbc.gridy = 1;
    if (balances.size() > 0) {
      final var builder = new FormBuilder();
      // asset balances
      for (AssetBalance balance : balances) {
        var singleAssetName = new JPanel(new FlowLayout(FlowLayout.LEADING));
        singleAssetName.add(new JBLabel(balance.getSymbol()));
        singleAssetName.add(getTextField(balance.getAddress()));
        builder
            .addLabeledComponent(singleAssetName,
                getTextField(new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US))
                    .format(balance.getBalance())));
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
