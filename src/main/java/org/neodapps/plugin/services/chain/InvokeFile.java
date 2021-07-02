package org.neodapps.plugin.services.chain;

import java.util.List;

/**
 * Represents an invoke file.
 */
public class InvokeFile {
  private List<InvokeFileItem> items;

  public List<InvokeFileItem> getItems() {
    return items;
  }

  public void setItems(List<InvokeFileItem> items) {
    this.items = items;
  }
}
