/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.blocks;

/**
 * BlockInfo table columns.
 */
enum BlockInfoTableColumn {
  INDEX(1, "Index"),
  TIME(2, "Time"),
  TRANSACTIONS(3, "Transactions"),
  HASH(4, "Hash"),
  SIZE(5, "Size");

  private final int index;
  private final String name;

  BlockInfoTableColumn(int index, String name) {
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
