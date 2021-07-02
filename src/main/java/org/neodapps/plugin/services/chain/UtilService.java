/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.chain;

import com.intellij.openapi.project.Project;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.http.HttpService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.PrivateChain;

/**
 * A util service for functions that doesn't belong to other service.
 */
public class UtilService {

  private final Project project;

  public UtilService(Project project) {
    this.project = project;
  }

  /**
   * Creates the neow3j instance.
   *
   * @param chain selected chain
   * @return neow3j instance
   */
  public Neow3j getNeow3jInstance(ChainLike chain) {
    var endpoint = chain.getSelectedItem().getUrl();
    Neow3j neow3j;
    if (chain.getType().equals(BlockChainType.PRIVATE)) {
      // set magic number
      Neow3jConfig config = new Neow3jConfig();
      config.setNetworkMagic(((PrivateChain) chain).getConfig().getMagic());
      neow3j = Neow3j.build(new HttpService(endpoint), config);
    } else {
      neow3j = Neow3j.build(new HttpService(endpoint));
    }
    return neow3j;
  }

  /**
   * Gets the magic number of a chain.
   *
   * @param chain chain to get the magic number of
   * @return returns the magic number
   */
  public Long getMagicNumber(ChainLike chain) {
    if (chain.getType().equals(BlockChainType.PRIVATE)) {
      return ((PrivateChain) chain).getConfig().getMagic();
    }
    var neow3j = getNeow3jInstance(chain);
    if (neow3j == null) {
      return 0L;
    }
    try {
      return Integer.toUnsignedLong(
          ByteBuffer.wrap(neow3j.getNetworkMagicNumber()).order(ByteOrder.LITTLE_ENDIAN)
              .getInt());
    } catch (IOException e) {
      NeoNotifier.notifyError(project, e.getMessage());
      return 0L;
    }
  }
}
