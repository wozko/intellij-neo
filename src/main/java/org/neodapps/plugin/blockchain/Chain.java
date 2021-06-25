/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.blockchain;

import java.util.List;

/**
 * Represents a Neo blockchain.
 */
public class Chain implements ChainLike {

  private final BlockChainType type;
  private final List<ConsensusNode> nodes;

  public Chain(BlockChainType type, List<ConsensusNode> nodes) {
    this.type = type;
    this.nodes = nodes;
  }

  @Override
  public BlockChainType getType() {
    return this.type;
  }

  @Override
  public List<? extends ConsensusNodeLike> getNodes() {
    return this.nodes;
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
