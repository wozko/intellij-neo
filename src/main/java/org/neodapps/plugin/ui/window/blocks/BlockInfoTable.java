/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window.blocks;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import io.neow3j.protocol.core.response.NeoBlock;
import java.awt.Cursor;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.ListSelectionModel;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.models.BlockChainType;
import org.neodapps.plugin.models.ChainLike;
import org.neodapps.plugin.models.NodeRunningState;
import org.neodapps.plugin.services.BlockchainService;
import org.neodapps.plugin.topics.NodeChangeNotifier;
import org.neodapps.plugin.topics.NodeStatusNotifier;
import org.neodapps.plugin.topics.RefreshActionNotifier;

/**
 * Represents the block info table.
 */
public class BlockInfoTable extends JBTable implements Disposable {
  final Project project;

  /**
   * Creates the block info table.
   *
   * @param project       intellij project
   * @param selectedChain selected chain to query blocks
   */
  public BlockInfoTable(Project project, ChainLike selectedChain) {
    super(new BlockInfoTableModel(project, selectedChain));
    this.project = project;

    // set cursor
    setCursor(new Cursor(Cursor.HAND_CURSOR));

    // single selection allowed
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ListSelectionModel selectionModel = getSelectionModel();
    // show a popup with more details when a row is selected
    selectionModel.addListSelectionListener(e -> {
      int selectedRow = getSelectedRow();
      if (e.getValueIsAdjusting()) {
        return;
      }
      if (selectedRow == -1) {
        return;
      }
      NeoBlock block = ((BlockInfoTableModel) getModel()).getBlock(selectedRow);
      new BlockItemPopup(block, selectedChain).showPopup();

      // clear selection
      selectionModel.clearSelection();
    });

    var bus = project.getMessageBus();

    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, (startedChain) -> {
      // unsubscribe existing subscriptions
      unSubscribeFromBlocks();
    });

    // unsubscribe existing subscriptions
    bus.connect().subscribe(RefreshActionNotifier.REFRESH, this::unSubscribeFromBlocks);

    subscribeToBlocks(selectedChain);
  }

  /**
   * Subscribe to latest blocks.
   */
  public void subscribeToBlocks(ChainLike selectedChain) {
    var publisher = project.getMessageBus()
        .syncPublisher(NodeStatusNotifier.STATUS_CHANGED);
    try {
      if (selectedChain.getType() != BlockChainType.PRIVATE) {
        // if public directly subscribe
        ((BlockInfoTableModel) getModel()).subscribe();
        publisher.afterAction(NodeRunningState.RUNNING, selectedChain);
      } else {
        // if private, check magic number beforehand
        var status = project.getService(BlockchainService.class)
            .getNodeStatus(selectedChain);
        if (status == NodeRunningState.RUNNING) {
          ((BlockInfoTableModel) getModel()).subscribe();
        }
        publisher.afterAction(status, selectedChain);
      }
    } catch (URISyntaxException | IOException e) {
      publisher.afterAction(NodeRunningState.NOT_RUNNING, selectedChain);
      NeoNotifier.notifyError(project, e.getMessage());
    }
  }

  public void unSubscribeFromBlocks() {
    ((BlockInfoTableModel) getModel()).disposeObservable();
  }

  @Override
  public void dispose() {
    unSubscribeFromBlocks();
  }

}