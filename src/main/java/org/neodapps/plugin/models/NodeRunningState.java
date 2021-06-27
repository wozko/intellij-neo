/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.models;

import com.intellij.icons.AllIcons;
import javax.swing.Icon;

/**
 * Represents the state of a node.
 */
public enum NodeRunningState {
  RUNNING("Running", AllIcons.Debugger.ThreadStates.Idle),
  NOT_RUNNING("Not Running", AllIcons.Actions.Suspend),
  UNKNOWN("Loading", AllIcons.Actions.ProfileYellow);

  private final String name;
  private final Icon icon;

  NodeRunningState(final String name, final Icon icon) {
    this.name = name;
    this.icon = icon;
  }

  @Override
  public String toString() {
    return this.name;
  }

  public Icon getIcon() {
    return this.icon;
  }

}
