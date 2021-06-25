/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * Supports sending notification.
 */
public class Notifier {

  /**
   * Supports sending error notifications.
   *
   * @param project intellij project
   * @param content notification content
   */
  public static void notifyError(@Nullable Project project, String content) {
    NotificationGroupManager.getInstance().getNotificationGroup("org.neodapps.plugin")
        .createNotification(content, NotificationType.ERROR)
        .notify(project);
  }

  /**
   * Supports sending information notifications.
   *
   * @param project intellij project
   * @param content notification content
   */
  public static void notifySuccess(@Nullable Project project, String content) {
    NotificationGroup group =
        NotificationGroupManager.getInstance().getNotificationGroup("org.neodapps.plugin");
    group.createNotification(content, NotificationType.INFORMATION)
        .notify(project);
  }
}
