/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.topics;

import com.intellij.util.messages.Topic;
import org.neodapps.plugin.blockchain.ChainLike;

/**
 * An event fires when a selected node is changed.
 */
public interface NodeChangeNotifier {
  Topic<NodeChangeNotifier> NODE_CHANGE =
      Topic.create("NODE_CHANGED", NodeChangeNotifier.class);

  void nodeSelected(ChainLike selectedChain);

  void nodeDeselected();
}
