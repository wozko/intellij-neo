/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services;

import com.intellij.openapi.project.Project;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.models.BlockChainType;
import org.neodapps.plugin.models.Chain;
import org.neodapps.plugin.models.ChainLike;
import org.neodapps.plugin.models.ConsensusNode;
import org.neodapps.plugin.models.PrivateChain;
import org.neodapps.plugin.topics.NodeChangeNotifier;

/**
 * A service that maintains the list of chains.
 */
public class ChainListService {

  private final Project neoProject;

  private ChainLike selected;

  public ChainListService(Project project) {
    this.neoProject = project;
  }

  /**
   * Sets the selected chain and node and sends out a refresh event.
   *
   * @param selected selected chain
   */
  public void setSelectedValues(ChainLike selected) {
    this.selected = selected;
    // publish change node event so the ui get updated
    NodeChangeNotifier publisher =
        neoProject.getMessageBus().syncPublisher(NodeChangeNotifier.NODE_CHANGE);
    publisher.afterAction(selected);
  }

  public ChainLike getSelectedValue() {
    return selected;
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
   * Reloads the blockchain list and looks for a newly added private net.
   *
   * @return returns blockchain list
   */
  public List<ChainLike> loadAndLookForNewChain(String newlyAddedChainName) {
    List<ChainLike> chains = new ArrayList<>();
    chains.add(getTestNet());

    // load express chains
    chains.addAll(loadExpressChains(newlyAddedChainName));
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

  /**
   * Reads the parent location for .neo-express files.
   *
   * @param newlyAddedChainName newly added file name to look for
   * @return List of private chains
   */
  private List<PrivateChain> loadExpressChains(String newlyAddedChainName) {

    var maxAmountTries = 3; // look for newly created .neo-express file 3 times before give up
    var waitTime = 1000; // wait 1 second in between checks

    var counter = 0;
    var foundFile = false;
    List<File> files = null;
    while (counter++ < maxAmountTries) {
      files = getFilteredFileList();
      foundFile = files.stream()
          .anyMatch(f -> FilenameUtils.getBaseName(f.getName()).equals(newlyAddedChainName));
      if (foundFile) {
        break;
      } else {
        try {
          Thread.sleep(waitTime);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    List<PrivateChain> chains = new ArrayList<>();
    for (File file : files) {
      chains.add(new PrivateChain(file.toPath()));
    }
    return chains;

  }

  private List<File> getFilteredFileList() {
    File dir = new File(Objects.requireNonNull(neoProject.getBasePath()));
    FileFilter fileFilter = new WildcardFileFilter("*.neo-express");
    File[] files = dir.listFiles(fileFilter);
    List<File> fileList;
    if (files == null) {
      fileList = new ArrayList<>();
    } else {
      fileList = List.of(files);
    }
    return fileList;
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
      NeoNotifier.notifyError(neoProject, "Node URL format error.");
    }
    return null;
  }
}
