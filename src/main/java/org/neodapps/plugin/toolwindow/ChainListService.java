/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.toolwindow;

import com.intellij.openapi.project.Project;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.neodapps.plugin.Notifier;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.Chain;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.ConsensusNode;
import org.neodapps.plugin.neoexpress.PrivateChain;

/**
 * A service that maintains the list of chains.
 */
public class ChainListService {

  private final Project neoProject;

  public ChainListService(Project project) {
    this.neoProject = project;
  }

  /**
   * Reloads the blockchain list.
   *
   * @return returns blockchain list
   */
  public List<ChainLike> loadChains() {
    List<ChainLike> chains = new ArrayList<>();
    chains.add(getTestNet());

    // load express chains
    chains.addAll(loadExpressChains());
    return chains;
  }

  /**
   * Reads the parent location for .neo-express files.
   *
   * @return List of private chains
   */
  private List<PrivateChain> loadExpressChains() {
    File dir = new File(Objects.requireNonNull(neoProject.getBasePath()));
    FileFilter fileFilter = new WildcardFileFilter("*.neo-express");
    File[] files = dir.listFiles(fileFilter);
    List<PrivateChain> chains = new ArrayList<>();
    if (files != null) {
      for (File file : files) {
        chains.add(new PrivateChain(file.toPath()));
      }
    }
    return chains;
  }

  private ChainLike getTestNet() {
    // TODO: possibly get this from an external resource
    return new Chain(
        BlockChainType.TEST,
        Arrays.asList(
            new ConsensusNode(getUri("http://seed1t.neo.org"), 20332),
            new ConsensusNode(getUri("http://seed2t.neo.org"), 20332),
            new ConsensusNode(getUri("http://seed3t.neo.org"), 20332),
            new ConsensusNode(getUri("http://seed4t.neo.org"), 20332),
            new ConsensusNode(getUri("http://seed5t.neo.org"), 20332)
        )
    );
  }

  private URI getUri(String url) {
    try {
      return new URI(url);
    } catch (URISyntaxException e) {
      // this will not be visited
      Notifier.notifyError(neoProject, "Node URL format error.");
    }
    return null;
  }
}
