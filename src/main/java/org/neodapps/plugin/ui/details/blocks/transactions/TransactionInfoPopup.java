/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.blocks.transactions;

import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.protocol.core.response.Transaction;
import java.util.Map;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;

/**
 * Represents transaction info popup.
 */
public class TransactionInfoPopup {
  private final Transaction transaction;
  private final Map<String, String> knownAddresses;

  public TransactionInfoPopup(Map<String, String> knownAddresses, Transaction transaction) {
    this.transaction = transaction;
    this.knownAddresses = knownAddresses;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    ComponentPopupBuilder builder = JBPopupFactory
        .getInstance().createComponentPopupBuilder(getContent(), null);
    builder
        .setTitle(NeoMessageBundle
            .message("transaction.info.popup.title", transaction.getHash().toString()));
    builder.setFocusable(true);
    builder.setMovable(true);
    builder.setResizable(true);
    builder.setRequestFocus(true);
    builder.setCancelOnOtherWindowOpen(false);
    builder.setCancelOnClickOutside(false);
    var popup = builder.createPopup();
    popup.showInFocusCenter();
  }

  private JPanel getContent() {
    FormBuilder builder = new FormBuilder();

    // add hash field
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("transaction.info.hash")),
        getTextField(transaction.getHash().toString()));

    // add sender
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("transaction.info.sender")),
        getTextField(
            knownAddresses.getOrDefault(transaction.getSender(), transaction.getSender())));

    // add size
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("transaction.info.size")),
        getTextField(String.format("%,d Bytes", transaction.getSize())));

    // add signer
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("transaction.info.nonce")),
        getTextField(transaction.getNonce().toString()));

    // add system fee
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("transaction.info.system.fee")),
        getTextField(transaction.getSysFee()));

    // add system fee
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("transaction.info.network.fee")),
        getTextField(transaction.getNetFee()));

    // add system fee
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("transaction.info.valid.until")),
        getTextField(transaction.getValidUntilBlock().toString()));

    // add script
    var script = new JBTextArea();
    script.setColumns(50);
    script.setText(transaction.getScript());
    script.setEditable(false);

    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("transaction.info.script")),
        script);


    var panel = JBUI.Panels.simplePanel();
    JPanel content = builder.getPanel();
    content.setBorder(JBUI.Borders.empty(0, 10));
    panel.add(new JBScrollPane(content));

    return panel;
  }

  private JBTextField getTextField(String content) {
    var field = new JBTextField(content);
    field.setEditable(false);
    return field;
  }
}
