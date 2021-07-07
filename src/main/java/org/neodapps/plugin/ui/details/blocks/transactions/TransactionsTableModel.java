/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.blocks.transactions;

import io.neow3j.protocol.core.response.Transaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.blockchain.express.ExpressConsensusNode;
import org.neodapps.plugin.blockchain.express.ExpressWallet;
import org.neodapps.plugin.blockchain.express.ExpressWalletAccount;

/**
 * Represents the model of transactions table.
 */
public class TransactionsTableModel extends AbstractTableModel {
  final List<Transaction> transactions;
  final Map<String, String> knownAddress;
  private final String[] columnNames = {
      TransactionsTableColumn.HASH.getName(), TransactionsTableColumn.SENDER.getName(),
      TransactionsTableColumn.SIZE.getName()
  };

  /**
   * Create the model of transactions table.
   *
   * @param transactions list of transactions.
   */
  public TransactionsTableModel(List<Transaction> transactions, ChainLike chain) {
    this.transactions = transactions;

    // set known addresses
    knownAddress = new HashMap<>();
    if (chain.getType().equals(BlockChainType.PRIVATE)) {
      var privateChain = (PrivateChain) chain;
      var genesis =
          ((ExpressConsensusNode) privateChain.getSelectedItem()).getWallet().getAccounts().stream()
              .filter(
                  a -> a.getLabel() != null && a.getLabel().equals("Consensus MultiSigContract"))
              .findFirst();
      genesis.ifPresent(expressWalletAccount -> knownAddress
          .put(expressWalletAccount.getScriptHash(), "genesis"));

      for (ExpressWallet wallet : privateChain.getConfig().getWallets()) {
        for (ExpressWalletAccount account : wallet.getAccounts()) {
          knownAddress.put(account.getScriptHash(), wallet.getName());
        }
      }
    }
  }

  @Override
  public int getRowCount() {
    return transactions.size();
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
    switch (columnIndex) {
      case 0:
        return transactions.get(rowIndex).getHash();
      case 1:
        return getOptionalName(transactions.get(rowIndex).getSender());
      case 2:
        return String.format("%d Bytes", transactions.get(rowIndex).getSize());
      default:
        return null;
    }
  }

  public Map<String, String> getKnownAddress() {
    return knownAddress;
  }

  public String getOptionalName(String walletHash) {
    return knownAddress.getOrDefault(walletHash, walletHash);
  }
}