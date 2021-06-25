/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.blockchain;

import java.util.List;

/**
 * An interface that represents public or private chain.
 */
public interface ChainLike {

  BlockChainType getType();

  List<? extends ConsensusNodeLike> getNodes();
}
