/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.toolwindow.topics;

import com.intellij.util.messages.Topic;

/**
 * An event that fires when chain is refreshed.
 */
public interface RefreshChainsNotifier {
  Topic<RefreshChainsNotifier> REFRESH_NODE =
      Topic.create("REFRESH_NODE", RefreshChainsNotifier.class);

  void beforeAction();

  void afterAction();
}
