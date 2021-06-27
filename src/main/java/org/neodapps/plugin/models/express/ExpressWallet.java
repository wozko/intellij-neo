/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.models.express;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a wallet in neo-express config file.
 */
public class ExpressWallet {

  @SerializedName("name")
  private String name;

  @SerializedName("accounts")
  private List<ExpressWalletAccount> accounts;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ExpressWalletAccount> getAccounts() {
    return accounts;
  }

  public void setAccounts(List<ExpressWalletAccount> accounts) {
    this.accounts = accounts;
  }

}
