/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window;

import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.core.response.NeoWitness;
import java.awt.GridLayout;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.neodapps.plugin.NeoMessageBundle;

/**
 * Represents block info popup.
 */
public class BlockItemPopup {

  final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm a");
  private final NeoBlock block;

  public BlockItemPopup(NeoBlock block) {
    this.block = block;
  }

  /**
   * Shows the popup.
   */
  public void showPopup() {
    ComponentPopupBuilder builder = JBPopupFactory
        .getInstance().createComponentPopupBuilder(getContent(block), null);
    builder
        .setTitle(NeoMessageBundle.message("block.info.popup.title", block.getHash().toString()));
    builder.setFocusable(true);
    builder.setMovable(true);
    builder.setResizable(true);
    builder.setRequestFocus(true);
    builder.setCancelOnOtherWindowOpen(false);
    builder.setCancelOnClickOutside(false);
    var popup = builder.createPopup();
    popup.showInFocusCenter();
  }

  private JPanel getContent(NeoBlock block) {
    FormBuilder builder = new FormBuilder();

    // add index field
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("block.info.index.label")),
        getTextField(String.format("%,d", block.getIndex())));

    // add time field
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("block.info.time.label")),
        getTextField(sdf.format(new Timestamp(block.getTime()).getTime())));

    // add block hash
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("block.info.hash.label")),
        getTextField(block.getHash().toString()));

    // add size
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("block.info.size.label")),
        getTextField(String.format("%,d Bytes", block.getSize())));

    // add merkle root
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("block.info.merkle.root.label")),
        getTextField(block.getMerkleRootHash().toString()));

    // add next consensus root
    builder.addLabeledComponent(
        new JBLabel(NeoMessageBundle.message("block.info.next.consensus.label")),
        getTextField(block.getNextConsensus()));

    // add witness data
    var witnesses = block.getWitnesses();
    if (witnesses.size() > 0) {
      builder.addLabeledComponent(new JBLabel(NeoMessageBundle.message("block.info.witness.label")),
          getWitnessData(witnesses));
    }


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

  private JComponent getWitnessData(List<NeoWitness> witnesses) {
    var witnessPanel = JBUI.Panels.simplePanel();
    witnessPanel.setBorder(JBUI.Borders.customLine(JBColor.border()));

    witnessPanel.setLayout(new GridLayout(witnesses.size(), 1));

    for (NeoWitness witness : witnesses) {
      //  invocation script
      var invocationScript = new JBTextArea();
      invocationScript.setColumns(50);
      invocationScript.setText(witness.getInvocation());
      invocationScript.setEditable(false);

      //  verification script
      var verificationScript = new JBTextArea();
      verificationScript.setColumns(50);
      verificationScript.setText(witness.getVerification());
      verificationScript.setEditable(false);

      var builder = new FormBuilder();
      builder.addLabeledComponent(
          new JBLabel(NeoMessageBundle.message("block.info.witness.invocation.label")),
          new JBScrollPane(invocationScript),
          true);
      builder.addLabeledComponent(
          new JBLabel(NeoMessageBundle.message("block.info.witness.verification.label")),
          new JBScrollPane(verificationScript),
          true);
      var content = builder.getPanel();
      content.setBorder(JBUI.Borders.empty(2, 5));
      witnessPanel.add(content);
    }
    return witnessPanel;
  }
}
