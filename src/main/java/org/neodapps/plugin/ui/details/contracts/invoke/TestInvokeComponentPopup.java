package org.neodapps.plugin.ui.details.contracts.invoke;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.crypto.Base64;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.script.ScriptReader;
import io.neow3j.types.ContractParameter;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.util.List;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.services.chain.BlockchainService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows
 * invocation data on testing.
 */
public class TestInvokeComponentPopup implements Disposable {
  private final Project project;
  private final ChainLike chain;

  private final InvocationResult invocationResult;
  private final NeoGetContractState.ContractState contractState;
  private final ContractManifest.ContractABI.ContractMethod method;
  private final List<ContractParameter> parameters;
  private final NEP6Wallet wallet;

  private JBPopup popup;
  private ToolWindowButton actionButton;

  /**
   * Creates the component that shows
   * invocation data on testing.
   */
  public TestInvokeComponentPopup(Project project, ChainLike chain,
                                  InvocationResult result,
                                  NeoGetContractState.ContractState contractState,
                                  ContractManifest.ContractABI.ContractMethod method,
                                  List<ContractParameter> parameters, NEP6Wallet wallet) {
    this.project = project;
    this.chain = chain;
    this.invocationResult = result;
    this.contractState = contractState;
    this.method = method;
    this.parameters = parameters;
    this.wallet = wallet;
  }

  private void invokeStep() {
    var worker = new SwingWorker<NeoSendRawTransaction.RawTransaction, Void>() {

      @Override
      protected NeoSendRawTransaction.RawTransaction doInBackground() {
        return project.getService(BlockchainService.class)
            .invokeContractMethod(chain, contractState, method, parameters, wallet);
      }

      @Override
      protected void done() {
        NeoSendRawTransaction.RawTransaction transaction;
        try {
          transaction = get();
          if (transaction != null) {
            NeoNotifier.notifySuccess(project, NeoMessageBundle
                .message("contracts.invoke.submitted", transaction.getHash().toString()));
          }
        } catch (Exception e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };

    worker.execute();
  }

  private JComponent getComponent() {
    final var builder = new FormBuilder();
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("contracts.invoke.file.test.state")),
        getTextField(invocationResult.getState().toString()));

    if (invocationResult.getException() != null) {
      builder.addLabeledComponent(
          new JBLabel(NeoMessageBundle.message("contracts.invoke.file.test.exception")),
          getTextField(invocationResult.getException()));
    }

    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("contracts.invoke.file.test.gas")),
        getTextField(invocationResult.getGasConsumed()));

    var stackItems = invocationResult.getStack();
    if (stackItems.size() > 0) {
      builder.addLabeledComponent(
          new JBLabel(NeoMessageBundle.message("contracts.invoke.file.test.stack")),
          getStackItemsComponent(stackItems));
    }

    var scriptText = invocationResult.getScript();
    if (scriptText != null) {
      var script = new JBTextArea();
      script.setColumns(50);
      script.setText(ScriptReader.convertToOpCodeString(Base64.decode(scriptText)));
      script.setEditable(false);

      builder.addLabeledComponent(
          new JBLabel(NeoMessageBundle.message("contracts.invoke.file.test.script")),
          script);
    }

    // action
    this.actionButton =
        new ToolWindowButton(NeoMessageBundle.message("contracts.invoke.action"),
            AllIcons.Actions.Execute,
            e -> {
              closePopup();
              invokeStep();
            });
    builder.addComponent(actionButton);

    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(4, 10));
    return content;
  }

  private JPanel getStackItemsComponent(List<StackItem> items) {
    var itemListPanel = new JPanel();
    itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));

    for (StackItem item : items) {
      switch (item.getType()) {
        case BOOLEAN:
          itemListPanel.add(getTextField(String.valueOf(item.getBoolean())));
          break;
        case INTEGER:
          itemListPanel.add(getTextField(String.valueOf(item.getInteger())));
          break;
        case BYTE_STRING:
          itemListPanel.add(getTextField(item.getString()));
          break;
        case ARRAY:
          var panel = getStackItemsComponent(item.getList());
          panel.setBorder(JBUI.Borders.empty(0, 5));
          itemListPanel.add(panel);
          break;
        default:
          itemListPanel.add(getTextField(item.toString()));
      }
    }
    return itemListPanel;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    var builder = JBPopupFactory
        .getInstance().createComponentPopupBuilder(getComponent(), null);
    builder.setTitle(NeoMessageBundle.message("contracts.invoke.test.title"));
    builder.setFocusable(true);
    builder.setMovable(true);
    builder.setResizable(true);
    builder.setRequestFocus(true);
    builder.setCancelOnOtherWindowOpen(false);
    builder.setCancelOnClickOutside(false);
    builder.setCancelOnWindowDeactivation(false);
    popup = builder.createPopup();
    popup.showInCenterOf(
        Objects.requireNonNull(ToolWindowManager.getInstance(project).getToolWindow("Neo"))
            .getComponent());
  }

  /**
   * Closes the popup.
   */
  public void closePopup() {
    if (popup != null && !popup.isDisposed()) {
      popup.dispose();
      popup = null;
    }
  }

  private JBTextField getTextField(String content) {
    var field = new JBTextField(content);
    field.setEditable(false);
    return field;
  }

  @Override
  public void dispose() {
    popup = null;
    actionButton = null;
  }
}
