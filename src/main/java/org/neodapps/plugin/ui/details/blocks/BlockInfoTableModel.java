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
  final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm a");
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
    return blocks.size();
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
    int index = blocks.size() - rowIndex - 1;

    switch (columnIndex) {
      case 0:
        return blocks.get(index).getIndex();
      case 1:
        return sdf.format(new Timestamp(blocks.get(index).getTime()).getTime());
      case 2:
        return blocks.get(index).getTransactions().size();
      case 3:
        return blocks.get(index).getHash();
      case 4:
        return String.format("%d Bytes", blocks.get(index).getSize());
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
      blocks.add(blockReq.getBlock());
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

  @Override
  public void dispose() {
    disposeObservable();
  }

  public NeoBlock getBlock(int index) {
    return blocks.get(blocks.size() - index - 1);
  }
}