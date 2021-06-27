/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.models;

/**
 * Represents block chain type.
 */
public enum BlockChainType {
  MAIN("Main Net"),
  TEST("Test Net"),
  PRIVATE("Private");

  private final String name;

  BlockChainType(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
