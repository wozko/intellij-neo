/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services;

import com.intellij.openapi.project.Project;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.models.BlockChainType;
import org.neodapps.plugin.models.Chain;
import org.neodapps.plugin.models.ChainLike;
import org.neodapps.plugin.models.NodeRunningState;
import org.neodapps.plugin.models.PrivateChain;

/**
 * Represents block chain util services.
 */
public class BlockchainService {
  private final Project neoProject;

  public BlockchainService(@NotNull Project project) {
    this.neoProject = project;
  }

  /**
   * Returns the status of a node.
   *
   * @param chain chain to check the status
   * @return the status
   */
  public NodeRunningState getNodeStatus(ChainLike chain) {

    if (chain.getType().equals(BlockChainType.PRIVATE)) {
      return getPrivateNodeRunningState((PrivateChain) chain);
    } else {
      return getPublicNodeRunningState((Chain) chain);
    }
  }

  private NodeRunningState getPublicNodeRunningState(Chain chain) {
    String endpoint;
    try {
      endpoint = String.format("%s:%d", chain.getSelectedItem().getEndpoint(),
          chain.getSelectedItem().getRpcPort());
    } catch (URISyntaxException e) {
      NeoNotifier.notifyError(neoProject, e.getMessage());
      return NodeRunningState.NOT_RUNNING;
    }

    try {
      var neow3j = Neow3j.build(new HttpService(endpoint));
      neow3j.getBlockCount().send().getBlockCount();
      return NodeRunningState.RUNNING;
    } catch (IOException e) {
      return NodeRunningState.NOT_RUNNING;
    }

  }

  private NodeRunningState getPrivateNodeRunningState(PrivateChain chain) {
    String endpoint;
    try {
      endpoint = String.format("%s:%d", chain.getSelectedItem().getEndpoint(),
          chain.getSelectedItem().getRpcPort());
    } catch (URISyntaxException e) {
      NeoNotifier.notifyError(neoProject, e.getMessage());
      return NodeRunningState.NOT_RUNNING;
    }

    long magicNumber;
    try {
      // check if running without magic number
      var neow3j = Neow3j.build(new HttpService(endpoint));
      neow3j.getBlockCount().send().getBlockCount();
      magicNumber =
          Integer.toUnsignedLong(
              ByteBuffer.wrap(neow3j.getNetworkMagicNumber()).order(ByteOrder.LITTLE_ENDIAN)
                  .getInt());
    } catch (IOException e) {
      return NodeRunningState.NOT_RUNNING;
    }

    // compare magic number
    if (chain.getConfig().getMagic() == magicNumber) {
      return NodeRunningState.RUNNING;
    }

    return NodeRunningState.NOT_RUNNING;
  }
}

