/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.panels.Wrapper;
import java.awt.BorderLayout;
import java.util.concurrent.ExecutionException;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoNotifier;
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
            tabsWrapper.setContent(new JPanel());
            statusWrapper.setContent(new SelectedNodeStateComponent(project, null, null, true));
            checkStatus(selectedChain);
          }

          @Override
          public void nodeDeselected() {
            tabsWrapper.setContent(new JPanel());
            statusWrapper.setContent(new SelectedNodeStateComponent(project, null, null, false));
          }
        });
    statusWrapper.setContent(new SelectedNodeStateComponent(project, null, null, false));
    tabsWrapper.setContent(new JPanel());
  }

  private void checkStatus(ChainLike selectedChain) {
    var worker = new SwingWorker<NodeRunningState, Void>() {
      @Override
      protected NodeRunningState doInBackground() {
        return project.getService(BlockchainService.class).checkNodeStatus(selectedChain);
      }

      @Override
      protected void done() {
        NodeRunningState status;
        try {
          status = get();
          statusWrapper
              .setContent(new SelectedNodeStateComponent(project, selectedChain, status, false));
          if (status.equals(NodeRunningState.RUNNING)) {
            tabsWrapper.setContent(new TabsComponent(project, selectedChain));
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
