package org.neodapps.plugin.ui.details.contracts;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.FormBuilder;
import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.core.response.ContractManifest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.neodapps.plugin.NeoMessageBundle;

/**
 * Shows a wizard to create an invocation on a contract.
 */
public class InvokeForm extends Wrapper implements Disposable {
  private final Project project;
  private final SmartContract contract;

  private Wrapper panel;

  /**
   * Creates the invoke wizard component.
   *
   * @param project  neo-project
   * @param contract smart contract to show wizard
   */
  public InvokeForm(Project project, SmartContract contract) {
    this.project = project;
    this.contract = contract;
    this.panel = new Wrapper();
    setContent(getContent());
  }

  private JPanel getContent() {
    final var content = new JPanel();
    final var builder = new FormBuilder();

    List<ContractManifest.ContractABI.ContractMethod> methods;

    // operations
    try {
      methods = contract.getManifest().getAbi().getMethods();
    } catch (IOException e) {
      methods = new ArrayList<>();
      // todo, handle exception
    }
    var list = new JBList<>(methods);
    list.setCellRenderer(
        (methodList, contractMethod, i, b, b1) -> new JBLabel(contractMethod.getName()));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    if (!list.isEmpty()) {
      list.setSelectedIndex(0);
    }
    builder.addLabeledComponent(new JBLabel(NeoMessageBundle.message("contracts.invoke.operation")),
        list, true);

    // arguments
    return content;
  }

  @Override
  public void dispose() {
    panel = null;
  }
}
