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
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.http.HttpService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.neodapps.plugin.models.ChainLike;

/**
 * Represents the model of block table.
 */
public class BlockInfoTableModel extends AbstractTableModel implements Disposable {
  final Project project;
  final List<NeoBlock> blocks = new ArrayList<>();
  final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm a");
  final ChainLike selectedChain;
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
   * @param chain   selected chain
   */
  public BlockInfoTableModel(Project project, ChainLike chain) {
    this.project = project;
    this.selectedChain = chain;
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
  public void subscribe() throws URISyntaxException, IOException {
    var node = selectedChain.getSelectedItem();
    var endpoint = node.getEndpoint().toString();
    var neow3j =
        Neow3j.build(new HttpService(String.format("%s:%d", endpoint, node.getRpcPort())));
    var observable = neow3j.subscribeToNewBlocksObservable(true);

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