/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import io.neow3j.wallet.Wallet;
import java.util.List;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.services.chain.WalletService;
import org.neodapps.plugin.topics.NodeChangeNotifier;
import org.neodapps.plugin.ui.details.blocks.BlockInfoTable;
import org.neodapps.plugin.ui.details.contracts.ContractsComponent;
import org.neodapps.plugin.ui.details.wallets.WalletComponent;

/**
 * Represents the component with status and tabs.
 */
public class DetailsComponent extends Wrapper implements Disposable {

  private final Project project;
  private ChainLike selectedChain;
  private List<Wallet> wallets;
  private JBTabs tabs;

  /**
   * Creates the component with status and tabs.
   */
  public DetailsComponent(Project project) {
    this.project = project;
    var bus = project.getMessageBus();
    setContent(new JPanel());

    var thisRef = this;
    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, new NodeChangeNotifier() {
      @Override
      public void nodeSelected(ChainLike selectedChain) {
        tabs = new JBTabsImpl(project);
        wallets = project.getService(WalletService.class).getWallets(selectedChain);
        thisRef.selectedChain = selectedChain;

        // add tabs
        addBlockTableComponent();
        addWalletComponent();
        addContractsComponent();

        setContent(tabs.getComponent());
      }

      @Override
      public void nodeDeselected() {
        setContent(new JPanel());
      }
    });
  }

  private void addBlockTableComponent() {
    var table = new BlockInfoTable(this.project, selectedChain);
    TabInfo blockTab = new TabInfo(new JBScrollPane(table))
        .setText(NeoMessageBundle.message("toolwindow.tabs.blocks"));
    tabs.addTab(blockTab);
  }

  private void addWalletComponent() {
    var wallet = new WalletComponent(project, selectedChain, wallets);
    TabInfo walletTab = new TabInfo(new JBScrollPane(wallet))
        .setText(NeoMessageBundle.message("toolwindow.tabs.wallets"));
    tabs.addTab(walletTab);
  }

  private void addContractsComponent() {
    var contractsComponent = new ContractsComponent(project, selectedChain);
    TabInfo contractTab = new TabInfo(new JBScrollPane(contractsComponent))
        .setText(NeoMessageBundle.message("toolwindow.tabs.contracts"));
    tabs.addTab(contractTab);
  }

  @Override
  public void dispose() {
    tabs = null;
  }
}
