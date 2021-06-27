/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.topics;

import com.intellij.util.messages.Topic;

/**
 * Represents a refresh action event.
 */
public interface RefreshActionNotifier {
  Topic<RefreshActionNotifier> REFRESH = Topic.create("REFRESH", RefreshActionNotifier.class);

  void refreshActionCalled();
}
