/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.models.BlockChainType;
import org.neodapps.plugin.models.PrivateChain;
import org.neodapps.plugin.services.ChainListService;
import org.neodapps.plugin.topics.NodeChangeNotifier;
import org.neodapps.plugin.topics.RefreshActionNotifier;
import org.neodapps.plugin.ui.window.blocks.BlockInfoTable;
import org.neodapps.plugin.ui.window.wallets.WalletComponent;

/**
 * Represents the content shown in data component.
 */
public class DataComponent extends Wrapper {
  private final Project project;

  /**
   * Creates the content shown in data component.
   *
   * @param project intellij project
   */
  public DataComponent(@NotNull Project project) {
    this.project = project;

    var bus = project.getMessageBus();

    // node change
    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, (chainLike) -> {
      setContent(getContent());
    });

    // refresh
    bus.connect().subscribe(RefreshActionNotifier.REFRESH, () -> {
      setContent(getContent());
    });

  }

  private JComponent getContent() {
    var selectedChain = project.getService(ChainListService.class).getSelectedValue();
    if (selectedChain == null) {
      return new JPanel();
    }

    var blockListTable = new BlockInfoTable(project, selectedChain);
    JBTabs tabs = new JBTabsImpl(project);

    // block list tab
    TabInfo blockTab = new TabInfo(new JBScrollPane(blockListTable))
        .setText(NeoMessageBundle.message("toolwindow.tabs.blocks"));
    tabs.addTab(blockTab);

    if (selectedChain.getType().equals(BlockChainType.PRIVATE)) {
      // wallets tab
      TabInfo walletsTab =
          new TabInfo(new JBScrollPane(new WalletComponent((PrivateChain) selectedChain)))
              .setText(NeoMessageBundle.message("toolwindow.tabs.wallets"));
      tabs.addTab(walletsTab);
    }
    return tabs.getComponent();
  }

}
