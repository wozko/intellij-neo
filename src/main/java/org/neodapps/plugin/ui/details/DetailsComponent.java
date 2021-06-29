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
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.topics.NodeChangeNotifier;
import org.neodapps.plugin.ui.details.blocks.BlockInfoTable;

/**
 * Represents the component with status and tabs.
 */
public class DetailsComponent extends Wrapper implements Disposable {

  private final Project project;
  private JBTabs tabs;

  /**
   * Creates the component with status and tabs.
   */
  public DetailsComponent(Project project) {
    this.project = project;
    var bus = project.getMessageBus();
    setContent(new JPanel());
    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, new NodeChangeNotifier() {
      @Override
      public void nodeSelected(ChainLike selectedChain) {
        tabs = new JBTabsImpl(project);
        addBlockTableComponent(selectedChain);

        // private chain
        if (selectedChain.getType().equals(BlockChainType.PRIVATE)) {
          addWalletComponent(selectedChain);
        }

        setContent(tabs.getComponent());
      }

      @Override
      public void nodeDeselected() {
        setContent(new JPanel());
      }
    });
  }

  private void addBlockTableComponent(ChainLike selectedChain) {
    var table = new BlockInfoTable(this.project, selectedChain);
    TabInfo blockTab = new TabInfo(new JBScrollPane(table))
        .setText(NeoMessageBundle.message("toolwindow.tabs.blocks"));
    tabs.addTab(blockTab);
  }

  private void addWalletComponent(ChainLike selectedChain) {
    var wallet = new WalletComponent((PrivateChain) selectedChain);
    TabInfo walletTab = new TabInfo(new JBScrollPane(wallet))
        .setText(NeoMessageBundle.message("toolwindow.tabs.wallets"));
    tabs.addTab(walletTab);
  }

  @Override
  public void dispose() {
    tabs = null;
  }
}
