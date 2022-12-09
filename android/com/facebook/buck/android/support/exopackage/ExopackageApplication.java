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

package com.facebook.buck.android.support.exopackage;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import java.lang.reflect.Constructor;

/**
 * A base class for implementing an Application that delegates to an {@link ApplicationLike}
 * instance. This is used in conjunction with secondary dex files so that the logic that would
 * normally live in the Application class is loaded after the secondary dexes are loaded.
 */
public abstract class ExopackageApplication<T extends ApplicationLike> extends Application {

  private static final int SECONDARY_DEX_MASK = 1;
  private static final int NATIVE_LIBRARY_MASK = 2;
  private static final int RESOURCES_MASK = 4;
  private static final int MODULES_MASK = 8;

  private final String delegateClassName;
  private final int exopackageFlags;
  private T delegate;

  /**
   * @param exopackageFlags Bitmask used to determine which exopackage feature is enabled in the
   *     current build. This should usually be {@code BuildConfig.EXOPACKAGE_FLAGS}.
   */
  protected ExopackageApplication(int exopackageFlags) {
    this(DefaultApplicationLike.class.getName(), exopackageFlags);
  }

  /**
   * @param delegateClassName The fully-qualified name of the {@link ApplicationLike} class that
   *     will act as the delegate for application lifecycle callbacks.
   * @param exopackageFlags Bitmask used to determine which exopackage feature is enabled in the
   *     current build. This should usually be {@code BuildConfig.EXOPACKAGE_FLAGS}.
   */
  protected ExopackageApplication(String delegateClassName, int exopackageFlags) {
    this.delegateClassName = delegateClassName;
    this.exopackageFlags = exopackageFlags;
  }

  private boolean isExopackageEnabledForSecondaryDex() {
    return (exopackageFlags & SECONDARY_DEX_MASK) != 0;
  }

  private boolean isExopackageEnabledForNativeLibraries() {
    return (exopackageFlags & NATIVE_LIBRARY_MASK) != 0;
  }

  private boolean isExopackageEnabledForResources() {
    return (exopackageFlags & RESOURCES_MASK) != 0;
  }

  private boolean isExopackageEnabledForModules() {
    return (exopackageFlags & MODULES_MASK) != 0;
  }

  private T createDelegate() {
    if (isExopackageEnabledForSecondaryDex()) {
      ExopackageDexLoader.loadExopackageJars(this, isExopackageEnabledForModules());
      if (isExopackageEnabledForModules()) {
        ExoHelper.setupHotswap(this);
      }
    }

    if (isExopackageEnabledForNativeLibraries()) {
      ExopackageSoLoader.init(this);
    }

    if (isExopackageEnabledForResources()) {
      ResourcesLoader.init(this);
    }

    try {
      // Use reflection to create the delegate so it doesn't need to go into the primary dex.
      Class<T> implClass = (Class<T>) Class.forName(delegateClassName);
      Constructor<T> constructor = implClass.getConstructor(Application.class);
      return constructor.newInstance(this);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private synchronized void ensureDelegate() {
    if (delegate == null) {
      delegate = createDelegate();
    }
  }

  /**
   * Hook for sub-classes to run logic after the {@link Application#attachBaseContext} has been
   * called but before the delegate is created. Implementors should be very careful what they do
   * here since {@link android.app.Application#onCreate} will not have yet been called.
   */
  protected void onBaseContextAttached() {}

  /** @return the delegate, or {@code null} if not set up. */
  // @Nullable  - Don't want to force a reference to that annotation in the primary dex.
  public final T getDelegateIfPresent() {
    return delegate;
  }

  @Override
  protected final void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    onBaseContextAttached();
    ensureDelegate();
  }

  @Override
  public final void onCreate() {
    super.onCreate();
    if (isExopackageEnabledForResources()) {
      ResourcesLoader.onAppCreated(this);
    }
    ensureDelegate();
    delegate.onCreate();
  }

  @Override
  public final void onTerminate() {
    super.onTerminate();
    if (delegate != null) {
      delegate.onTerminate();
    }
  }

  @Override
  public final void onLowMemory() {
    super.onLowMemory();
    if (delegate != null) {
      delegate.onLowMemory();
    }
  }

  @TargetApi(14)
  @Override
  public final void onTrimMemory(int level) {
    super.onTrimMemory(level);
    if (delegate != null) {
      delegate.onTrimMemory(level);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (delegate != null) {
      delegate.onConfigurationChanged(newConfig);
    }
  }

  @Override
  public Object getSystemService(String name) {
    if (delegate != null) {
      Object service = delegate.getSystemService(name);
      if (service != null) {
        return service;
      }
    }
    return super.getSystemService(name);
  }
}
