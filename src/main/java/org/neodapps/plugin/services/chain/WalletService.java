/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.chain;

import com.intellij.openapi.project.Project;
import io.neow3j.crypto.WIF;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.blockchain.express.ExpressWallet;
import org.neodapps.plugin.blockchain.express.ExpressWalletAccount;

/**
 * Represents various wallet utility services.
 */
public class WalletService {
  private final Project project;

  // a list of wallets maintained throughout project session
  private final Map<Long, List<Wallet>> importedWallets;

  public WalletService(@NotNull Project project) {
    this.project = project;
    this.importedWallets = new HashMap<>();
  }

  /**
   * Gets the wallets for a chain.
   *
   * @param chain selected chain
   * @return returns a list of wallets
   */
  public List<Wallet> getWallets(ChainLike chain) {
    if (chain.getType().equals(BlockChainType.PRIVATE)) {
      return getWalletForPrivateChain((PrivateChain) chain);
    } else {
      return getImportedWallets((Chain) chain);
    }
  }

  /**
   * Import a nep6file as a wallet.
   *
   * @param nep6File path to nep6 file.
   * @param chain    selected chain (testnet/mainnet)
   */
  public void addImportedWallet(String name, Path nep6File, Chain chain) {
    try {
      var wallet = Wallet.fromNEP6Wallet(nep6File.toFile())
          .name(name);
      var wallets = getWallets(chain);
      wallets.add(wallet);
    } catch (IOException e) {
      NeoNotifier.notifyError(project, e.getMessage());
    }
  }

  private List<Wallet> getWalletForPrivateChain(PrivateChain chain) {
    var wallets = new ArrayList<Wallet>();
    for (ExpressWallet expressWallet : chain.getConfig().getWallets()) {
      wallets.add(getNeo3jWallet(expressWallet));
    }
    return wallets;
  }

  private List<Wallet> getImportedWallets(Chain chain) {
    var magicNumber = project.getService(UtilService.class).getMagicNumber(chain);
    return importedWallets.computeIfAbsent(magicNumber, k -> new ArrayList<>());
  }

  /**
   * Creates neo3j wallet account from express wallet account.
   *
   * @param expressWalletAccount express wallet account
   * @return neo3j wallet account
   */
  private Account getNeo3jWalletAccount(ExpressWalletAccount expressWalletAccount) {
    return Account.fromWIF(WIF.getWIFFromPrivateKey(expressWalletAccount.getPrivateKey().getBytes(
        StandardCharsets.UTF_8)));

  }

  /**
   * Creates neo3j wallet from express wallet.
   *
   * @param expressWallet express wallet
   * @return neo3j wallet
   */
  private Wallet getNeo3jWallet(ExpressWallet expressWallet) {
    var expressAccounts = expressWallet.getAccounts();
    var neo3jAccounts = new Account[expressAccounts.size()];
    for (int i = 0; i < expressAccounts.size(); i++) {
      neo3jAccounts[i] = getNeo3jWalletAccount(expressAccounts.get(i));
    }
    return Wallet.withAccounts(neo3jAccounts).name(expressWallet.getName());
  }
}
