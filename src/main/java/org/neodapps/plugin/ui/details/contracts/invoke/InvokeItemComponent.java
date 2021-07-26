package org.neodapps.plugin.ui.details.contracts.invoke;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.services.chain.InvokeFileItem;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows a step in invoke file.
 */
public class InvokeItemComponent extends Wrapper implements Disposable {
  private final Project project;
  private final ChainLike chain;
  private final InvokeFileItem item;
  private final List<NeoGetContractState.ContractState> deployedContracts;
  private final List<NEP6Wallet> availableWallets;
  private final InvokeItemAction invokeItemAction;

  private Wrapper contentWrapper;
  private ComboBox<String> contractNameComboBox;
  private JBLabel contractNameError;
  private ComboBox<String> operationNamesComboBox;
  private JBLabel operationNameError;
  private Wrapper argsPanel;
  private ToolWindowButton runStepButton;

  private NeoGetContractState.ContractState selectedContract;
  private ContractManifest.ContractABI.ContractMethod selectedOperation;
  private List<ArgumentField> argumentFields;

  /**
   * Creates the component that shows a step in invoke file.
   */
  public InvokeItemComponent(Project project,
                             ChainLike chain,
                             InvokeFileItem item,
                             InvokeItemAction invokeItemAction,
                             List<NeoGetContractState.ContractState> deployedContracts,
                             List<NEP6Wallet> availableWallets) {
    this.project = project;
    this.chain = chain;
    this.item = item;
    this.invokeItemAction = invokeItemAction;
    this.deployedContracts = deployedContracts;
    this.availableWallets = availableWallets;

    this.contentWrapper = new Wrapper();
    this.contentWrapper.setBorder(
        new CompoundBorder(JBUI.Borders.empty(5), JBUI.Borders.customLine(JBColor.border())));

    contentWrapper.setContent(getItemComponent());
    setContent(contentWrapper);

    // set select values
    contractNameComboBox.setSelectedItem(item.getContract());
    operationNamesComboBox.setSelectedItem(item.getOperation());
  }

