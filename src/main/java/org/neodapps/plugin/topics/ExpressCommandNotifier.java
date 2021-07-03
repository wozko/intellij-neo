/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.topics;

import com.intellij.util.messages.Topic;
import java.util.UUID;

/**
 * Represents topics for express commands.
 */
public interface ExpressCommandNotifier {
  Topic<ExpressCommandNotifier> RUNNER =
      Topic.create("RUNNER", ExpressCommandNotifier.class);

  void afterCompletion(UUID id);
}
