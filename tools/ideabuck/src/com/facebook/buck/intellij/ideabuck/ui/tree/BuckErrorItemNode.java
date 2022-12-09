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

package com.facebook.buck.intellij.ideabuck.ui.tree;

public class BuckErrorItemNode extends BuckTextNode {
  private final int mLine;
  private final int mColumn;

  public BuckErrorItemNode(String text, int line, int column) {
    super(text, TextType.ERROR);
    mLine = line;
    mColumn = column;
  }

  public String getError() {
    return getText();
  }

  public int getLine() {
    return mLine;
  }

  public int getColumn() {
    return mColumn;
  }
}
