/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.chain;

import com.intellij.openapi.project.Project;
import io.neow3j.contract.FungibleToken;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoGetNep17Balances;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.types.Hash160;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.Chain;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.NodeRunningState;
import org.neodapps.plugin.blockchain.PrivateChain;

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
    if (chain == null) {
      return NodeRunningState.NOT_RUNNING;
    }
    if (chain.getType().equals(BlockChainType.PRIVATE)) {
      return getPrivateNodeRunningState((PrivateChain) chain);
    } else {
      return getPublicNodeRunningState((Chain) chain);
    }
  }

  /**
   * Returns the Nep17 balances of list of addresses with asset symbol.
   *
   * @param addresses addresses to check balances of
   * @param chain     selected chain
   * @return a map of token
   */
  public Map<String, List<AssetBalance>> getAssetBalances(
      Set<String> addresses,
      ChainLike chain) {
    Map<String, List<AssetBalance>> result = new HashMap<>();
    try {
      var node = chain.getSelectedItem();
      var neow3j = Neow3j
          .build(new HttpService(String.format("%s:%d", node.getEndpoint(), node.getRpcPort())));

      for (String address : addresses) {
        var balances = neow3j.getNep17Balances(Hash160.fromAddress(address)).send().getBalances();
        List<AssetBalance> balanceList = new ArrayList<>();
        for (NeoGetNep17Balances.Nep17Balance balance : balances.getBalances()) {
          var asset = balance.getAssetHash();
          var token = new FungibleToken(asset, neow3j);
          var decimals = token.getDecimals();
          var balanceWithDecimals = NumberUtils.toDouble(balance.getAmount(), 0);
          double balanceWithoutDecimals = 0;

          if (balanceWithDecimals != 0) {
            balanceWithoutDecimals = balanceWithDecimals / Math.pow(10, decimals);
          }

          balanceList.add(new AssetBalance(token.getSymbol(), asset.toAddress(), decimals,
              balanceWithoutDecimals));
        }
        result.put(address, balanceList);
      }
    } catch (URISyntaxException | IOException e) {
      NeoNotifier.notifyError(neoProject, e.getMessage());
    }
    return result;
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

