/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window;

import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Represents button used throughout tool window.
 */
public class ToolWindowButton extends JButton {
  /**
   * Creates button used throughout tool window.
   *
   * @param text button content
   * @param icon button icon
   */
  public ToolWindowButton(String text, Icon icon) {
    super(text);
    setIcon(icon);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        super.mouseEntered(e);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        super.mouseExited(e);
      }
    });
  }

  public ToolWindowButton(String text, Icon icon, ActionListener onClick) {
    this(text, icon);
    addActionListener(onClick);
  }
}
