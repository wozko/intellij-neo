/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */


package org.neodapps.plugin.services.chain;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an invoke file.
 */
public class InvokeFile {

  private List<InvokeFileItem> items;
  private long lastModifiedDate;
  private final Path path;

  public InvokeFile(@NotNull Path path) throws IOException {
    this.path = path;
    this.items = getItems();
  }

  /**
   * Gets the invoke items in an invoke json file.
   *
   * @return invoke item list read from file
   */
  public List<InvokeFileItem> getItems() throws IOException {
    boolean update = this.items == null;

    // if not null, check if the file has updated since last modified
    if (!update) {
      long modifiedDate =
          Files.readAttributes(this.path, BasicFileAttributes.class).lastModifiedTime()
              .toMillis();
      if (modifiedDate > lastModifiedDate) {
        update = true;
      }
      lastModifiedDate = modifiedDate;
    }

    // json to pojo
    if (update) {
      Gson gson = new Gson();
      items = Arrays.asList(gson.fromJson(new FileReader(this.path.toFile()),
          InvokeFileItem[].class));
    }
    return items;
  }

  public Path getPath() {
    return path;
  }
}
