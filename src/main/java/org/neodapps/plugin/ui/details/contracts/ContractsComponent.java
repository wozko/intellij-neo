package org.neodapps.plugin.ui.details.contracts;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.ChainLike;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the component that shows contracts details.
 */
public class ContractsComponent extends Wrapper {

  final Project project;
  final Wrapper walletComponent;

  /**
   * Creates the component that shows contracts details.
   *
   * @param project intellij project
   * @param chain   selected chain
   */
  public ContractsComponent(Project project, ChainLike chain) {
    this.project = project;
    walletComponent = new Wrapper();

    var panel = JBUI.Panels.simplePanel();
    panel.addToTop(getToolBar(chain));
    panel.addToCenter(walletComponent);

    walletComponent.setContent(new JPanel());
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

  private Path browseForNefFile() {
    final FileChooserDescriptor descriptor =
        FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();
    final VirtualFile toSelect =
        LocalFileSystem.getInstance().findFileByPath(Objects.requireNonNull(project.getBasePath()));
    descriptor.setTitle(NeoMessageBundle.message("contracts.deploy.pick.file"));
    VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, toSelect);
    if (virtualFile != null) {
      return virtualFile.toNioPath();
    }
    return null;
  }

  private void deployContract(Path nefFile) {
    System.out.println(nefFile.getFileName());
  }
}
