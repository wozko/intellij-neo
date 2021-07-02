/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.blockchain;

/**
 * Represents a consensus node in a public chain.
 */
public class ConsensusNode implements ConsensusNodeLike {
  private final String url;

  public ConsensusNode(String endpoint, Integer rpcPort) {
    this.url = String.format("%s:%d", endpoint, rpcPort);
  }

  @Override
  public String toString() {
    return this.url;
  }

  @Override
  public String getUrl() {
    return this.url;
  }
}