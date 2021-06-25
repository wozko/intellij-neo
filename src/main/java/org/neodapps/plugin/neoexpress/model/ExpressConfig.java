/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.neoexpress.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import org.neodapps.plugin.blockchain.ConsensusNodeLike;

/**
 * Represents .neo-express config file.
 */
public class ExpressConfig {

  @SerializedName("magic")
  private Long magic;

  @SerializedName("address-version")
  private Byte addressVersion;

  @SerializedName("consensus-nodes")
  private List<ExpressConsensusNode> consensusNodes;

  @SerializedName("wallets")
  private List<ExpressWallet> wallets;

  @SerializedName("settings")
  private Map<String, String> settings;

  public Long getMagic() {
    return magic;
  }

  public void setMagic(Long magic) {
    this.magic = magic;
  }

  public Byte getAddressVersion() {
    return addressVersion;
  }

  public void setAddressVersion(Byte addressVersion) {
    this.addressVersion = addressVersion;
  }

  public List<? extends ConsensusNodeLike> getConsensusNodes() {
    return consensusNodes;
  }

  public void setConsensusNodes(List<ExpressConsensusNode> consensusNodes) {
    this.consensusNodes = consensusNodes;
  }

  public List<ExpressWallet> getWallets() {
    return wallets;
  }

  public void setWallets(List<ExpressWallet> wallets) {
    this.wallets = wallets;
  }

  public Map<String, String> getSettings() {
    return settings;
  }

  public void setSettings(Map<String, String> settings) {
    this.settings = settings;
  }

}
