/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.wallets;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.blockchain.express.ExpressConfig;
import org.neodapps.plugin.blockchain.express.ExpressWallet;
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

    walletComponent.setContent(getWalletListComponent(chain.getConfig()));
    setContent(panel);
  }

  private JComponent getToolBar(PrivateChain chain) {
    // toolbar has two buttons
    JPanel buttonPanel = JBUI.Panels.simplePanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    // create wallet
    JButton createWalletButton =
        new ToolWindowButton(
            NeoMessageBundle.message("toolwindow.create.wallet"),
            AllIcons.General.Add, actionEvent -> {
          var popup = new CreateWalletPopupComponent(project, chain);
          popup.showPopup();
        });
    buttonPanel.add(createWalletButton);

    // refresh wallet balances
    // refresh icon
    JButton refreshButton =
        new ToolWindowButton("",
            AllIcons.Javaee.UpdateRunningApplication,
            actionEvent -> {
              walletComponent.setContent(getWalletListComponent(chain.getConfig()));
            });
    buttonPanel.add(refreshButton);
    return buttonPanel;
  }

  private JComponent getWalletListComponent(ExpressConfig config) {
    var panel = new JPanel();
    var layout = new GridLayout();
    layout.setColumns(1);
    panel.setLayout(layout);

    for (ExpressWallet wallet : config.getWallets()) {
      panel.add(getWalletComponent(wallet));
    }

    return panel;
  }

  private JComponent getWalletComponent(ExpressWallet wallet) {
    if (wallet.getAccounts().size() == 0) {
      return new JPanel();
    }

    var builder = new FormBuilder();
    builder.addLabeledComponent(new JBLabel("Name"), getTextField(wallet.getName()));

    var account = wallet.getAccounts().get(0);
    builder.addLabeledComponent(new JBLabel("Address"), getTextField(account.getScriptHash()));
    builder.addLabeledComponent(new JBLabel("Neo"), getTextField("500"));
    builder.addLabeledComponent(new JBLabel("Gas"), getTextField("1000"));

    var content = builder.getPanel();
    content.setBorder(JBUI.Borders.customLine(JBColor.border()));
    return content;
  }

  private JBTextField getTextField(String content) {
    var field = new JBTextField(content);
    field.setEditable(false);
    return field;
  }
}
