/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window.blocks.transactions;

import io.neow3j.protocol.core.response.Transaction;
import java.awt.Cursor;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import org.neodapps.plugin.models.ChainLike;

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

    setModel(new TransactionsTableModel(transactions, chain));
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
      System.out.println(selectedRow);
      // clear selection
      selectionModel.clearSelection();
    });
  }
}
