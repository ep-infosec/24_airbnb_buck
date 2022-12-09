/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.intellij.ideabuck.notification;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class BuckNotification {
  private static final NotificationGroup NOTIFICATION_GROUP =
      NotificationGroup.balloonGroup("Buck Notification Group");

  @NotNull private final Project project;

  @NotNull
  public static BuckNotification getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, BuckNotification.class);
  }

  public BuckNotification(@NotNull Project project) {
    this.project = project;
  }

  /** Show a warning balloon with the given message. */
  public void showWarningBalloon(@NotNull final String message) {
    NOTIFICATION_GROUP.createNotification(message, NotificationType.WARNING).notify(project);
  }

  /** Show an info balloon with the given message and attach the given listener. */
  public void showInfoBalloon(@NotNull String message, NotificationListener listener) {
    NOTIFICATION_GROUP
        .createNotification("", message, NotificationType.INFORMATION, listener)
        .notify(project);
  }

  /** Show a warning balloon with the given message and attach the given listener. */
  public void showWarningBalloon(@NotNull String message, NotificationListener listener) {
    NOTIFICATION_GROUP
        .createNotification("", message, NotificationType.WARNING, listener)
        .notify(project);
  }

  /** Show an error balloon with the given message and attach the given listener. */
  public void showErrorBalloon(@NotNull String message, NotificationListener listener) {
    NOTIFICATION_GROUP
        .createNotification("", message, NotificationType.ERROR, listener)
        .notify(project);
  }
}
