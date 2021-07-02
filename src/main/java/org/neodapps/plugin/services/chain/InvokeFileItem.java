package org.neodapps.plugin.services.chain;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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

  public String getContract() {
    return contract;
  }

  public String getOperation() {
    return operation;
  }

  public List<Object> getArgs() {
    return args;
  }
}
