/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.MessageBundle;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.ConsensusNodeLike;
import org.neodapps.plugin.blockchain.NodeRunningState;
import org.neodapps.plugin.toolwindow.popups.CreatePrivateNetPopup;
import org.neodapps.plugin.toolwindow.topics.NodeChangeNotifier;
import org.neodapps.plugin.toolwindow.topics.NodeListNotifier;

/**
 * Represents the content shown in tool window.
 */
public class ToolWindowComponent implements Disposable {

  private final Project project;
  private JBSplitter panel;

  private Wrapper chainsListWrapper;
  private Wrapper statusWrapper;
  private Wrapper mainDataWrapper;

  private List<ChainLike> chains;

  private boolean selected;
  private ChainLike selectedChain;
  private ConsensusNodeLike selectedNode;

  /**
   * Creates the tool window content and watch for changes.
   *
   * @param project neo project.
   */
  public ToolWindowComponent(@NotNull Project project) {
    this.project = project;
    // subscribe to events and change UI
    MessageBus bus = project.getMessageBus();

    // chain or node change
    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, (chain, node) -> {
      // set selected value
      selected = true;
      selectedChain = chain;
      selectedNode = node;

      chainsListWrapper.setContent(getChainListWrapperContent());
      statusWrapper.setContent(getStatusWrapperContent());
      mainDataWrapper.setContent(getDataWrapperContent());
    });

    // refresh event
    bus.connect().subscribe(NodeListNotifier.REFRESH_NODE, () -> {
      // update chain list
      chains = project.getService(ChainListService.class).loadChains();

      // reset selected chains/node
      selected = false;
      selectedChain = null;
      selectedNode = null;

      chainsListWrapper.setContent(getChainListWrapperContent());
      statusWrapper.setContent(getStatusWrapperContent());
      mainDataWrapper.setContent(getDataWrapperContent());

    });