  private JPanel getItemComponent() {
    initializeComponents();
    fillComboBoxSuggestions();

    var builder = new FormBuilder();
    var contractNameTitle = new JPanel(new FlowLayout(FlowLayout.LEADING));
    contractNameTitle.add(new JBLabel(NeoMessageBundle.message("contracts.invoke.file.contract")));
    contractNameTitle.add(this.contractNameError);
    builder.addLabeledComponent(contractNameTitle, this.contractNameComboBox, true);

    var operationNameTitle = new JPanel(new FlowLayout(FlowLayout.LEADING));
    operationNameTitle
        .add(new JBLabel(NeoMessageBundle.message("contracts.invoke.file.operation")));
    operationNameTitle.add(this.operationNameError);
    builder.addLabeledComponent(operationNameTitle, this.operationNamesComboBox, true);

    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("contracts.invoke.file.arguments")),
        argsPanel, true);

    // actions
    var actionsPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    // delete button
    var deleteButton =
        new ToolWindowButton(NeoMessageBundle.message("contracts.invoke.file.delete.step"),
            AllIcons.Hierarchy.MethodNotDefined, e -> invokeItemAction.deleteInvokeItem());
    actionsPanel.add(deleteButton);
    actionsPanel.add(runStepButton);
    builder.addComponent(actionsPanel);
    return builder.getPanel();
  }

  private void initializeComponents() {
    this.argsPanel = new Wrapper();

    // initialize contract combo box and error
    this.contractNameComboBox = new ComboBox<>();
    this.contractNameError =
        new JBLabel(NeoMessageBundle.message("contracts.invoke.file.contract.not.found"));
    contractNameError.setForeground(JBColor.red);


    // initialize operation contract box and error
    this.operationNamesComboBox = new ComboBox<>();
    this.operationNameError =
        new JBLabel(NeoMessageBundle.message("contracts.invoke.file.contract.method.not.found"));
    operationNameError.setForeground(JBColor.red);

    // run step button
    runStepButton =
        new ToolWindowButton(NeoMessageBundle.message("contracts.invoke.file.run.step"),
            AllIcons.Duplicates.SendToTheRight, e -> {
          var popup = new InvokeItemRunPopup(project, chain, availableWallets, selectedContract,
              selectedOperation,
              argumentFields.stream().map(ArgumentField::getParam).collect(Collectors.toList()));
          popup.showPopup();
        });

  }

  private void fillComboBoxSuggestions() {
    this.contractNameComboBox.removeAllItems();
    this.operationNamesComboBox.removeAllItems();

    // setup contract combo box
    var contractNameList =
        this.deployedContracts.stream().map(c -> c.getManifest().getName()).collect(
            Collectors.toList());
    for (String name : contractNameList) {
      contractNameComboBox.addItem(name);
    }

    contractNameComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        this.argsPanel.setContent(new JPanel());
        this.operationNamesComboBox.removeAllItems();
        var selectedContractOption = this.deployedContracts.stream()
            .filter(c -> c.getManifest().getName().equals(contractNameComboBox.getSelectedItem()))
            .collect(Collectors.toList());

        if (selectedContractOption.isEmpty()) {
          showContractNameError(true);
          selectedContract = null;
        } else {
          showContractNameError(false);
          selectedContract = selectedContractOption.get(selectedContractOption.size() - 1);
          var methods = selectedContract.getManifest()
              .getAbi()
              .getMethods();

          if (methods.size() > 0) {
            for (ContractManifest.ContractABI.ContractMethod method : methods) {
              operationNamesComboBox.addItem(method.getName());
            }
            operationNamesComboBox.setSelectedIndex(0);
            selectedOperation = methods.get(0);
          }
        }
        item.setContract((String) contractNameComboBox.getSelectedItem());

        // mark as modified
        invokeItemAction.markInvokeItemModified();
      }
    });
    contractNameComboBox.setEditable(true);

    operationNamesComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        this.argsPanel.setContent(new JPanel());
        if (selectedContract != null) {
          var selectedOperationOption =
              selectedContract.getManifest().getAbi().getMethods().stream()
                  .filter(m -> m.getName().equals(operationNamesComboBox.getSelectedItem()))
                  .findAny();
          if (selectedOperationOption.isEmpty()) {
            showOperationNameError(true);
            selectedOperation = null;
          } else {
            showOperationNameError(false);
            selectedOperation = selectedOperationOption.get();
          }
        } else {
          // already showing error for contract name field
          showOperationNameError(false);
        }
        item.setOperation(operationNamesComboBox.getItem());

        // mark as modified
        invokeItemAction.markInvokeItemModified();

        // when operation selection change
        // change the arguments accordingly
        setArguments();
      }
    });
    operationNamesComboBox.setEditable(true);
  }

  private void setArguments() {
    argumentFields = new ArrayList<>();
    var argumentBuilder = new FormBuilder();
    var args = item.getArgs();
    if (selectedOperation == null) {
      // if contract and method not found in deployed
      // just list the arguments
      for (int i = 0; i < args.size(); i++) {
        var arg = args.get(i);
        argumentBuilder.addLabeledComponent(
            new JBLabel(NeoMessageBundle.message("contracts.invoke.file.argument", i + 1)),
            new JBTextField(arg.toString()));
      }
      setRunnable(false);
    } else {
      // if operation found list param names with values
      var params = selectedOperation.getParameters();
      for (int i = 0; i < params.size(); i++) {
        var parameter = params.get(i);
        var argParam = new ArgumentField(parameter.getParamName(), parameter.getParamType());
        argumentFields.add(argParam);
        var value = "";
        if (i < args.size()) {
          value = args.get(i).toString();
        }
        argParam.setText(value);
        argumentBuilder
            .addLabeledComponent(new JBLabel(parameter.getParamName()), argParam);
      }
      setRunnable(true);
    }
    var content = argumentBuilder.getPanel();
    content.setBorder(JBUI.Borders.empty(2, 5));
    argsPanel.setContent(content);
  }

  private void setRunnable(boolean runnable) {
    runStepButton.setEnabled(runnable);
  }

  private void showContractNameError(boolean show) {
    this.contractNameError.setVisible(show);
  }

  private void showOperationNameError(boolean show) {
    this.operationNameError.setVisible(show);
  }

  @Override
  public void dispose() {
    this.contentWrapper = null;
    this.contractNameComboBox = null;
    this.contractNameError = null;
    this.operationNamesComboBox = null;
    this.operationNameError = null;
    this.argsPanel = null;
    this.argumentFields = null;
  }

  static class ArgumentField extends JBTextField {
    private final ContractParameterType type;

    ArgumentField(String name, ContractParameterType type) {
      this.type = type;
    }

    public ContractParameter getParam() {
      var value = getText();
      try {
        switch (type) {
          case BOOLEAN:
            return ContractParameter.bool(BooleanUtils.toBoolean(value));
          case INTEGER:
            return ContractParameter.integer(NumberUtils.toInt(value));
          case BYTE_ARRAY:
            return ContractParameter.byteArray(value.getBytes());
          case STRING:
            return ContractParameter.string(value);
          case HASH160:
            return ContractParameter.hash160(Hash160.fromAddress(value));
          case HASH256:
            // need to specify hash string
            return ContractParameter.hash256(value);
          case PUBLIC_KEY:
            return ContractParameter.publicKey(value);
          case SIGNATURE:
            // signature hash string
            return ContractParameter.signature(value);
          case ANY:
            return ContractParameter.any(value);
          case ARRAY:
          case MAP:
          case INTEROP_INTERFACE:
          case VOID:
            // TODO: implement these
            break;
          default:
            break;
        }
      } catch (Exception e) {
        // conversation exception
        // notify
        return ContractParameter.any(value);
      }
      return ContractParameter.any(value);
    }
  }
}
