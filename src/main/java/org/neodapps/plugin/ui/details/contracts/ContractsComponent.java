/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.contracts;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import io.neow3j.protocol.core.response.NeoGetContractState;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.services.chain.ContractServices;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows contracts details.
 */
public class ContractsComponent extends Wrapper {

  final Project project;
  final ChainLike chain;

  final Wrapper contractsComponent;

  /**
   * Creates the component that shows contracts details.
   *
   * @param project intellij project
   * @param chain   selected chain
   */
  public ContractsComponent(Project project, ChainLike chain) {
    this.project = project;
    this.chain = chain;

    contractsComponent = new Wrapper();

    var panel = JBUI.Panels.simplePanel();
    panel.addToTop(getToolBar(chain));
    panel.addToCenter(contractsComponent);

    contractsComponent.setContent(getLoadingComponent());
    setContent(panel);
    setContractList();
  }

  private void setContractList() {
    var worker = new SwingWorker<List<NeoGetContractState.ContractState>, Void>() {
      @Override
      protected List<NeoGetContractState.ContractState> doInBackground() {
        return project.getService(ContractServices.class).getContracts(chain);
      }

      @Override
      protected void done() {
        try {
          var contracts = get();
          final var panel = new JPanel();
          panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

          contracts.forEach(contractState -> panel.add(getContractComponent(contractState)));

          contractsComponent.setContent(new JBScrollPane(panel));
        } catch (InterruptedException | ExecutionException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };
    worker.execute();
  }

  private JComponent getLoadingComponent() {
    var panel = new JPanel();
    panel.setBorder(JBUI.Borders.empty(5));
    panel.add(new JBLabel(NeoMessageBundle.message("toolwindow.loading")));
    return panel;
  }

  private JComponent getToolBar(ChainLike chain) {
    // toolbar has two buttons
    var buttonPanel = JBUI.Panels.simplePanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    // create wallet
    var deployContractButton =
        new ToolWindowButton(
            NeoMessageBundle.message("contracts.deploy"),
            AllIcons.Modules.AddExcludedRoot, actionEvent -> {
          var popup = new DeployContractPopup(project, new ArrayList<>());
          popup.showPopup();
        });
    buttonPanel.add(deployContractButton);

    return buttonPanel;
  }

  private JComponent getContractComponent(NeoGetContractState.ContractState contractState) {
    final var panel = new JPanel();
    panel.setBorder(
        JBUI.Borders.compound(JBUI.Borders.customLine(JBColor.border()), JBUI.Borders.empty(5, 2)));

    var manifest = contractState.getManifest();
    panel.add(new JBLabel(manifest.getName()));

    return panel;
  }
}
