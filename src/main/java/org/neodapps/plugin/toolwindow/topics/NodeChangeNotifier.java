/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.toolwindow.topics;

import com.intellij.util.messages.Topic;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.ConsensusNodeLike;

/**
 * An event fires when a node is changed.
 */
public interface NodeChangeNotifier {
  Topic<NodeChangeNotifier> NODE_CHANGE =
      Topic.create("NODE_CHANGED", NodeChangeNotifier.class);

  void beforeAction();

  void afterAction(ChainLike chain, ConsensusNodeLike node);
}
