/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.wallets;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import java.util.Objects;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.neodapps.plugin.NeoMessageBundle;
import org.neodapps.plugin.blockchain.PrivateChain;
import org.neodapps.plugin.services.express.NeoExpressService;
import org.neodapps.plugin.ui.ToolWindowButton;

/**
 * Represents the create wallet popup component.
 */
public class CreateWalletPopupComponent implements Disposable {

  private final Project project;
  private final PrivateChain chain;
  private final String[]
      randomNames =
      new String[] {"Quail", "Joshua", "Nolan", "Ali", "Rahim", "Alea", "Bert", "Colleen", "Keelie",
          "Arthur", "Blaze", "Kibo", "Mary", "Jameson", "Isadora", "Mallory", "Lars", "Harriet",
          "Gage", "Felicia", "Genevieve", "Marsden", "Doris", "Belle", "Buffy", "Hiroko", "Melvin",
          "Priscilla", "Kelly", "Glenna", "Otto", "Magee", "Daquan", "Ruby", "Nita", "Brynn",
          "Willow", "Amity", "Macey", "Pearl", "Fletcher", "Marshall", "Rinah", "Emery", "Carolyn",
          "Quinn", "MacKensie", "Phelan", "Britanni", "Xenos", "Athena", "Sonya", "Kylee",
          "Constance", "Quamar", "Christine", "Ivan", "Nigel", "Rina", "Maile", "Chava", "Giselle",
          "Nadine", "Haley", "Quinn", "Zelda", "Wallace", "Reese", "Gretchen", "Janna", "Iliana",
          "Lucas", "Jessamine", "Nehru", "Igor", "Raymond", "Logan", "Linus", "Dominique",
          "Tallulah", "Eden", "Iliana", "Maisie", "Jesse", "Hu", "Kirk", "Francis", "Lana",
          "MacKensie", "Dara", "Rudyard", "Ryder", "Barrett", "Keely", "Flynn", "Noelle", "Nigel",
          "Leah", "Jordan", "Bert"};
  private JBPopup popup;
  private JTextField nameField;
  private ToolWindowButton actionButton;

  public CreateWalletPopupComponent(Project project, PrivateChain chain) {
    this.project = project;
    this.chain = chain;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    var builder = JBPopupFactory
        .getInstance().createComponentPopupBuilder(getComponent(), nameField);
    builder.setTitle(NeoMessageBundle.message("toolwindow.wallet.create.prompt.title"));
    builder.setFocusable(true);
    builder.setMovable(true);
    builder.setResizable(true);
    builder.setRequestFocus(true);
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
    this.nameField = new JTextField(randomNames[new Random().nextInt(100)]);
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("toolwindow.wallet.create.prompt.name")), nameField,
        true);

    this.actionButton =
        new ToolWindowButton(NeoMessageBundle.message("toolwindow.wallet.create.prompt.action"),
            AllIcons.Actions.Execute,
            e -> {
              closePopup();
              project.getService(NeoExpressService.class)
                  .createWallet(nameField.getText(), chain);
            });

    builder.addComponent(actionButton);
    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(0, 10));
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
