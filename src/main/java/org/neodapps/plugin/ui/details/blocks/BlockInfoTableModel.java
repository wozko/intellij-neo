/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.blocks;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.http.HttpService;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.neodapps.plugin.blockchain.ChainLike;

/**
 * Represents the model of block table.
 */
public class BlockInfoTableModel extends AbstractTableModel implements Disposable {
  final Project project;
  final List<NeoBlock> blocks = new ArrayList<>();
  final List<NeoBlock> nonEmptyBlocks = new ArrayList<>();
  final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm a");
  private boolean hideEmptyBlocks;
  private final String[] columnNames = {
      BlockInfoTableColumn.INDEX.getName(), BlockInfoTableColumn.TIME.getName(),
      BlockInfoTableColumn.TRANSACTIONS.getName(), BlockInfoTableColumn.HASH.getName(),
      BlockInfoTableColumn.SIZE.getName()
  };
  io.reactivex.disposables.Disposable disposableRxJx;

  /**
   * Create the model of block table.
   *
   * @param project intellij project
   */
  public BlockInfoTableModel(Project project) {
    this.project = project;
  }

  @Override
  public int getRowCount() {
    return hideEmptyBlocks ? nonEmptyBlocks.size() : blocks.size();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    var blockList = hideEmptyBlocks ? nonEmptyBlocks : blocks;
    int index = blockList.size() - rowIndex - 1;

    switch (columnIndex) {
      case 0:
        return blockList.get(index).getIndex();
      case 1:
        return sdf.format(new Timestamp(blockList.get(index).getTime()).getTime());
      case 2:
        return blockList.get(index).getTransactions().size();
      case 3:
        return blockList.get(index).getHash();
      case 4:
        return String.format("%d Bytes", blockList.get(index).getSize());
      default:
        return null;
    }
  }

  /**
   * Subscribe table model to latest blocks.
   *
   * @throws URISyntaxException thrown when url is in an invalid format.
   */
  public void subscribe(ChainLike selectedChain) throws URISyntaxException, IOException {
    var neow3j =
        Neow3j.build(new HttpService(selectedChain.getSelectedItem().getUrl()));
    var blockCount = neow3j.getBlockCount().send().getBlockCount();
    var observable = neow3j
        .catchUpToLatestAndSubscribeToNewBlocksObservable(
            new BigInteger("1").max(blockCount.subtract(new BigInteger("10"))),
            true);

    disposableRxJx = observable.subscribe(blockReq -> {
      var block = blockReq.getBlock();
      blocks.add(block);
      if (block.getTransactions().size() > 0) {
        nonEmptyBlocks.add(block);
      }
    });
  }

  /**
   * Used to dispose the rxjs when no longer needed.
   */
  public void disposeObservable() {
    if (disposableRxJx != null && !disposableRxJx.isDisposed()) {
      disposableRxJx.dispose();
    }
  }

  public void hideEmptyBlocks(boolean hide) {
    this.hideEmptyBlocks = hide;
  }

  @Override
  public void dispose() {
    disposeObservable();
  }

  public NeoBlock getBlock(int index) {
    return blocks.get(blocks.size() - index - 1);
  }
}