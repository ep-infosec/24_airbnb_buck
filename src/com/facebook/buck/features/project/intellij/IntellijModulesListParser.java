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

package com.facebook.buck.features.project.intellij;

import com.facebook.buck.core.exceptions.HumanReadableException;
import com.facebook.buck.core.util.log.Logger;
import com.facebook.buck.features.project.intellij.model.ModuleIndexEntry;
import com.facebook.buck.util.xml.XmlDomParser;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Responsible for parsing an existing modules.xml file */
public class IntellijModulesListParser {
  private static Logger LOG = Logger.get(IntellijModulesListParser.class);

  /**
   * @param modulesFile modules.xml input stream
   * @return A list of module entries as specified by the modules.xml file
   * @throws IOException
   */
  public ImmutableSet<ModuleIndexEntry> getAllModules(InputStream modulesFile) throws IOException {
    final Document doc;
    try {
      doc = XmlDomParser.parse(modulesFile);
    } catch (SAXException e) {
      LOG.error("Cannot read modules.xml file", e);
      throw new HumanReadableException(
          "Could not update 'modules.xml' file because it is malformed", e);
    }
    final Builder<ModuleIndexEntry> builder = ImmutableSet.builder();
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      final NodeList moduleList =
          (NodeList)
              xpath
                  .compile("/project/component/modules/module")
                  .evaluate(doc, XPathConstants.NODESET);
      for (int i = 0; i < moduleList.getLength(); i++) {
        final Element moduleEntry = (Element) moduleList.item(i);
        if (!moduleEntry.hasAttribute("filepath")) {
          continue;
        }
        String filepath = moduleEntry.getAttribute("filepath");
        String fileurl = moduleEntry.getAttribute("fileurl");
        String filepathWithoutProjectPrefix;
        // The template has a hardcoded $PROJECT_DIR$/ prefix, so we need to strip that out
        // of the value we pass to ST
        if (filepath.startsWith("$PROJECT_DIR$")) {
          filepathWithoutProjectPrefix = filepath.substring("$PROJECT_DIR$".length() + 1);
        } else {
          filepathWithoutProjectPrefix = filepath;
        }
        builder.add(
            ModuleIndexEntry.of(
                fileurl,
                Paths.get(filepathWithoutProjectPrefix),
                moduleEntry.hasAttribute("group") ? moduleEntry.getAttribute("group") : null));
      }
    } catch (XPathExpressionException e) {
      throw new HumanReadableException("Illegal xpath expression.", e);
    }
    return builder.build();
  }
}
