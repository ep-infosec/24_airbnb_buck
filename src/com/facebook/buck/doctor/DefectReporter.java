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

package com.facebook.buck.doctor;

import com.facebook.buck.core.model.BuildId;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.facebook.buck.core.util.immutables.BuckStyleValueWithBuilder;
import com.facebook.buck.doctor.config.DoctorProtocolVersion;
import com.facebook.buck.doctor.config.SourceControlInfo;
import com.facebook.buck.doctor.config.UserLocalConfiguration;
import com.facebook.buck.util.environment.BuildEnvironmentDescription;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/** Interface around the 'backend' of submitting a defect report. */
public interface DefectReporter {
  DefectSubmitResult submitReport(DefectReport defectReport) throws IOException;

  @BuckStyleValueWithBuilder
  interface DefectSubmitResult {

    /**
     * If value is empty then no request was made and report was saved locally.
     *
     * @return if request was successful.
     */
    Optional<Boolean> getIsRequestSuccessful();

    /** Returns the protocol version of the request based on {@link DoctorProtocolVersion}. */
    DoctorProtocolVersion getRequestProtocol();

    /** @return The location where the report was saved, it can be a remote link or a local path */
    Optional<String> getReportSubmitLocation();

    /** @return the content of the response. */
    Optional<String> getReportSubmitMessage();

    /** @return if an error occurred it will have the error. */
    Optional<String> getReportSubmitErrorMessage();

    /** @return The id of the generated report object by the server */
    Optional<String> getReportId();
  }

  /** Information helpful when diagnosing 'buck is not picking up changes' reports. */
  @BuckStyleValue
  interface FileChangesIgnoredReport {
    Optional<Path> getWatchmanDiagReport();
  }

  @BuckStyleValueWithBuilder
  interface DefectReport {
    Optional<AbstractReport.UserReport> getUserReport();

    ImmutableSet<BuildId> getHighlightedBuildIds();

    ImmutableSet<Path> getIncludedPaths();

    BuildEnvironmentDescription getBuildEnvironmentDescription();

    Optional<SourceControlInfo> getSourceControlInfo();

    Optional<String> getExtraInfo();

    Optional<FileChangesIgnoredReport> getFileChangesIgnoredReport();

    Optional<UserLocalConfiguration> getUserLocalConfiguration();

    static Builder builder() {
      return new Builder();
    }

    class Builder extends ImmutableDefectReport.Builder {}
  }
}
