/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.models;

import java.net.URI;

/**
 * Represents a consensus node in a public chain.
 */
public class ConsensusNode implements ConsensusNodeLike {
  private final URI endpoint;
  private final Integer rpcPort;

  public ConsensusNode(URI endpoint, Integer rpcPort) {
    this.endpoint = endpoint;
    this.rpcPort = rpcPort;
  }

  public URI getEndpoint() {
    return endpoint;
  }

  public Integer getRpcPort() {
    return rpcPort;
  }

  @Override
  public String toString() {
    return String.format("%s:%d", endpoint, rpcPort);
  }
}