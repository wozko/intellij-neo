/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.chain;

import static java.util.Collections.emptyList;

import com.intellij.openapi.project.Project;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.core.response.ExpressContractState;
import io.neow3j.protocol.http.HttpService;
import java.io.IOException;
import java.util.List;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.PrivateChain;

/**
 * Represents various contract utility functions.
 */
public class ContractServices {
  private final Project project;

  public ContractServices(Project project) {
    this.project = project;
  }

  /**
   * Gets a list of contracts deployed in the chain.
   * If the selected chain is private net, the contract is taken from neo-express.
   * Else only native contracts are returned.
   *
   * @param chain selected chain.
   * @return returns a list of contracts
   */
  public List<ExpressContractState> getContracts(ChainLike chain) {
    if (chain.getType().equals(BlockChainType.PRIVATE)) {
      return getContractFromExpressRpc((PrivateChain) chain);
    } else {
      // return only native contracts
      // todo: keep track of deployed contracts
      return emptyList();
    }
  }

  /**
   * Returns the native contract list.
   *
   * @param chain selected chain
   * @return native contract list
   */
  public List<? extends ExpressContractState> getNativeContracts(ChainLike chain) {
    var neow3j = project.getService(UtilService.class).getNeow3jInstance(chain);
    try {
      return neow3j.getNativeContracts().send().getNativeContracts();
    } catch (IOException e) {
      NeoNotifier.notifyError(project, e.getMessage());
      return emptyList();
    }
  }

  private List<ExpressContractState> getContractFromExpressRpc(PrivateChain chain) {
    var neow3j = Neow3jExpress.build(new HttpService(chain.getSelectedItem().getUrl()));
    List<ExpressContractState> contracts;
    try {
      contracts = neow3j.expressListContracts().send().getContracts();
    } catch (IOException e) {
      NeoNotifier.notifyError(project, e.getMessage());
      return emptyList();
    }
    return contracts;
  }
}
