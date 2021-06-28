/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.util.ui.FormBuilder;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.NeoMessageBundle;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class SettingsComponent implements Disposable {
  private JBLoadingPanel settingsPanel;
  private TextFieldWithBrowseButton dotNetRoot;
  private TextFieldWithBrowseButton neoExpressExecutablePath;

  /**
   * Returns the setting component panel.
   *
   * @return Returns a {@link JPanel} with fields to save plugin settings.
   */
  public JPanel getPanel() {
    settingsPanel = new JBLoadingPanel(new BorderLayout(), this, 300);

    // dot net location picker
    dotNetRoot = new TextFieldWithBrowseButton();
    var chooseDirectoryDescriptor =
        FileChooserDescriptorFactory.createSingleFolderDescriptor();
    chooseDirectoryDescriptor.setShowFileSystemRoots(true);
    chooseDirectoryDescriptor.withShowHiddenFiles(true);
    dotNetRoot.addBrowseFolderListener(new TextBrowseFolderListener(chooseDirectoryDescriptor));

    // neo express binary location picker
    neoExpressExecutablePath = new TextFieldWithBrowseButton();
    var chooseBinary =
        FileChooserDescriptorFactory.createSingleFileDescriptor();
    chooseBinary.setShowFileSystemRoots(true);
    chooseBinary.withShowHiddenFiles(true);
    neoExpressExecutablePath
        .addBrowseFolderListener(new TextBrowseFolderListener(chooseBinary));

    // create panel content
    var content = FormBuilder.createFormBuilder()
        .addLabeledComponent(new JBLabel(NeoMessageBundle.message("settings.dotnet.picker")),
            dotNetRoot, 1, false)
        .addLabeledComponent(new JBLabel(NeoMessageBundle.message("settings.neo.picker")),
            neoExpressExecutablePath, 1, false)
        .addComponent(new JBLabel(NeoMessageBundle.message("settings.neo.hint")))
        .getPanel();

    settingsPanel.add(content, BorderLayout.NORTH);
    return settingsPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return neoExpressExecutablePath;
  }

  @NotNull
  public String getDotNetRootPath() {
    return dotNetRoot.getText();
  }

  public void setDotNetRootPath(@NotNull String path) {
    dotNetRoot.setText(path);
  }

  @NotNull
  public String getNeoExpressExecutablePath() {
    return neoExpressExecutablePath.getText();
  }

  public void setNeoExpressExecutablePath(@NotNull String path) {
    neoExpressExecutablePath.setText(path);
  }

  @Override
  public void dispose() {
    settingsPanel = null;
    dotNetRoot = null;
    neoExpressExecutablePath = null;
  }
}
