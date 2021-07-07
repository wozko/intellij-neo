package org.neodapps.plugin.ui.details;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import javax.swing.JComponent;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.ui.details.blocks.BlockInfoTable;
import org.neodapps.plugin.ui.details.contracts.ContractsComponent;
import org.neodapps.plugin.ui.details.wallets.WalletComponent;

/**
 * Represents main data component that use a tabbed ui.
 */
public class TabsComponent extends Wrapper implements Disposable {
  private final Project project;
  private JBTabs tabs;

  /**
   * Creates the tabbed component.
   *
   * @param project intellij project
   */
  public TabsComponent(Project project, ChainLike chain) {
    this.project = project;
    this.tabs = new JBTabsImpl(project);
    setContent(getTabs(chain));
  }

  private JComponent getTabs(ChainLike chain) {
    tabs.removeAllTabs();
    addBlockTableComponent(chain);

    // for now wallets and contracts tab only shown for private nets
    // while it should work for testnet, it is not tested properly
    if (chain.getType().equals(BlockChainType.PRIVATE)) {
      addWalletComponent(chain);
      addContractsComponent(chain);
    }

    return tabs.getComponent();
  }

  private void addBlockTableComponent(ChainLike selectedChain) {
    var table = new BlockInfoTable(this.project, selectedChain);
    TabInfo blockTab = new TabInfo(new JBScrollPane(table))
        .setText(NeoMessageBundle.message("toolwindow.tabs.blocks"));
    tabs.addTab(blockTab);
  }

  private void addWalletComponent(ChainLike selectedChain) {
    var wallet = new WalletComponent(project, selectedChain);
    TabInfo walletTab = new TabInfo(new JBScrollPane(wallet))
        .setText(NeoMessageBundle.message("toolwindow.tabs.wallets"));
    tabs.addTab(walletTab);
  }

  private void addContractsComponent(ChainLike selectedChain) {
    var contractsComponent = new ContractsComponent(project, selectedChain);
    TabInfo contractTab = new TabInfo(new JBScrollPane(contractsComponent))
        .setText(NeoMessageBundle.message("toolwindow.tabs.contracts"));
    tabs.addTab(contractTab);
  }

  @Override
  public void dispose() {
    tabs.removeAllTabs();
    tabs = null;
  }
}
