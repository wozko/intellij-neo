/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */


package org.neodapps.plugin.services.chain;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an invoke file.
 */
public class InvokeFile extends File {

  private List<InvokeFileItem> items;
  private long lastModifiedDate;

  public InvokeFile(@NotNull String pathname) {
    super(pathname);
  }

  /**
   * Gets the invoke items in an invoke json file.
   *
   * @return invoke item list read from file
   */
  public List<InvokeFileItem> getItems() {
    boolean update = this.items == null;

    try {
      // if not null, check if the file has updated since last modified
      if (!update) {
        long modifiedDate =
            Files.readAttributes(Paths.get(getPath()), BasicFileAttributes.class).lastModifiedTime()
                .toMillis();
        if (modifiedDate > lastModifiedDate) {
          update = true;
        }
        lastModifiedDate = modifiedDate;
      }

      // json to pojo
      if (update) {
        Gson gson = new Gson();
        items = Arrays.asList(gson.fromJson(new FileReader(this),
            InvokeFileItem[].class));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return items;
  }
}
