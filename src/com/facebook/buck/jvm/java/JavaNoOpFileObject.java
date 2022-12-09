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

package com.facebook.buck.jvm.java;

import com.facebook.buck.util.zip.JarBuilder;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

/**
 * An {@link JarFileObject} implementation that represents a {@link javax.tools.FileObject} that has
 * no operations and does not write the contents to any form of output.
 */
public class JavaNoOpFileObject extends JarFileObject {

  public JavaNoOpFileObject(URI uri, String pathInJar, Kind kind) {
    super(uri, pathInJar, kind);
  }

  @Override
  public InputStream openInputStream() {
    throw new UnsupportedOperationException();
  }

  @Override
  public OutputStream openOutputStream() {
    return new OutputStream() {
      @Override
      public void write(int b) {}

      @Override
      public void close() {}
    };
  }

  @Override
  public Reader openReader(boolean ignoreEncodingErrors) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Writer openWriter() {
    return new OutputStreamWriter(openOutputStream());
  }

  @Override
  public void writeToJar(JarBuilder jarBuilder, String owner) {}
}
