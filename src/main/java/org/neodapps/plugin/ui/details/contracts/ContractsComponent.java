/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.contracts;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.wallet.nep6.NEP6Wallet;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.NeoNotifier;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.services.chain.ContractServices;
import org.neodapps.plugin.services.chain.InvokeFile;
import org.neodapps.plugin.services.chain.WalletService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows contracts details.
 */
public class ContractsComponent extends Wrapper implements Disposable {

  final Project project;
  final ChainLike chain;

  Wrapper contractsWrapper;
  Wrapper toolbarWrapper;
  Wrapper invokeFileWrapper;

  /**
   * Creates the component that shows contracts details.
   *
   * @param project intellij project
   * @param chain   selected chain
   */
  public ContractsComponent(Project project, ChainLike chain) {
    this.project = project;
    this.chain = chain;

    this.toolbarWrapper = new Wrapper();
    this.contractsWrapper = new Wrapper();
    this.invokeFileWrapper = new Wrapper();

    var panel = JBUI.Panels.simplePanel();
    panel.addToTop(toolbarWrapper);
    panel.addToCenter(contractsWrapper);
    panel.addToBottom(invokeFileWrapper);

    toolbarWrapper.setContent(getLoadingComponent());
    contractsWrapper.setContent(getLoadingComponent());
    invokeFileWrapper.setContent(new JPanel());
    setContent(panel);

    loadAndSetContent();
  }

  private void loadAndSetContent() {
    var worker =
        new SwingWorker<Pair<List<NEP6Wallet>, List<NeoGetContractState.ContractState>>, Void>() {
          @Override
          protected Pair<List<NEP6Wallet>,
              List<NeoGetContractState.ContractState>> doInBackground() {
            return new Pair<>(project.getService(WalletService.class).getWallets(chain),
                project.getService(ContractServices.class).getContracts(chain));
          }

          @Override
          protected void done() {
            try {
              var pair = get();
              var wallets = pair.getFirst();
              var contracts = pair.getSecond();

              toolbarWrapper.setContent(getToolBar(wallets, contracts));
              contractsWrapper.setContent(getContractContent(contracts));
            } catch (InterruptedException | ExecutionException e) {
              NeoNotifier.notifyError(project, e.getMessage());
            }
          }
        };
    worker.execute();
  }

  private JComponent getContractContent(List<NeoGetContractState.ContractState> contracts) {
    final var panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    contracts.forEach(contractState -> panel.add(getContractComponent(contractState)));

    return new JBScrollPane(panel);
  }

  private JComponent getLoadingComponent() {
    var panel = new JPanel();
    panel.setBorder(JBUI.Borders.empty(5));
    panel.add(new JBLabel(NeoMessageBundle.message("toolwindow.loading")));
    return panel;
  }

  private JComponent getToolBar(List<NEP6Wallet> wallets,
                                List<NeoGetContractState.ContractState> contracts) {
    // toolbar has two buttons
    var buttonPanel = JBUI.Panels.simplePanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    // create wallet
    var deployContractButton =
        new ToolWindowButton(
            NeoMessageBundle.message("contracts.deploy"),
            AllIcons.Modules.AddExcludedRoot, actionEvent -> {
          var popup = new DeployContractPopup(project, chain, wallets);
          popup.showPopup();
        });
    buttonPanel.add(deployContractButton);

    // refresh
    var refreshButton =
        new ToolWindowButton("",
            AllIcons.Javaee.UpdateRunningApplication,
            actionEvent -> {
              toolbarWrapper.setContent(getLoadingComponent());
              contractsWrapper.setContent(getLoadingComponent());
              loadAndSetContent();
            });
    buttonPanel.add(refreshButton);

    // open invoke file
    var openInvokeFile =
        new ToolWindowButton(
            NeoMessageBundle.message("contracts.invoke"),
            AllIcons.Actions.Lightning, actionEvent -> {
          var chooseJson =
              FileChooserDescriptorFactory.createSingleFileDescriptor("json");
          chooseJson.setRoots(
              LocalFileSystem.getInstance()
                  .findFileByPath(Objects.requireNonNull(project.getBasePath())));
          var file = FileChooser.chooseFile(chooseJson, project, null);
          openInvokeFile(file, wallets, contracts);
        });
    buttonPanel.add(openInvokeFile);

    return buttonPanel;
  }

  private JComponent getContractComponent(NeoGetContractState.ContractState contractState) {
    final var panel = new JPanel();
    panel.setBorder(
        new CompoundBorder(JBUI.Borders.customLine(JBColor.border()), JBUI.Borders.empty(5, 2)));

    var manifest = contractState.getManifest();
    panel.add(new JBLabel(manifest.getName()));

    return panel;
  }

  private void openInvokeFile(VirtualFile file, List<NEP6Wallet> wallets,
                              List<NeoGetContractState.ContractState> contracts) {
    var worker = new SwingWorker<InvokeFile, Void>() {
      @Override
      protected InvokeFile doInBackground() {
        InvokeFile invokeFile = null;
        try {
          invokeFile = new InvokeFile(file.toNioPath());
        } catch (IOException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
        return invokeFile;
      }

      @Override
      protected void done() {
        try {
          var invokeFile = get();
          invokeFileWrapper
              .setContent(new InvokeFileComponent(project, invokeFile, wallets, contracts));
        } catch (InterruptedException | ExecutionException e) {
          NeoNotifier.notifyError(project, e.getMessage());
        }
      }
    };
    worker.execute();
  }

  @Override
  public void dispose() {
    contractsWrapper = null;
    toolbarWrapper = null;
  }
}
