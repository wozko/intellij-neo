/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.contracts.invoke;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.services.chain.InvokeFile;
import org.neodapps.plugin.services.chain.InvokeFileItem;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows an invoke file.
 */
public class InvokeFileComponent extends Wrapper implements Disposable {
  private final Project project;
  private final ChainLike chain;
  private final InvokeFile invokeFile;
  private final List<NEP6Wallet> wallets;
  private final List<NeoGetContractState.ContractState> contracts;

  private Wrapper mainPanel;
  private Wrapper itemListWrapper;
  private JPanel itemListPanel;

  private ToolWindowButton saveButton;
  private ToolWindowButton addStepButton;

  private Map<String, InvokeItemComponent> invokeItemComponentMap;

  /**
   * Creates the component that shows an invoke file.
   *
   * @param project    intellij project
   * @param invokeFile invoke file representation
   */
  public InvokeFileComponent(Project project,
                             ChainLike chain,
                             InvokeFile invokeFile,
                             List<NEP6Wallet> wallets,
                             List<NeoGetContractState.ContractState> contractList) {
    this.project = project;
    this.chain = chain;
    this.invokeFile = invokeFile;
    this.wallets = wallets;
    this.contracts = contractList;

    this.invokeItemComponentMap = new HashMap<>();
    this.itemListWrapper = new Wrapper();
    this.itemListWrapper.setBorder(JBUI.Borders.customLine(JBColor.border()));
    this.itemListWrapper.setContent(getLoadingComponent());
    this.itemListPanel = new JPanel();
    itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));

    this.mainPanel = new Wrapper();
    this.mainPanel.setContent(this.itemListWrapper);
    setContent(this.mainPanel);
    setItems();
  }

  private void setItems() {
    var worker = new SwingWorker<Map<String, InvokeFileItem>, Void>() {
      @Override
      protected Map<String, InvokeFileItem> doInBackground() {
        Map<String, InvokeFileItem> items;
        try {
          items = invokeFile.getItems();
          return items;
        } catch (IOException e) {
          NeoNotifier.notifyError(project, e.getMessage());
          return new HashMap<>();
        }
      }

      @Override
      protected void done() {
        Map<String, InvokeFileItem> items;
        try {
          items = get();
          itemListWrapper.setContent(getInvokeItemListComponent(invokeFile, items));
        } catch (InterruptedException | ExecutionException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };
    worker.execute();
  }

  private JPanel getInvokeItemListComponent(InvokeFile file, Map<String, InvokeFileItem> items) {
    final var panel = JBUI.Panels.simplePanel();

    // title data like filename + save button
    var titlePanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    panel.addToTop(titlePanel);
    panel.addToCenter(itemListPanel);

    var titleField = new JBTextField(file.getPath().getFileName().toString());
    titleField.setEditable(false);
    titlePanel.add(titleField);

    // save changes
    saveButton =
        new ToolWindowButton(NeoMessageBundle.message("contracts.invoke.save"),
            AllIcons.Actions.MenuSaveall, e -> saveFileChanges());
    titlePanel.add(saveButton);

    // add a new step
    addStepButton = new ToolWindowButton(NeoMessageBundle.message("contracts.invoke.add"),
        AllIcons.General.Add, e -> addStep());
    titlePanel.add(addStepButton);

    items.forEach((id, item) -> {
      addInvokeItemToPanel(item);
    });
    return panel;
  }

  private void addInvokeItemToPanel(InvokeFileItem item) {
    var itemComponent = new InvokeItemComponent(project, chain, item, new InvokeItemAction() {
      @Override
      public void markInvokeItemModified() {
        markModified();
      }

      @Override
      public void deleteInvokeItem() {
        deleteStep(item);
      }
    }, contracts, wallets);
    invokeItemComponentMap.put(item.getId(), itemComponent);
    itemListPanel.add(itemComponent);
  }

  private JPanel getLoadingComponent() {
    var panel = new JPanel();
    panel.setBorder(JBUI.Borders.empty(5));
    panel.add(new JBLabel(NeoMessageBundle.message("toolwindow.loading")));
    return panel;
  }

  private void saveFileChanges() {
    saveButton.setEnabled(false);
    var worker = new SwingWorker<Boolean, Void>() {
      @Override
      protected Boolean doInBackground() {
        try {
          invokeFile.saveChanges();
          return true;
        } catch (IOException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
        return false;
      }

      @Override
      protected void done() {
        saveButton.setIcon(AllIcons.Actions.MenuSaveall);
        saveButton.setEnabled(true);
      }
    };
    worker.execute();
  }

  private void addStep() {
    var newItem = new InvokeFileItem();
    // add to pojo
    this.invokeFile.addItem(newItem);
    // add to component
    addInvokeItemToPanel(newItem);
    markModified();
  }

  private void deleteStep(InvokeFileItem item) {
    // delete from file
    this.invokeFile.removeItem(item);

    // remove from ui
    var id = item.getId();
    var panelToRemove = invokeItemComponentMap.get(id);
    if (panelToRemove != null) {
      itemListPanel.remove(panelToRemove);
      invokeItemComponentMap.remove(id);
    }
  }

  private void markModified() {
    this.saveButton.setIcon(AllIcons.Ide.ErrorPoint);
  }

  @Override
  public void dispose() {
    this.mainPanel = null;
    this.itemListWrapper = null;
    this.itemListPanel = null;
    this.invokeItemComponentMap = null;
    this.saveButton = null;
    this.addStepButton = null;
  }
}