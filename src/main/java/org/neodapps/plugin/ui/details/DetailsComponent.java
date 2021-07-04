/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.panels.Wrapper;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.BlockChainType;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.blockchain.NodeRunningState;
import org.neodapps.plugin.services.chain.BlockchainService;
import org.neodapps.plugin.topics.NodeChangeNotifier;

/**
 * Represents the component with status and tabs.
 */
public class DetailsComponent extends JPanel implements Disposable {
  private final Project project;

  private Wrapper statusWrapper;
  private Wrapper tabsWrapper;

  /**
   * Creates the component with status and tabs.
   */
  public DetailsComponent(Project project) {
    this.project = project;
    this.statusWrapper = new Wrapper();
    this.tabsWrapper = new Wrapper();

    setLayout(new BorderLayout());

    add(this.statusWrapper, BorderLayout.NORTH);
    add(this.tabsWrapper, BorderLayout.CENTER);

    project.getMessageBus().connect().subscribe(NodeChangeNotifier.NODE_CHANGE,
        new NodeChangeNotifier() {
          @Override
          public void nodeSelected(ChainLike selectedChain) {
            checkStatus(selectedChain);
          }

          @Override
          public void nodeDeselected() {
            checkStatus(null);
          }
        });

    checkStatus(null);
  }

  private void checkStatus(ChainLike selectedChain) {
    if (selectedChain == null) {
      statusWrapper.setContent(new SelectedNodeStateComponent(project, null, 0, null, false));
      return;
    }
    tabsWrapper.setContent(new JPanel());
    statusWrapper.setContent(new SelectedNodeStateComponent(project, null, 0, null, true));
    var worker = new SwingWorker<List<NodeRunningState>, Void>() {
      @Override
      protected List<NodeRunningState> doInBackground() {
        var list = new ArrayList<NodeRunningState>();
        var service = project.getService(BlockchainService.class);
        if (selectedChain.getType().equals(BlockChainType.PRIVATE)) {
          // if private chain, all nodes should be checked
          selectedChain.getNodes()
              .forEach(node -> list.add(service.checkNodeStatus(selectedChain, node)));
        } else {
          // if public check only the selected node
          list.add(service.checkNodeStatus(selectedChain, selectedChain.getSelectedItem()));
        }
        return list;
      }

      @Override
      protected void done() {
        List<NodeRunningState> statusList;
        try {
          statusList = get();
          if (selectedChain.getType().equals(BlockChainType.PRIVATE)) {
            // if private chain, all nodes should be checked
            var nodes = selectedChain.getNodes();
            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            var running = true;
            for (int i = 0; i < nodes.size(); i++) {
              var node = nodes.get(i);
              var status = statusList.get(i);
              running = running && status.equals(NodeRunningState.RUNNING);
              panel
                  .add(new SelectedNodeStateComponent(project, selectedChain, i, status, false));
            }
            statusWrapper.setContent(panel);
            if (running) {
              tabsWrapper.setContent(new TabsComponent(project, selectedChain));
            }
          } else {
            // if public only selected node
            statusWrapper.setContent(new SelectedNodeStateComponent(project, selectedChain,
                0, statusList.get(0), false));

            if (statusList.get(0).equals(NodeRunningState.RUNNING)) {
              tabsWrapper.setContent(new TabsComponent(project, selectedChain));
            }
          }
        } catch (InterruptedException | ExecutionException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };
    worker.execute();
  }


  @Override
  public void dispose() {
    statusWrapper = null;
    tabsWrapper = null;
  }
}
