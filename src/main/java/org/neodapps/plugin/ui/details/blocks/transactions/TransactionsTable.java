/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.blocks.transactions;

import io.neow3j.protocol.core.response.Transaction;
import java.awt.Cursor;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import org.neodapps.plugin.blockchain.ChainLike;

/**
 * Represents transactions table.
 */
public class TransactionsTable extends JTable {

  List<Transaction> transactions;


  /**
   * Creates transactions table.
   *
   * @param transactions transactions list
   */
  public TransactionsTable(List<Transaction> transactions, ChainLike chain) {
    this.transactions = transactions;
    setRowHeight(40);
    setModel(new TransactionsTableModel(transactions, chain));
    // set cursor
    setCursor(new Cursor(Cursor.HAND_CURSOR));

    // single selection allowed
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ListSelectionModel selectionModel = getSelectionModel();
    var knownAddresses = ((TransactionsTableModel) getModel()).getKnownAddress();
    // show a popup with more details when a row is selected
    selectionModel.addListSelectionListener(e -> {
      int selectedRow = getSelectedRow();
      if (e.getValueIsAdjusting()) {
        return;
      }
      if (selectedRow == -1) {
        return;
      }
      var popup =
          new TransactionInfoPopup(knownAddresses, transactions.get(selectedRow));
      popup.showPopup();
      // clear selection
      selectionModel.clearSelection();
    });
  }
}
