/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */


package org.neodapps.plugin.services.chain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an invoke file.
 */
public class InvokeFile {

  private Map<String, InvokeFileItem> items;
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
  public Map<String, InvokeFileItem> getItems() throws IOException {
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
      items = Arrays.stream(gson.fromJson(new FileReader(this.path.toFile()),
          InvokeFileItem[].class)).collect(Collectors.toMap(InvokeFileItem::getId, Function
          .identity()));
    }
    return items;
  }

  public Path getPath() {
    return path;
  }

  /**
   * Serialize the invoke file.
   */
  public void saveChanges() throws IOException {
    if (items == null) {
      return;
    }
    var data = new GsonBuilder().setPrettyPrinting().create().toJson(this.items.values());
    Files.write(this.path, data.getBytes());
  }

  /**
   * Adds an item to the invoke file.
   *
   * @param item item to be added.
   */
  public void addItem(InvokeFileItem item) {
    items.put(item.getId(), item);
  }

  /**
   * Removes an item from an invoke file.
   *
   * @param item item to be added.
   */
  public void removeItem(InvokeFileItem item) {
    items.remove(item.getId());
  }
}
