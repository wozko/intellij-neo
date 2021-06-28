/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.models.ChainLike;
import org.neodapps.plugin.models.ConsensusNodeLike;
import org.neodapps.plugin.services.ChainListService;
import org.neodapps.plugin.topics.NodeChangeNotifier;
import org.neodapps.plugin.topics.PrivateChainCreatorNotifier;
import org.neodapps.plugin.topics.RefreshActionNotifier;

/**
 * Represents the interface used to change node.
 */
public class NodeSelectorComponent extends Wrapper {

  private final Project project;

  private JButton applyButton;

  /**
   * Creates the interface used to change node.
   */
  public NodeSelectorComponent(@NotNull Project project) {
    this.project = project;
    var bus = this.project.getMessageBus();
    setContent(getContent(loadChains()));

    // refresh event
    bus.connect().subscribe(RefreshActionNotifier.REFRESH, () -> {
      setContent(getContent(loadChains()));
    });

    // node changed
    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, (e) -> {
      // enable button
      applyButton.setEnabled(true);
      applyButton.setIcon(AllIcons.Actions.BuildLoadChanges);
    });

    // chain added
    // a new private net is created
    bus.connect().subscribe(PrivateChainCreatorNotifier.NEW_PRIVATE_NET_CREATED, (name) -> {
      setContent(getContent(loadAndLookForNewChain(name)));
    });
  }

  private JComponent getContent(java.util.List<ChainLike> chains) {
    var chainListService = project.getService(ChainListService.class);
    var selectedChain = chainListService.getSelectedValue();

    if (selectedChain == null) {
      selectedChain = chains.get(0);
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
    nodeComboBox.setSelectedIndex(selectedChain.getSelectedIndex());


    applyButton =
        new ToolWindowButton(NeoMessageBundle.message("toolwindow.pick.apply"),
            AllIcons.Actions.BuildLoadChanges);

    applyButton.addActionListener(actionEvent -> {

      // disable button
      applyButton.setIcon(AllIcons.Nodes.PpInvalid);
      applyButton.setEnabled(false);

      var selected = (ChainLike) chainComboBox.getSelectedItem();
      assert selected != null;
      selected.setSelectedIndex(nodeComboBox.getSelectedIndex());
      project.getService(ChainListService.class)
          .setSelectedValues(selected);
    });


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

        // un-applied changed
        applyButton.setIcon(AllIcons.General.Modified);
      }
    });

    nodeComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        // un-applied changed
        applyButton.setIcon(AllIcons.General.Modified);
      }
    });

    chainPickerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
    chainPickerPanel.add(chainComboBox);
    chainPickerPanel.add(nodeComboBox);
    chainPickerPanel.add(applyButton);

    return chainPickerPanel;
  }

  private List<ChainLike> loadChains() {
    var chainListService = project.getService(ChainListService.class);
    return chainListService.loadChains();
  }

  private List<ChainLike> loadAndLookForNewChain(String newlyAddedChainName) {
    var chainListService = project.getService(ChainListService.class);
    return chainListService.loadAndLookForNewChain(newlyAddedChainName);
  }

}
