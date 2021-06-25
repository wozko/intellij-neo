/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.neoexpress.model;

/**
 * Represents different neo-express commands.
 */
public enum ExpressCommands {
  CREATE("create"),
  CONTRACT("contract"),
  RESET("reset"),
  RUN("run"),
  SHOW("show"),
  TRANSFER("transfer"),
  WALLET("wallet");

  private final String text;

  ExpressCommands(String value) {
    this.text = value;
  }


  @Override
  public String toString() {
    return this.text;
  }
}
