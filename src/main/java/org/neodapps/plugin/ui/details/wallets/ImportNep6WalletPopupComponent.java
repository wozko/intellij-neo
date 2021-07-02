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
  private TextFieldWithBrowseButton nep6Path;
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

    // neo express binary location picker
    nep6Path = new TextFieldWithBrowseButton();
    var chooseBinary =
        FileChooserDescriptorFactory.createSingleFileDescriptor();
    nep6Path.addBrowseFolderListener(new TextBrowseFolderListener(chooseBinary));
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.import.wallet.nep6.file")), nep6Path,
        true);

    this.actionButton =
        new ToolWindowButton(NeoMessageBundle.message("toolwindow.wallet.create.prompt.action"),
            AllIcons.Actions.Execute,
            e -> {
              closePopup();
              project.getService(WalletService.class)
                  .addImportedWallet(nameField.getText(), Paths.get(nep6Path.getText()), chain);
            });

    builder.addComponent(actionButton);
    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(4, 10));
    return content;
  }

  @Override
  public void dispose() {
    if (popup != null && !popup.isDisposed()) {
      popup.dispose();
    }
    popup = null;
    nameField = null;
    actionButton = null;
  }
}