    // a new private net is created
    bus.connect().subscribe(NodeListNotifier.CHAIN_ADDED, () -> {
      // update chain list
      chains = project.getService(ChainListService.class).loadChains();

      // update chain list
      chainsListWrapper.setContent(getChainListWrapperContent());
    });
  }

  /**
   * Returns the content required for tool window.
   *
   * @return the content that will be used by {@link NeoToolWindowFactory}
   */
  public JPanel getContent() {

    if (panel == null) {
      // split window into two panels
      panel = new JBSplitter(true, 0.2f);

      // no chains/node have selected
      selected = false;

      // set toolbar
      panel.setFirstComponent(createToolBar());

      // set data panel
      panel.setSecondComponent(getDetails());
    }
    return panel;
  }

  @Override
  public void dispose() {
    panel = null;
  }

  private JComponent createToolBar() {
    JPanel toolbar = JBUI.Panels.simplePanel();
    toolbar.setBorder(JBUI.Borders.customLineBottom(JBColor.border()));
    toolbar.setLayout(new FlowLayout(FlowLayout.LEADING));

    // chain/node picker
    chainsListWrapper = new Wrapper();
    chainsListWrapper.setContent(getChainListWrapperContent());
    toolbar.add(chainsListWrapper);

    // create/refresh buttons
    toolbar.add(getActionPanel());

    return toolbar;
  }

  private JPanel getChainListWrapperContent() {
    if (chains == null) {
      chains = project.getService(ChainListService.class).loadChains();
    }

    // if selected are empty, set to first one
    if (selectedChain == null || selectedNode == null) {
      selectedChain = chains.get(0);
      selectedNode = selectedChain.getNodes().get(0);
    }

    var chainPickerPanel = JBUI.Panels.simplePanel();
    chainPickerPanel.setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 1, 1, 1));

    // pick the network
    final ComboBox<ChainLike> chainComboBox = new ComboBox<>();
    // add all chains to picker
    for (ChainLike chain : chains) {
      chainComboBox.addItem(chain);
    }
    chainComboBox.setSelectedItem(selectedChain);

    // pick the node
    final ComboBox<ConsensusNodeLike> nodeComboBox = new ComboBox<>();
    for (ConsensusNodeLike node : selectedChain.getNodes()) {
      nodeComboBox.addItem(node);
    }
    nodeComboBox.setSelectedItem(selectedNode);

    // change node list on chain change
    chainComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        ChainLike selection = (ChainLike) e.getItem();

        nodeComboBox.removeAllItems();
        for (ConsensusNodeLike node : selection.getNodes()) {
          nodeComboBox.addItem(node);
        }
        // set first node as default
        nodeComboBox.setSelectedIndex(0);

      }
    });

    chainPickerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
    chainPickerPanel.add(chainComboBox);
    chainPickerPanel.add(nodeComboBox);

    JButton applyChangesButton =
        new ToolWindowButton(MessageBundle.message("toolwindow.pick.apply"),
            AllIcons.Actions.BuildLoadChanges, actionEvent -> {

          // publish change node event so the ui get updated
          NodeChangeNotifier publisher =
              project.getMessageBus().syncPublisher(NodeChangeNotifier.NODE_CHANGE);
          publisher.afterAction((ChainLike) chainComboBox.getSelectedItem(),
              (ConsensusNodeLike) nodeComboBox.getSelectedItem());
        });

    chainPickerPanel.add(applyChangesButton);

    return chainPickerPanel;
  }

  private JPanel getActionPanel() {
    JPanel buttonPanel = JBUI.Panels.simplePanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    // create private net button
    JButton createPrivateNetButton =
        new ToolWindowButton(MessageBundle.message("toolwindow.create.private.net"),
            AllIcons.General.Add, actionEvent -> {
          CreatePrivateNetPopup popup = new CreatePrivateNetPopup(project);
          popup.showPopup();
        });
    buttonPanel.add(createPrivateNetButton);

    // refresh icon
    JButton refreshButton = new ToolWindowButton("", AllIcons.Javaee.UpdateRunningApplication,
        actionEvent -> {
          // publish refresh node event so the ui get updated
          NodeListNotifier publisher =
              project.getMessageBus().syncPublisher(NodeListNotifier.REFRESH_NODE);
          publisher.afterAction();
        });
    buttonPanel.add(refreshButton);
    return buttonPanel;
  }

  private JPanel getDetails() {

    JPanel content = JBUI.Panels.simplePanel();
    content.setLayout(new BorderLayout());

    // add status panel
    statusWrapper = new Wrapper();
    statusWrapper.setContent(getStatusWrapperContent());
    content.add(statusWrapper, BorderLayout.NORTH);

    // add main details
    mainDataWrapper = new Wrapper();
    mainDataWrapper.setContent(getDataWrapperContent());
    content.add(mainDataWrapper, BorderLayout.CENTER);

    return content;
  }

  private JPanel getStatusWrapperContent() {
    var statusPanel = JBUI.Panels.simplePanel();
    statusPanel.setLayout(new FlowLayout());
    statusPanel.setBorder(JBUI.Borders.customLineBottom(JBColor.border()));

    if (!selected) {
      statusPanel.add(new JBLabel(MessageBundle.message("toolwindow.select.node")));
      var mockButton = new ToolWindowButton(MessageBundle.message("toolwindow.pick.apply"),
          AllIcons.Actions.BuildLoadChanges, null);
      mockButton.setEnabled(false);
      statusPanel.add(mockButton);
      statusPanel.add(new JBLabel(MessageBundle.message("toolwindow.select.node2")));
      return statusPanel;
    }

    // node name
    var selectedNodeName = new JBTextField(selectedNode.toString());
    selectedNodeName.setEditable(false);

    // status
    JBLabel status = new JBLabel();
    NodeRunningState state = NodeRunningState.RUNNING;
    // todo
    status.setText(state.toString());
    status.setIcon(state.getIcon());

    statusPanel.add(selectedNodeName);
    statusPanel.add(status);

    return statusPanel;
  }

  private JPanel getDataWrapperContent() {
    return new JPanel();
  }

  static class ToolWindowButton extends JButton {
    public ToolWindowButton(String text, Icon icon, ActionListener onClick) {
      super(text);
      setIcon(icon);
      addActionListener(onClick);

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          setCursor(new Cursor(Cursor.HAND_CURSOR));
          super.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          super.mouseExited(e);
        }
      });
    }
  }
}
