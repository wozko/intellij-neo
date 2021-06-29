/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.blockchain.express;

/**
 * Represents different neo-express commands.
 */
public enum ExpressCommand {
  CREATE("create"),
  CONTRACT("contract"),
  RESET("reset"),
  RUN("run"),
  SHOW("show"),
  TRANSFER("transfer"),
  WALLET("wallet");

  private final String text;

  ExpressCommand(String value) {
    this.text = value;
  }

  @Override
  public String toString() {
    return this.text;
  }
}
