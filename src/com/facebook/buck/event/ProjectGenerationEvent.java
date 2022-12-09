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

package com.facebook.buck.event;

public abstract class ProjectGenerationEvent extends AbstractBuckEvent implements WorkAdvanceEvent {

  public ProjectGenerationEvent() {
    super(EventKey.unique());
  }

  @Override
  public String getValueString() {
    return "";
  }

  public static Started started() {
    return new Started();
  }

  public static Finished finished() {
    return new Finished();
  }

  public static Processed processed() {
    return new Processed();
  }

  public static class Started extends ProjectGenerationEvent {
    @Override
    public String getEventName() {
      return PROJECT_GENERATION_STARTED;
    }
  }

  public static class Finished extends ProjectGenerationEvent {
    @Override
    public String getEventName() {
      return PROJECT_GENERATION_FINISHED;
    }
  }

  public static class Processed extends ProjectGenerationEvent {
    @Override
    public String getEventName() {
      return "ProjectGenerationProcessed";
    }
  }
}
