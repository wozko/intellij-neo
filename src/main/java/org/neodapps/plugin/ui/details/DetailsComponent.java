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
import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.NodeRunningState;
import org.neodapps.plugin.services.chain.BlockchainService;
import org.neodapps.plugin.topics.NodeChangeNotifier;
import org.neodapps.plugin.ui.details.blocks.BlockInfoTable;
import org.neodapps.plugin.ui.details.contracts.ContractsComponent;
import org.neodapps.plugin.ui.details.wallets.WalletComponent;

/**
 * Represents the component with status and tabs.
 */
public class DetailsComponent extends JPanel implements Disposable {

  private final Project project;
  private ChainLike selectedChain;
  private Wrapper statusWrapper;
  private Wrapper tabsWrapper;

  /**
   * Creates the component with status and tabs.
   */
  public DetailsComponent(Project project) {
    this.project = project;
    this.statusWrapper = new Wrapper();
    this.tabsWrapper = new Wrapper();
    setLayout(new BorderLayout());
    add(this.statusWrapper, BorderLayout.NORTH);
    add(this.tabsWrapper, BorderLayout.CENTER);

    var bus = project.getMessageBus();
    var thisRef = this;

    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, new NodeChangeNotifier() {
      @Override
      public void nodeSelected(ChainLike selectedChain) {

        var blockchainService = project.getService(BlockchainService.class);
        var status = blockchainService.getNodeStatus(selectedChain);
        // add status content
        statusWrapper.setContent(new SelectedNodeStateComponent(project, selectedChain, status));

        if (status.equals(NodeRunningState.RUNNING)) {
          // add tabs
          var tabs = new JBTabsImpl(project);
          thisRef.selectedChain = selectedChain;
          addBlockTableComponent(tabs);
          addWalletComponent(tabs);
          addContractsComponent(tabs);
          tabsWrapper.setContent(tabs);
        }
        
      }

      @Override
      public void nodeDeselected() {
        // remove details component
        tabsWrapper.setContent(new JPanel());

        // add status component
        statusWrapper.setContent(new SelectedNodeStateComponent(project, null, null));
      }
    });

    statusWrapper.setContent(new SelectedNodeStateComponent(project, null, null));
  }

  private void addBlockTableComponent(JBTabs tabs) {
    var table = new BlockInfoTable(this.project, selectedChain);
    TabInfo blockTab = new TabInfo(new JBScrollPane(table))
        .setText(NeoMessageBundle.message("toolwindow.tabs.blocks"));
    tabs.addTab(blockTab);
  }

  private void addWalletComponent(JBTabs tabs) {
    var wallet = new WalletComponent(project, selectedChain);
    TabInfo walletTab = new TabInfo(new JBScrollPane(wallet))
        .setText(NeoMessageBundle.message("toolwindow.tabs.wallets"));
    tabs.addTab(walletTab);
  }

  private void addContractsComponent(JBTabs tabs) {
    var contractsComponent = new ContractsComponent(project, selectedChain);
    TabInfo contractTab = new TabInfo(new JBScrollPane(contractsComponent))
        .setText(NeoMessageBundle.message("toolwindow.tabs.contracts"));
    tabs.addTab(contractTab);
  }

  @Override
  public void dispose() {
    statusWrapper = null;
    tabsWrapper = null;
  }
}
