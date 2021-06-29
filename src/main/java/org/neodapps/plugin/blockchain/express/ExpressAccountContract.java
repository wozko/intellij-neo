/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.blockchain.express;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a contract in an account
 * in neo-express config file.
 */
public class ExpressAccountContract {

  @SerializedName("script")
  private String script;

  @SerializedName("parameters")
  private List<String> parameters;

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }
}
