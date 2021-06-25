/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.neoexpress.service;

import com.intellij.openapi.project.Project;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.neodapps.plugin.Notifier;
import org.neodapps.plugin.neoexpress.model.ExpressCommands;
import org.neodapps.plugin.settings.SettingsState;

/**
 * Neo express task runner.
 */
public final class NeoExpressService {

  private final Project neoProject;

  public NeoExpressService(Project project) {
    neoProject = project;
  }

  /**
   * Creates a private net configuration.
   *
   * @param numberOfNodes  Number of consensus nodes to create, Allowed values are: 1, 4, 7
   * @param addressVersion Version to use for addresses in this blockchain instance
   * @param name           Name of the configuration
   */
  public void createPrivateNet(int numberOfNodes, long addressVersion, String name) {
    if (numberOfNodes != 1 && numberOfNodes != 4 && numberOfNodes != 7) {
      numberOfNodes = 1;
    }
    var commandResult =
        runCommand(ExpressCommands.CREATE, Arrays.asList("-c", String.valueOf(numberOfNodes),
            "-a", String.valueOf(addressVersion), "-f", name));
    if (commandResult != null) {
      Notifier.notifySuccess(neoProject, String.format("\"%s\" private net created.", name));
    }
  }

  /**
   * Runs a neo-express command.
   *
   * @param command command to run
   * @param options command options
   * @return output of command
   */
  public String runCommand(ExpressCommands command, List<String> options) {
    var settings = SettingsState.getInstance();


    if (settings.dotNetRoot.isEmpty()) {
      Notifier.notifyError(neoProject, "DOTNET_ROOT path is not set (Settings > Tools > Neo). ");
      return null;
    }

    if (settings.neoExpressLocation.isEmpty()) {
      Notifier.notifyError(neoProject,
          "Neo-express executable path is not set (Settings > Tools > Neo) ");
      return null;
    }

    List<String> params = new ArrayList<>();
    params.add(settings.neoExpressLocation);
    params.add(command.toString());
    params.addAll(options);

    ProcessBuilder builder = new ProcessBuilder(params);
    builder.directory(new File(Objects.requireNonNull(neoProject.getBasePath())));
    builder.environment().put("DOTNET_ROOT", settings.dotNetRoot);

    String stdout;
    String stder;
    try {
      Process p = builder.start();
      stdout = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
      stder = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
    } catch (IOException e) {
      Notifier.notifyError(neoProject, e.getMessage() + "\n\n"
          + "Make sure neo-express is configured properly (Settings > Tools > Neo)");
      return null;
    }

    if (!stder.isEmpty()) {
      Notifier.notifyError(neoProject,
          stder + "\n\n" + "Make sure neo-express is configured properly (Settings > Tools > Neo)");
      return null;
    }

    return stdout;
  }
}
