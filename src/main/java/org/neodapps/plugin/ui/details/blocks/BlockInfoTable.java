/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.blocks;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import io.neow3j.protocol.core.response.NeoBlock;
import java.awt.Cursor;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.ListSelectionModel;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.topics.NodeChangeNotifier;

/**
 * Represents the block info table.
 */
public class BlockInfoTable extends JBTable implements Disposable {
  final Project project;

  /**
   * Creates the block info table.
   *
   * @param project         intellij project
   * @param hideEmptyBlocks if empty blocks should be filtered out
   */
  public BlockInfoTable(Project project, ChainLike selectedChain, boolean hideEmptyBlocks) {
    super(new BlockInfoTableModel(project));
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

    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, new NodeChangeNotifier() {
      @Override
      public void nodeSelected(ChainLike selectedChain) {
        unSubscribeFromBlocks();
      }

      @Override
      public void nodeDeselected() {
        unSubscribeFromBlocks();
      }
    });

    subscribeToBlocks(selectedChain, hideEmptyBlocks);
  }

  /**
   * Subscribe to latest blocks.
   */
  public void subscribeToBlocks(ChainLike selectedChain, boolean hideEmptyBlocks) {
    if (selectedChain == null) {
      return;
    }
    try {
      ((BlockInfoTableModel) getModel()).subscribe(selectedChain, hideEmptyBlocks);
    } catch (URISyntaxException | IOException e) {
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