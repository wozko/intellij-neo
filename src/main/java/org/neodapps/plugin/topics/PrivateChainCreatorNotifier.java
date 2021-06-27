/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.topics;

import com.intellij.util.messages.Topic;

/**
 * Represents events fired when a new private chain is created.
 */
public interface PrivateChainCreatorNotifier {

  // user adds a new private net
  Topic<PrivateChainCreatorNotifier> NEW_PRIVATE_NET_CREATED =
      Topic.create("NEW_PRIVATE_NET_CREATED", PrivateChainCreatorNotifier.class);

  void privateNetCreated(String privateNetName);
}
