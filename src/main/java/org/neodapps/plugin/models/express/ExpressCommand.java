/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.models.express;

import com.intellij.util.messages.Topic;
import org.neodapps.plugin.topics.ExpressCommandNotifier;

/**
 * Represents different neo-express commands.
 */
public enum ExpressCommand {
  CREATE("create", ExpressCommandNotifier.CREATE_COMMAND),
  CONTRACT("contract", ExpressCommandNotifier.CONTRACT_COMMAND),
  RESET("reset", ExpressCommandNotifier.RESET_COMMAND),
  RUN("run", ExpressCommandNotifier.RUN_COMMAND),
  SHOW("show", ExpressCommandNotifier.SHOW_COMMAND),
  TRANSFER("transfer", ExpressCommandNotifier.TRANSFER_COMMAND),
  WALLET("wallet", ExpressCommandNotifier.WALLET_COMMAND);

  private final String text;
  private final Topic<ExpressCommandNotifier> topic;

  ExpressCommand(String value, Topic<ExpressCommandNotifier> topic) {
    this.text = value;
    this.topic = topic;
  }

  @Override
  public String toString() {
    return this.text;
  }

  public Topic<ExpressCommandNotifier> getTopic() {
    return topic;
  }
}
