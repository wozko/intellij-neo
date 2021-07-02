/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.toolbar;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.ConsensusNodeLike;
import org.neodapps.plugin.services.chain.ChainListService;
import org.neodapps.plugin.topics.NodeChangeNotifier;
import org.neodapps.plugin.topics.PrivateChainCreatorNotifier;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the node picker component.
 */
public class NodePickerComponent extends Wrapper implements Disposable {

  private final Project project;
  private ComboBox<ChainLike> chainComboBox;
  private ComboBox<ConsensusNodeLike> nodeComboBox;
  private ToolWindowButton applyButton;

  private List<ChainLike> chains;
  private ChainLike selectedChain;
  private int selectedIndex;

  /**
   * Creates the node picker component.
   */
  public NodePickerComponent(Project project) {
    this.project = project;
    chainComboBox = new ComboBox<>();
    nodeComboBox = new ComboBox<>();

    applyButton = new ToolWindowButton(NeoMessageBundle.message("toolwindow.pick.apply"),
        AllIcons.Actions.BuildLoadChanges);

    // change node list on chain change
    chainComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        selectedChain = (ChainLike) chainComboBox.getSelectedItem();

        nodeComboBox.removeAllItems();
        for (ConsensusNodeLike node : selectedChain.getNodes()) {
          nodeComboBox.addItem(node);
        }
        // set first node as default
        nodeComboBox.setSelectedIndex(selectedChain.getSelectedIndex());

        // un-applied changed
        showIconAsUnsavedChanges();
      }
    });

    nodeComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        selectedIndex = nodeComboBox.getSelectedIndex();
        // un-applied changed
        showIconAsUnsavedChanges();
      }
    });

    var chainPickerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    chainPickerPanel.setBorder(JBUI.Borders.customLine(JBColor.border()));
    chainPickerPanel.add(chainComboBox);
    chainPickerPanel.add(nodeComboBox);
    chainPickerPanel.add(applyButton);
    setContent(chainPickerPanel);

    // data
    var service = project.getService(ChainListService.class);
    var chains = service.loadChains();
    var appliedChain = service.getAppliedChain();
    setChains(chains);
    appliedChain.ifPresent(this::setSelectedChain);

    // apply button action
    setApplyButtonAction();

    // set listeners
    setTopicListeners(project);
  }

  /**
   * Sets chains to the chain list.
   */
  private void setChains(List<ChainLike> chains) {
    this.chains = chains;
    // remove all
    chainComboBox.removeAllItems();

    for (ChainLike chain : chains) {
      chainComboBox.addItem(chain);
    }
  }

  /**
   * Set action on apply button.
   */
  private void setApplyButtonAction() {
    // remove older listeners
    Arrays.stream(applyButton.getActionListeners())
        .forEach(l -> applyButton.removeActionListener(l));

    var service = project.getService(ChainListService.class);
    applyButton.addActionListener(
        e -> {
          final var selected = getSelectedChain();
          selected.setSelectedIndex(getSelectedNode());
          service.setAppliedChain(selected);
        }
    );
  }

  private ChainLike getSelectedChain() {
    return selectedChain;
  }

  /**
   * Sets the selected chain in UI.
   *
   * @param chain selected chain
   */
  private void setSelectedChain(ChainLike chain) {
    if (chain == null) {
      chain = chains.get(0);
    }
    chainComboBox.setSelectedItem(chain);
    nodeComboBox.removeAllItems();
    for (ConsensusNodeLike node : chain.getNodes()) {
      nodeComboBox.addItem(node);
    }
    nodeComboBox.setSelectedIndex(chain.getSelectedIndex());

    // reset icon changes
    resetApplyButtonChange();
  }

  private int getSelectedNode() {
    return selectedIndex;
  }

  @Override
  public void dispose() {
    chainComboBox = null;
    nodeComboBox = null;
    applyButton = null;
  }


  private void showIconAsUnsavedChanges() {
    applyButton.setIcon(AllIcons.General.Modified);
  }

  private void resetApplyButtonChange() {
    applyButton.setIcon(AllIcons.Actions.BuildLoadChanges);
  }

  private void setTopicListeners(Project project) {
    var bus = project.getMessageBus();
    var service = project.getService(ChainListService.class);

    // node changed
    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, new NodeChangeNotifier() {
      @Override
      public void nodeSelected(ChainLike selectedChain) {
        setSelectedChain(selectedChain);
      }

      @Override
      public void nodeDeselected() {
        setChains(service.loadChains());
      }
    });

    // new net created
    bus.connect()
        .subscribe(PrivateChainCreatorNotifier.NEW_PRIVATE_NET_CREATED, (privateNetName) -> {
          setChains(service.loadAndLookForNewChain(privateNetName));
        });
  }
}
