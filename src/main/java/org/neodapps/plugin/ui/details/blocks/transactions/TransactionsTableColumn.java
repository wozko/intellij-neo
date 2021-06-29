/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.blocks.transactions;

/**
 * BlockInfo table columns.
 */
enum TransactionsTableColumn {
  HASH(0, "Hash"),
  SENDER(1, "SENDER"),
  SIZE(2, "Size");


  private final int index;
  private final String name;

  TransactionsTableColumn(int index, String name) {
    this.index = index;
    this.name = name;
  }

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }
}
