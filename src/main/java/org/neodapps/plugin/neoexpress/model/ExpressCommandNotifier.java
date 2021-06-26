/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.neoexpress.model;

import com.intellij.util.messages.Topic;

/**
 * Represents topics for express commands.
 */
public interface ExpressCommandNotifier {
  Topic<ExpressCommandNotifier> CREATE_COMMAND =
      Topic.create("CREATE_COMMAND", ExpressCommandNotifier.class);
  Topic<ExpressCommandNotifier> CONTRACT_COMMAND =
      Topic.create("CONTRACT_COMMAND", ExpressCommandNotifier.class);
  Topic<ExpressCommandNotifier> RESET_COMMAND =
      Topic.create("RESENT_COMMAND", ExpressCommandNotifier.class);
  Topic<ExpressCommandNotifier> RUN_COMMAND =
      Topic.create("RUN_COMMAND", ExpressCommandNotifier.class);
  Topic<ExpressCommandNotifier> SHOW_COMMAND =
      Topic.create("SHOW_COMMAND", ExpressCommandNotifier.class);
  Topic<ExpressCommandNotifier> TRANSFER_COMMAND =
      Topic.create("TRANSFER_COMMAND", ExpressCommandNotifier.class);
  Topic<ExpressCommandNotifier> WALLET_COMMAND =
      Topic.create("TRANSFER_COMMAND", ExpressCommandNotifier.class);

  Topic<ExpressCommandNotifier> ANY_COMMAND =
      Topic.create("ANY_COMMAND", ExpressCommandNotifier.class);

  void afterCompletion(ExpressCommand command);
}
