/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.topics;

import com.intellij.util.messages.Topic;
import org.neodapps.plugin.models.ChainLike;
import org.neodapps.plugin.models.NodeRunningState;

/**
 * Notifies on status changes.
 */
public interface NodeStatusNotifier {
  Topic<NodeStatusNotifier> STATUS_CHANGED =
      Topic.create("STATUS_CHANGED", NodeStatusNotifier.class);

  void afterAction(NodeRunningState runningState, ChainLike chain);
}
