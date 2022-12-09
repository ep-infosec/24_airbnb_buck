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

package com.facebook.buck.event.listener;

import com.facebook.buck.core.config.BuckConfig;
import com.facebook.buck.core.config.ConfigView;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

/** Strong-type configuration for [scribe_event_listener] section */
@BuckStyleValue
public abstract class ScribeEventListenerConfig implements ConfigView<BuckConfig> {

  public static final String BUILDFILE_SECTION_NAME = "scribe_event_listener";

  @Override
  public abstract BuckConfig getDelegate();

  public static ScribeEventListenerConfig of(BuckConfig delegate) {
    return ImmutableScribeEventListenerConfig.of(delegate);
  }

  /** If false (default) then Buck events are not logged to Scribe */
  @Value.Lazy
  public boolean getEnabled() {
    return getDelegate()
        .getValue(BUILDFILE_SECTION_NAME, "enabled")
        .map(Boolean::parseBoolean)
        .orElse(false);
  }

  /** @return Scribe category to receive Buck events */
  @Value.Lazy
  public String getCategory() {
    return getDelegate().getValue(BUILDFILE_SECTION_NAME, "category").orElse("buck_events");
  }

  /**
   * @return Event names that are allowed to be serialized and logged to Scribe. Only explicitly
   *     specified events would be logged to prevent possible high traffic flow.
   */
  @Value.Lazy
  public Iterable<String> getEvents() {
    return getDelegate().getListWithoutComments(BUILDFILE_SECTION_NAME, "events");
  }

  /**
   * @return Statuses that allow BuildRuleFinished events to be logged to Scribe. Will return a
   *     subset of https://fburl.com/m4o5p1mg
   */
  @Value.Lazy
  public Iterable<String> getEnabledBuildRuleFinishedStatuses() {
    ImmutableList<String> list =
        getDelegate()
            .getListWithoutComments(BUILDFILE_SECTION_NAME, "enabled_build_rule_finished_statuses");
    return list.isEmpty() ? ImmutableList.of("FAIL") : list;
  }
}
