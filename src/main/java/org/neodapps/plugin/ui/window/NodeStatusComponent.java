/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.models.BlockChainType;
import org.neodapps.plugin.models.NodeRunningState;
import org.neodapps.plugin.models.PrivateChain;
import org.neodapps.plugin.services.ChainListService;
import org.neodapps.plugin.services.NeoExpressService;
import org.neodapps.plugin.topics.NodeChangeNotifier;
import org.neodapps.plugin.topics.NodeStatusNotifier;

/**
 * Represent the component that show node status.
 */
public class NodeStatusComponent extends Wrapper {
  private final Project project;

  /**
   * Creates the component that show node status.
   *
   * @param project intelli project
   */
  public NodeStatusComponent(@NotNull Project project) {
    this.project = project;
    setContent(getContent(NodeRunningState.UNKNOWN));

    var bus = this.project.getMessageBus();

    // node changed
    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, (changed) -> {
      setContent(getContent(NodeRunningState.UNKNOWN));
    });

    // node started running
    bus.connect().subscribe(NodeStatusNotifier.STATUS_CHANGED, (runningState, chain) -> {
      setContent(getContent(runningState));
    });

  }

  private JComponent getContent(NodeRunningState runningState) {
    var statusPanel = JBUI.Panels.simplePanel();
    statusPanel.setLayout(new FlowLayout());
    statusPanel.setBorder(JBUI.Borders.customLineBottom(JBColor.border()));

    var selectedValue = project.getService(ChainListService.class).getSelectedValue();

    if (selectedValue == null) {
      statusPanel.add(new JBLabel(NeoMessageBundle.message("toolwindow.select.node")));
      var mockButton =
          new ToolWindowButton(
              NeoMessageBundle.message("toolwindow.pick.apply"),
              AllIcons.Actions.BuildLoadChanges, null);
      mockButton.setEnabled(false);
      statusPanel.add(mockButton);
      statusPanel.add(new JBLabel(NeoMessageBundle.message("toolwindow.select.node2")));
      return statusPanel;
    }

    var selectedNode = selectedValue.getSelectedItem();
    // node name
    var selectedNodeName =
        new JBTextField(String.format("%s (%s)", selectedValue, selectedNode.toString()));
    selectedNodeName.setEditable(false);

    // status
    JBLabel status = new JBLabel();
    status.setText(runningState.toString());
    status.setIcon(runningState.getIcon());

    statusPanel.add(selectedNodeName);
    statusPanel.add(status);

    if (runningState.equals(NodeRunningState.NOT_RUNNING) && selectedValue.getType().equals(
        BlockChainType.PRIVATE)) {
      // add an start button
      statusPanel.add(getRunActionButton((PrivateChain) selectedValue));
    }
    return statusPanel;
  }

  private ToolWindowButton getRunActionButton(
      PrivateChain value) {
    return new ToolWindowButton(
        NeoMessageBundle.message("toolwindow.private.net.start"),
        AllIcons.Actions.Execute,
        e -> {
          // run the node
          project.getService(NeoExpressService.class).runPrivateNet(value);
        }
    );
  }
}
