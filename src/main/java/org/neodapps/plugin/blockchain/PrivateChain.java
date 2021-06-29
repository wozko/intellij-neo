/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.blockchain;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import org.neodapps.plugin.blockchain.express.ExpressConfig;

/**
 * Represents a private net created by neo-express.
 */
public class PrivateChain implements ChainLike {

  /**
   * neo-express config file
   * Eg: default.neo-express
   */
  private final Path configFileLocation;

  private ExpressConfig config;
  private int selectedIndex;
  private long lastModifiedDate;

  public PrivateChain(Path configFileLocation) {
    this.configFileLocation = configFileLocation;
  }

  /**
   * Used to get the configuration of the private net.
   */
  public ExpressConfig getConfig() {

    // read the file if config is null
    boolean update = this.config == null;

    try {
      // if not null, check if the file has updated since last modified
      if (!update) {
        long modifiedDate =
            Files.readAttributes(configFileLocation, BasicFileAttributes.class).lastModifiedTime()
                .toMillis();
        if (modifiedDate > lastModifiedDate) {
          update = true;
        }
        lastModifiedDate = modifiedDate;
      }

      // json to pojo
      if (update) {
        Gson gson = new Gson();
        config = gson.fromJson(new FileReader(configFileLocation.toAbsolutePath().toFile()),
            ExpressConfig.class);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return config;
  }

  @Override
  public String toString() {
    return configFileLocation.getFileName().toString();
  }

  @Override
  public BlockChainType getType() {
    return BlockChainType.PRIVATE;
  }

  @Override
  public List<? extends ConsensusNodeLike> getNodes() {
    return getConfig().getConsensusNodes();
  }

  @Override
  public int getSelectedIndex() {
    return this.selectedIndex;
  }

  @Override
  public void setSelectedIndex(int selectedIndex) {
    this.selectedIndex = selectedIndex;
  }

  @Override
  public ConsensusNodeLike getSelectedItem() {
    return getNodes().get(selectedIndex);
  }


}
