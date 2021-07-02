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
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
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

    contractsComponent.setContent(getComponent());
    setContent(panel);
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

  private JComponent getComponent() {
    var contracts = project.getService(ContractServices.class).getContracts(chain);
    final var panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    contracts.forEach(contractState -> {
      panel.add(getContractComponent(contractState));
    });

    return new JBScrollPane(panel);
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
