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

package com.facebook.buck.rules.modern.impl;

import com.facebook.buck.rules.modern.ValueCreator;
import com.facebook.buck.rules.modern.ValueTypeInfo;
import com.facebook.buck.rules.modern.ValueVisitor;
import com.google.common.hash.HashCode;

/**
 * ValueTypeInfo for HashCode. These are basically treated as just the underlying hashcode string.
 */
public class HashCodeValueTypeInfo implements ValueTypeInfo<HashCode> {
  public static final ValueTypeInfo<HashCode> INSTANCE = new HashCodeValueTypeInfo();

  @Override
  public <E extends Exception> void visit(HashCode value, ValueVisitor<E> visitor) throws E {
    visitor.visitString(value.toString());
  }

  @Override
  public <E extends Exception> HashCode create(ValueCreator<E> creator) throws E {
    return HashCode.fromString(creator.createString());
  }
}
