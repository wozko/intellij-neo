/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import java.util.Arrays;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.NodeRunningState;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.services.express.NeoExpressService;
import org.neodapps.plugin.topics.NodeChangeNotifier;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents node status in the tool window.
 */
public class SelectedNodeStateComponent extends Wrapper implements Disposable {

  private final Project project;
  private final ToolWindowButton refreshStatusButton;

  private JBTextField nodeNameLabel;
  private JBLabel nodeStatusLabel;
  private JPanel applyInstructions;
  private ToolWindowButton actionButton;

  /**
   * Shows the status if a chain is selected.
   *
   * @param project intellij project
   */
  public SelectedNodeStateComponent(Project project, ChainLike selected, int nodeIndex,
                                    NodeRunningState state, boolean loading) {
    this.project = project;
    nodeNameLabel = new JBTextField();
    nodeNameLabel.setEditable(false);
    nodeStatusLabel = new JBLabel();

    applyInstructions = new JPanel(new FlowLayout());
    applyInstructions.add(new JBLabel(NeoMessageBundle.message("toolwindow.select.node")));
    var mockButton =
        new ToolWindowButton(
            NeoMessageBundle.message("toolwindow.pick.apply"),
            AllIcons.Actions.BuildLoadChanges, null);
    mockButton.setEnabled(false);
    applyInstructions.add(mockButton);
    applyInstructions.add(new JBLabel(NeoMessageBundle.message("toolwindow.select.node.continue")));

    actionButton = new ToolWindowButton(
        NeoMessageBundle.message("toolwindow.private.net.start"),
        AllIcons.Actions.Execute);

    refreshStatusButton = new ToolWindowButton(
        NeoMessageBundle.message("toolwindow.private.net.status"),
        AllIcons.Javaee.UpdateRunningApplication
    );

    if (loading) {
      setLoading();
    } else if (selected == null) {
      setNotSelected();
    } else {
      setRunningState(selected, nodeIndex, state);
    }
  }

  /**
   * Sets the status as not selected.
   */
  private void setNotSelected() {
    var content = getNotSelectedContent();
    setContent(content);
  }

  private void setLoading() {
    var panel = new JPanel(new FlowLayout());
    panel.add(new JBLabel(NeoMessageBundle.message("toolwindow.loading")));
    setContent(panel);
  }

  /**
   * Sets the state of the node as running.
   */
  private void setRunningState(ChainLike chain, int nodeIndex,
                               NodeRunningState runningState) {
    if (chain == null) {
      return;
    }
    var nodeName = String.format("%s (%s)", chain, chain.getNodes().get(nodeIndex));
    var content = getRunningStateContent(nodeName, runningState);
    content.add(refreshStatusButton);
    // set run button 
    if (runningState.equals(NodeRunningState.NOT_RUNNING)
        && chain.getType().equals(BlockChainType.PRIVATE)) {
      setAction(chain, nodeIndex);
      content.add(actionButton);
    }
    setContent(content);
  }

  /**
   * Show run action when a node is not running.
   * Only applies to not running private net.
   */
  private void setAction(ChainLike chain, int nodeIndex) {
    // remove older listeners
    Arrays.stream(actionButton.getActionListeners())
        .forEach(l -> actionButton.removeActionListener(l));

    actionButton.addActionListener(a -> {
      // do not run this in background
      project.getService(NeoExpressService.class)
          .runPrivateNet((PrivateChain) chain, nodeIndex);
    });

    refreshStatusButton.addActionListener(
        a -> project.getMessageBus().syncPublisher(NodeChangeNotifier.NODE_CHANGE)
            .nodeSelected(chain));
  }

  private JPanel getRunningStateContent(String nodeName, NodeRunningState runningState) {
    var statusPanel = new JPanel(new FlowLayout());
    statusPanel.setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0));
    nodeNameLabel.setText(nodeName);
    nodeStatusLabel.setIcon(runningState.getIcon());
    nodeStatusLabel.setText(runningState.toString());
    statusPanel.add(nodeNameLabel);
    statusPanel.add(nodeStatusLabel);
    return statusPanel;
  }

  private JPanel getNotSelectedContent() {
    var statusPanel = new JPanel(new FlowLayout());
    statusPanel.setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0));
    statusPanel.add(applyInstructions);
    return statusPanel;
  }

  @Override
  public void dispose() {
    nodeNameLabel = null;
    nodeStatusLabel = null;
    actionButton = null;
    applyInstructions = null;
  }
}
