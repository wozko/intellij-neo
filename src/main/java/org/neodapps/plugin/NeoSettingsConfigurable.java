/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin;

import com.intellij.openapi.options.Configurable;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.neodapps.plugin.persistance.SettingsState;
import org.neodapps.plugin.ui.SettingsComponent;

/**
 * Provides controller functionality for application settings.
 */
public class NeoSettingsConfigurable implements Configurable {

  private SettingsComponent settingsComponent;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return NeoMessageBundle.message("settings.title");
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return settingsComponent.getPreferredFocusedComponent();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    settingsComponent = new SettingsComponent();
    return settingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    var settings = SettingsState.getInstance();
    return !settingsComponent.getDotNetRootPath().equals(settings.dotNetRoot)
        || !settingsComponent.getNeoExpressExecutablePath().equals(settings.neoExpressLocation);
  }

  @Override
  public void apply() {
    var settings = SettingsState.getInstance();
    settings.dotNetRoot = settingsComponent.getDotNetRootPath();
    settings.neoExpressLocation = settingsComponent.getNeoExpressExecutablePath();
  }

  @Override
  public void reset() {
    var settings = SettingsState.getInstance();
    settingsComponent.setDotNetRootPath(settings.dotNetRoot);
    settingsComponent.setNeoExpressExecutablePath(settings.neoExpressLocation);
  }

  @Override
  public void disposeUIResources() {
    settingsComponent = null;
  }

}