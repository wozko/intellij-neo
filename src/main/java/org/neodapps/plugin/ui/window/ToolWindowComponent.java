/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.JBUI;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.neodapps.plugin.NeoToolWindowFactory;

/**
 * Represents the content shown in tool window.
 */
public class ToolWindowComponent implements Disposable {

  private final Project project;
  private JBSplitter panel;

  /**
   * Creates the tool window content.
   *
   * @param project neo project.
   */
  public ToolWindowComponent(@NotNull Project project) {
    this.project = project;
  }

  /**
   * Returns the content required for tool window.
   *
   * @return the content that will be used by {@link NeoToolWindowFactory}
   */
  public JPanel getContent() {

    if (panel == null) {
      // split window into two panels
      panel = new JBSplitter(true, 0.2f);

      // set toolbar
      panel.setFirstComponent(createToolBar());

      // set data panel
      panel.setSecondComponent(getDetails());
    }
    return panel;
  }

  @Override
  public void dispose() {
    panel = null;
  }

  private JComponent createToolBar() {
    JPanel toolbar = JBUI.Panels.simplePanel();
    toolbar.setBorder(JBUI.Borders.customLineBottom(JBColor.border()));
    toolbar.setLayout(new FlowLayout(FlowLayout.LEADING));

    // select/apply component
    toolbar.add(new NodeSelectorComponent(project));

    // create/refresh buttons
    toolbar.add(new ActionComponent(project));

    return toolbar;
  }

  private JPanel getDetails() {

    JPanel content = JBUI.Panels.simplePanel();
    content.setLayout(new BorderLayout());

    // add status panel
    content.add(new NodeStatusComponent(project), BorderLayout.NORTH);

    // add main details
    content.add(new DataComponent(project), BorderLayout.CENTER);

    return content;
  }


  private JPanel getDataWrapperContent() {
    return new JPanel();
  }

  static class ToolWindowButton extends JButton {
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
}
