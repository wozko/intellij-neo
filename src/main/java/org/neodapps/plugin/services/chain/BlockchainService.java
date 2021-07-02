/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.chain;

import com.intellij.openapi.project.Project;
import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.Token;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Wallet;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final Project project;

  public BlockchainService(@NotNull Project project) {
    this.project = project;
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
   * Deploy a smart contract.
   *
   * @param nefPath        path to nef compiled file
   * @param manifestPath   path to manifest file
   * @param walletToDeploy wallet used to deploy
   * @param chain          chain to deploy
   */
  public void deployContract(Path nefPath, Path manifestPath, Wallet walletToDeploy,
                             ChainLike chain) {
    var neow3j = project.getService(UtilService.class).getNeow3jInstance(chain);
    if (neow3j == null) {
      // notified exiting
      return;
    }
    try {
      NefFile nefFile = NefFile.readFromFile(nefPath.toFile());
      ContractManifest manifest = ObjectMapperFactory
          .getObjectMapper()
          .readValue(new FileInputStream(manifestPath.toFile()), ContractManifest.class);

      var response = new ContractManagement(neow3j)
          .deploy(nefFile, manifest)
          .wallet(walletToDeploy)
          .signers(Signer.global(walletToDeploy.getAccounts().get(0).getScriptHash()))
          .sign()
          .send().getRawResponse();
      NeoNotifier.notifySuccess(project, response);
    } catch (Throwable e) {
      NeoNotifier.notifyError(project, e.getMessage());
    }
  }

  /**
   * Returns the Nep17 balances of list of addresses with asset symbol.
   *
   * @param chain selected chain
   * @return a map of token
   */
  public Map<Wallet, List<TokenBalance>> getTokenBalances(
      List<Wallet> wallets, ChainLike chain) {
    Map<Wallet, List<TokenBalance>> result = new HashMap<>();

    Neow3j neow3j = project.getService(UtilService.class).getNeow3jInstance(chain);
    if (neow3j == null) {
      // notified, exiting
      return result;
    }
    try {
      for (Wallet wallet : wallets) {
        var balances = wallet.getNep17TokenBalances(neow3j);
        List<TokenBalance> balanceList = new ArrayList<>();
        for (Hash160 hash160 : balances.keySet()) {
          var balance = balances.get(hash160);
          var token = new Token(hash160, neow3j);
          var tokenBalance = new TokenBalance(hash160, token.getTotalSupply(), token.getDecimals(),
              token.getSymbol(), balance);
          balanceList.add(tokenBalance);
        }
        result.put(wallet, balanceList);
      }
    } catch (IOException e) {
      NeoNotifier.notifyError(project, e.getMessage());
    }
    return result;
  }


  private NodeRunningState getPublicNodeRunningState(Chain chain) {
    var neow3j = project.getService(UtilService.class).getNeow3jInstance(chain);
    if (neow3j == null) {
      return NodeRunningState.NOT_RUNNING;
    }

    try {
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
      return NodeRunningState.NOT_RUNNING;
    }

    long magicNumber;
    try {
      // check if running without magic number
      var neow3j = Neow3j.build(new HttpService(endpoint));
      neow3j.getBlockCount().send().getBlockCount();

      // get the magic number of running instance
      // do not use the util.getmagicnumber function here
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

