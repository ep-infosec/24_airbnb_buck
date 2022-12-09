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

package com.facebook.buck.httpserver;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import java.net.URL;
import javax.annotation.Nullable;
import org.eclipse.jetty.server.Request;

/** Handles requests to the root URI, {@code /}. */
class IndexHandlerDelegate implements TemplateHandlerDelegate {

  @Override
  public String getTemplateForRequest(Request baseRequest) {
    return "index";
  }

  @Nullable
  @Override
  public ImmutableMap<String, Object> getDataForRequest(Request baseRequest) {
    return ImmutableMap.of();
  }

  @Override
  public URL getTemplateGroup() {
    return Resources.getResource(IndexHandlerDelegate.class, "templates.stg");
  }
}
