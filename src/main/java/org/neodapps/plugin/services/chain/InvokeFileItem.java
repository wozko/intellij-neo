/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.chain;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an item in invoke file.
 * Typically an invoke file consists of one or more of these.
 */
public class InvokeFileItem {

  @SerializedName("contract")
  private String contract;

  @SerializedName("operation")
  private String operation;

  @SerializedName("args")
  private List<Object> args;

  // a unique field recognize an invoke item
  // this will not be serialized
  // this is used to map the equivalent ui component
  private transient String id;

  public String getContract() {
    return contract;
  }

  public String getOperation() {
    return operation;
  }

  public List<Object> getArgs() {
    return args;
  }

  /**
   * Returns the unique id of an invoke file.
   *
   * @return a unique id
   */
  public String getId() {
    if (id == null) {
      id = UUID.randomUUID().toString();
    }
    return id;
  }

  public void setContract(String contract) {
    this.contract = contract;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public void setArgs(List<Object> args) {
    this.args = args;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InvokeFileItem)) {
      return false;
    }
    InvokeFileItem that = (InvokeFileItem) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
