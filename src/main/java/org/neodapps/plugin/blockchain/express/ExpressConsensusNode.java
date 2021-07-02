/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.blockchain.express;

import com.google.gson.annotations.SerializedName;
import org.neodapps.plugin.blockchain.ConsensusNodeLike;

/**
 * Represents a node in neo-express config file.
 */
public class ExpressConsensusNode implements ConsensusNodeLike {

  @SerializedName("tcp-port")
  private Integer tcpPort;

  @SerializedName("ws-port")
  private Integer wsPort;

  @SerializedName("rpc-port")
  private Integer rpcPort;

  @SerializedName("debug-port")
  private Integer debugPort;

  @SerializedName("wallet")
  private ExpressWallet wallet;


  public Integer getTcpPort() {
    return tcpPort;
  }

  public void setTcpPort(Integer tcpPort) {
    this.tcpPort = tcpPort;
  }

  public Integer getWsPort() {
    return wsPort;
  }

  public void setWsPort(Integer wsPort) {
    this.wsPort = wsPort;
  }

  public Integer getRpcPort() {
    return rpcPort;
  }

  public void setRpcPort(Integer rpcPort) {
    this.rpcPort = rpcPort;
  }

  public Integer getDebugPort() {
    return debugPort;
  }

  public void setDebugPort(Integer debugPort) {
    this.debugPort = debugPort;
  }

  public ExpressWallet getWallet() {
    return wallet;
  }

  public void setWallet(ExpressWallet wallet) {
    this.wallet = wallet;
  }

  @Override
  public String getUrl() {
    return String.format("%s:%d", "http://127.0.0.1", rpcPort);
  }

  @Override
  public String toString() {
    return String.format("%s:%d", "http://127.0.0.1", rpcPort);
  }
}
