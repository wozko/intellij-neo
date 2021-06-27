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

/**
 * Represents and Account in neo-express config file.
 */
public class ExpressWalletAccount {

  @SerializedName("private-key")
  private String privateKey;

  @SerializedName("script-hash")
  private String scriptHash;

  @SerializedName("label")
  private Object label;

  @SerializedName("is-default")
  private Boolean isDefault;

  @SerializedName("contract")
  private ExpressAccountContract contract;

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getScriptHash() {
    return scriptHash;
  }

  public void setScriptHash(String scriptHash) {
    this.scriptHash = scriptHash;
  }

  public Object getLabel() {
    return label;
  }

  public void setLabel(Object label) {
    this.label = label;
  }

  public Boolean getDefault() {
    return isDefault;
  }

  public void setDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }

  public ExpressAccountContract getContract() {
    return contract;
  }

  public void setContract(ExpressAccountContract contract) {
    this.contract = contract;
  }
}
