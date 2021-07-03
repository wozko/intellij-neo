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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
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
        var selectedChain = (ChainLike) chainComboBox.getSelectedItem();

        nodeComboBox.removeAllItems();
        assert selectedChain != null;
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

    // set default chains
    setChains(null);

    // apply button action
    applyButton.addActionListener(
        e -> {
          final var selected = (ChainLike) chainComboBox.getSelectedItem();
          assert selected != null;
          selected.setSelectedIndex(nodeComboBox.getSelectedIndex());
          applyChanges(selected);
        }
    );

    // set listeners
    var bus = project.getMessageBus();
    // node change
    bus.connect().subscribe(NodeChangeNotifier.NODE_CHANGE, new NodeChangeNotifier() {
      @Override
      public void nodeSelected(ChainLike selectedChain) {
        setSelectedChain(selectedChain);
      }

      @Override
      public void nodeDeselected() {
        setChains(null);
      }
    });

    // new net created
    bus.connect()
        .subscribe(PrivateChainCreatorNotifier.NEW_PRIVATE_NET_CREATED, this::setChains);
  }


  /**
   * Sets chains to the chain list.
   */
  private void setChains(String newlyAddedChainName) {
    var worker = new SwingWorker<List<ChainLike>, Void>() {
      @Override
      protected List<ChainLike> doInBackground() {
        var service = project.getService(ChainListService.class);
        if (newlyAddedChainName == null) {
          return service.loadChains();
        } else {
          return service.loadAndLookForNewChain(newlyAddedChainName);
        }
      }

      @Override
      protected void done() {
        try {
          var chains = get();
          // remove all
          chainComboBox.removeAllItems();

          for (ChainLike chain : chains) {
            chainComboBox.addItem(chain);
          }

          markSelectedChain(chains);
        } catch (InterruptedException | ExecutionException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };
    worker.execute();
  }

  private void markSelectedChain(List<ChainLike> chains) {
    var worker = new SwingWorker<Optional<ChainLike>, Void>() {
      @Override
      protected Optional<ChainLike> doInBackground() {
        var service = project.getService(ChainListService.class);
        return service.getAppliedChain();
      }

      @Override
      protected void done() {
        try {
          var appliedChain = get();
          appliedChain.ifPresent(chainLike -> setSelectedChain(chainLike));
          if (appliedChain.isEmpty()) {
            setSelectedChain(chains.get(0));
          }
        } catch (InterruptedException | ExecutionException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };
    worker.execute();
  }

  private void applyChanges(ChainLike selected) {
    var worker = new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
        var service = project.getService(ChainListService.class);
        service.setAppliedChain(selected);
        return null;
      }
    };
    worker.execute();
  }

  /**
   * Sets the selected chain in UI.
   *
   * @param chain selected chain
   */
  private void setSelectedChain(ChainLike chain) {
    chainComboBox.setSelectedItem(chain);
    nodeComboBox.removeAllItems();
    for (ConsensusNodeLike node : chain.getNodes()) {
      nodeComboBox.addItem(node);
    }
    nodeComboBox.setSelectedIndex(chain.getSelectedIndex());
    // reset icon changes
    resetApplyButtonChange();
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
}
