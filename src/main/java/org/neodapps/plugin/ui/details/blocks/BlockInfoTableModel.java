/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.blocks;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.reactivex.Observable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

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
    var block = blocks.get(rowIndex);
    switch (columnIndex) {
      case 0:
        return block.getIndex();
      case 1:
        return sdf.format(new Timestamp(block.getTime()).getTime());
      case 2:
        return block.getTransactions().size();
      case 3:
        return block.getHash();
      case 4:
        return String.format("%d Bytes", block.getSize());
      default:
        return null;
    }
  }

  /**
   * Subscribe table model to latest blocks.
   *
   * @param hideEmptyBlocks filter empty blocks
   */
  public void subscribe(Observable<NeoGetBlock> observable, Boolean hideEmptyBlocks) {

    disposableRxJx = observable.subscribe(blockReq -> {
      var block = blockReq.getBlock();
      if (hideEmptyBlocks) {
        if (block.getTransactions().size() > 0) {
          blocks.add(0, block);
        }
      } else {
        blocks.add(0, block);
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

  @Override
  public void dispose() {
    disposeObservable();
  }

  public NeoBlock getBlock(int index) {
    return blocks.get(index);
  }
}