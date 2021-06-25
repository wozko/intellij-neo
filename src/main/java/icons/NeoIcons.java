/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package icons;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

/**
 * Provides neo icons to use throughout the plugin.
 */
public class NeoIcons {

  public static final @NotNull Icon Neo_13 = load("icons/neo-13.png");
  public static final @NotNull Icon Neo_16 = load("icons/neo-16.png");
  public static final @NotNull Icon Neo_24 = load("icons/neo-24.png");
  public static final @NotNull Icon Neo_32 = load("icons/neo-32.png");
  public static final @NotNull Icon Neo_64 = load("icons/neo-64.png");

  private static @NotNull Icon load(@NotNull String path) {
    return IconLoader.getIcon(path, NeoIcons.class);
  }
}
