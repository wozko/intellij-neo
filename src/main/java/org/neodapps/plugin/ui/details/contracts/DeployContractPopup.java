package org.neodapps.plugin.ui.details.contracts;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.wallet.Wallet;
import java.util.List;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the deploy contract popup.
 */
public class DeployContractPopup implements Disposable {
  private final Project project;
  private final List<Wallet> wallets;

  private JBPopup popup;
  private TextFieldWithBrowseButton nefPath;
  private JBLabel nefError;
  private TextFieldWithBrowseButton manifestPath;
  private JBLabel manifestError;
  private ToolWindowButton actionButton;
  private Wallet selectedWallet;

  /**
   * Creates the deploy contract popup.
   */
  public DeployContractPopup(Project project, List<Wallet> wallets) {
    this.project = project;
    this.wallets = wallets;
  }

  private JPanel getComponent() {
    final var builder = new FormBuilder();

    // neo express binary location picker
    nefPath = new TextFieldWithBrowseButton();
    var chooseBinary =
        FileChooserDescriptorFactory.createSingleFileDescriptor("nef");
    chooseBinary.setRoots(
        LocalFileSystem.getInstance()
            .findFileByPath(Objects.requireNonNull(project.getBasePath())));
    nefPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseBinary));
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("contracts.deploy.pick.file")), nefPath, true);

    nefError = new JBLabel(NeoMessageBundle.message("contracts.deploy.pick.file.error"));
    nefError.setVisible(false);
    builder.addComponent(nefError);


    manifestPath = new TextFieldWithBrowseButton();
    var chooseManifest =
        FileChooserDescriptorFactory.createSingleFileDescriptor("json");
    chooseBinary.setRoots(
        LocalFileSystem.getInstance()
            .findFileByPath(Objects.requireNonNull(project.getBasePath())));
    manifestPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseManifest));
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("contracts.deploy.pick.manifest.file")), manifestPath,
        true);

    manifestError =
        new JBLabel(NeoMessageBundle.message("contracts.deploy.pick.manifest.file.error"));
    manifestError.setVisible(false);
    builder.addComponent(manifestError);


    // wallet list
    JBList<Wallet> walletList = new JBList<>(wallets);
    walletList.setCellRenderer((walletList1, wallet, i, b, b1) -> new JBLabel(wallet.getName()));

    // selection listener
    walletList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    walletList.addListSelectionListener(e -> {
      selectedWallet = walletList.getSelectedValue();
    });
    builder
        .addLabeledComponent(NeoMessageBundle.message("contracts.deploy.select.wallet"), walletList,
            true);

    // deploy button
    actionButton =
        new ToolWindowButton(NeoMessageBundle.message("contracts.deploy.action"),
            AllIcons.Actions.Execute,
            e -> {
              var nefPathVal = nefPath.getText();
              var manifestPathVal = manifestPath.getText();
              if (nefPathVal.isEmpty()) {
                nefError.setVisible(true);
              } else if (manifestPathVal.isEmpty()) {
                manifestError.setVisible(true);
              } else {
                closePopup();
                // todo: deploy here
              }
            });
    builder.addComponent(actionButton);

    if (walletList.isEmpty()) {
      actionButton.setEnabled(false);
      builder
          .addComponent(new JBLabel(NeoMessageBundle.message("contracts.deploy.no.wallet")));
    } else {
      // set the default value
      walletList.setSelectedIndex(0);
      selectedWallet = walletList.getSelectedValue();
    }


    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(4, 10));

    return content;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    var builder = JBPopupFactory
        .getInstance().createComponentPopupBuilder(getComponent(), nefPath);
    builder.setTitle(NeoMessageBundle.message("contracts.deploy"));
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


  @Override
  public void dispose() {
    if (popup != null && !popup.isDisposed()) {
      popup.dispose();
    }
    popup = null;
    nefPath = null;
    manifestPath = null;
    actionButton = null;
  }
}
