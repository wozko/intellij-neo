/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.chain;

import com.intellij.openapi.project.Project;
import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.SmartContract;
import io.neow3j.contract.Token;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.nep6.NEP6Wallet;
import io.reactivex.Observable;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.Chain;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.ConsensusNodeLike;
import org.neodapps.plugin.blockchain.NodeRunningState;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.blockchain.express.ExpressConsensusNode;

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
   */
  public NodeRunningState checkNodeStatus(ChainLike chain, ConsensusNodeLike node) {
    var state = NodeRunningState.NOT_RUNNING;
    if (chain != null) {
      if (chain.getType().equals(BlockChainType.PRIVATE)) {
        state = getPrivateNodeRunningState((PrivateChain) chain, (ExpressConsensusNode) node);
      } else {
        state = getPublicNodeRunningState((Chain) chain);
      }
    }
    return state;
  }

  /**
   * Deploy a smart contract.
   *
   * @param nefPath        path to nef compiled file
   * @param manifestPath   path to manifest file
   * @param walletToDeploy wallet used to deploy
   * @param chain          chain to deploy
   */
  public void deployContract(String nefPath, String manifestPath, NEP6Wallet walletToDeploy,
                             ChainLike chain) {
    var neow3j = project.getService(UtilService.class).getNeow3jInstance(chain);
    if (neow3j == null) {
      // notified exiting
      return;
    }
    try {
      NefFile nefFile = NefFile.readFromFile(Paths.get(nefPath).toFile());
      ContractManifest manifest = ObjectMapperFactory
          .getObjectMapper()
          .readValue(new FileInputStream(Paths.get(manifestPath).toFile()), ContractManifest.class);

      var wallet = Wallet.fromNEP6Wallet(walletToDeploy);

      // decrypt wallet before deployment
      project.getService(WalletService.class).decryptWalletWithDefaultPassword(wallet);

      new ContractManagement(neow3j)
          .deploy(nefFile, manifest)
          .wallet(wallet)
          .signers(Signer.global(wallet.getAccounts().get(0).getScriptHash()))
          .sign()
          .send();
      NeoNotifier.notifySuccess(project,
          NeoMessageBundle.message("contracts.deploy.success", manifest.getName()));
      // encrypt wallet before deployment
      project.getService(WalletService.class).encryptWalletWithDefaultPassword(wallet);
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
  public Map<NEP6Wallet, List<TokenBalance>> getTokenBalances(
      List<NEP6Wallet> wallets, ChainLike chain) {
    Map<NEP6Wallet, List<TokenBalance>> result = new HashMap<>();

    Neow3j neow3j = project.getService(UtilService.class).getNeow3jInstance(chain);
    if (neow3j == null) {
      // notified, exiting
      return result;
    }
    try {
      for (NEP6Wallet wallet : wallets) {
        var balances = Wallet.fromNEP6Wallet(wallet).getNep17TokenBalances(neow3j);
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

  /**
   * Invokes a contract method.
   *
   * @param chain          selected chain
   * @param contractState  contract to invoke
   * @param methodToInvoke method to invoke
   * @param parameters     method params
   * @param nep6Wallet     wallet to sign
   */
  public void invokeContractMethod(ChainLike chain,
                                   NeoGetContractState.ContractState contractState,
                                   ContractManifest.ContractABI.ContractMethod methodToInvoke,
                                   List<ContractParameter> parameters,
                                   NEP6Wallet nep6Wallet) {
    var neow3j = project.getService(UtilService.class).getNeow3jInstance(chain);
    if (neow3j == null) {
      // notified, exiting
      return;
    }

    var wallet = Wallet.fromNEP6Wallet(nep6Wallet);
    project.getService(WalletService.class).decryptWalletWithDefaultPassword(wallet);

    try {
      new SmartContract(contractState.getHash(), neow3j)
          .invokeFunction(methodToInvoke.getName(), parameters.toArray(ContractParameter[]::new))
          .wallet(wallet)
          .signers(Signer.calledByEntry(wallet.getAccounts().get(0)))
          .sign()
          .send().getRawResponse();
      NeoNotifier.notifySuccess(project, NeoMessageBundle.message("contracts.invoke.submitted"));
    } catch (Throwable throwable) {
      NeoNotifier.notifyError(project, throwable.getMessage());
    }
  }

  /**
   * Subscribes and listen to blocks.
   *
   * @param chainLike chain to subscribe
   */
  public Observable<NeoGetBlock> subscribeToBlocks(ChainLike chainLike) {
    var neow3j = project.getService(UtilService.class).getNeow3jInstance(chainLike);
    if (neow3j == null) {
      // notified, exiting
      return null;
    }

    BigInteger blockCount;
    try {
      blockCount = neow3j.getBlockCount().send().getBlockCount();
    } catch (IOException e) {
      NeoNotifier.notifyError(project, e.getMessage());
      return null;
    }

    return neow3j
        .catchUpToLatestAndSubscribeToNewBlocksObservable(
            new BigInteger("0").max(blockCount.subtract(new BigInteger("10"))),
            true);

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

  private NodeRunningState getPrivateNodeRunningState(PrivateChain chain,
                                                      ExpressConsensusNode node) {
    long magicNumber;
    try {
      // check if running without magic number
      var neow3j = Neow3j.build(new HttpService(node.getUrl()));
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

