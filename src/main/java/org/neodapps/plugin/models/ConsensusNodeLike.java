/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.models;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents a node interface of private or public chain.
 */
public interface ConsensusNodeLike {

  URI getEndpoint() throws URISyntaxException;

  Integer getRpcPort();
}
