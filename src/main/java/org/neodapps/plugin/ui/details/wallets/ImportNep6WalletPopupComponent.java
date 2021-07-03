/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.wallets;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import java.nio.file.Paths;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.Chain;
import org.neodapps.plugin.services.chain.WalletService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the import wallet popup component.
 */
public class ImportNep6WalletPopupComponent implements Disposable {
  private final Project project;
  private final Chain chain;

  private JBPopup popup;
  private JTextField nameField;
  private JBLabel nameFieldError;

  private TextFieldWithBrowseButton nep6Path;
  private JBLabel nep6PathError;

  private ToolWindowButton actionButton;

  public ImportNep6WalletPopupComponent(Project project, Chain chain) {
    this.project = project;
    this.chain = chain;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    var builder = JBPopupFactory
        .getInstance().createComponentPopupBuilder(getComponent(), nameField);
    builder.setTitle(NeoMessageBundle.message("toolwindow.wallet.import.prompt.title"));
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


  private JPanel getComponent() {
    var builder = new FormBuilder();
    this.nameField =
        new JTextField();
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.wallet.import.prompt.name")), nameField,
        true);

    this.nameFieldError =
        new JBLabel(NeoMessageBundle.message("toolwindow.wallet.import.prompt.name.error"));
    nameFieldError.setVisible(false);
    builder.addComponent(nameFieldError);

    nep6Path = new TextFieldWithBrowseButton();
    var chooseManifest =
        FileChooserDescriptorFactory.createSingleFileDescriptor();
    nep6Path.addBrowseFolderListener(new TextBrowseFolderListener(chooseManifest));
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.import.wallet.nep6.file")), nep6Path,
        true);

    nep6PathError =
        new JBLabel(NeoMessageBundle.message("contracts.deploy.pick.manifest.file.error"));
    nep6PathError.setVisible(false);
    builder.addComponent(nep6PathError);

    this.actionButton =
        new ToolWindowButton(NeoMessageBundle.message("toolwindow.wallet.create.prompt.action"),
            AllIcons.Actions.Execute,
            e -> {
              var path = nep6Path.getText();
              var name = nameField.getText();
              if (path.isEmpty()) {
                nep6PathError.setEnabled(true);
              } else if (name.isEmpty()) {
                nameField.setEnabled(true);
              } else {
                closePopup();
                transferAsset(name, path, chain);
              }
            });

    builder.addComponent(actionButton);
    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(4, 10));
    return content;
  }

  private void transferAsset(String name, String path, Chain chain) {
    var worker = new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
        project.getService(WalletService.class)
            .addImportedWallet(name, Paths.get(path), chain);
        return null;
      }
    };
    worker.execute();
  }

  @Override
  public void dispose() {
    if (popup != null && !popup.isDisposed()) {
      popup.dispose();
    }
    popup = null;
    nameField = null;
    nameFieldError = null;
    actionButton = null;
  }
}
