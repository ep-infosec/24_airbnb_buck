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
import com.google.common.collect.ImmutableMap;

/** ValueTypeInfo for ImmutableSortedMaps. */
public class ImmutableMapValueTypeInfo<K, V> implements ValueTypeInfo<ImmutableMap<K, V>> {
  private final ValueTypeInfo<K> keyType;
  private final ValueTypeInfo<V> valueType;

  public ImmutableMapValueTypeInfo(ValueTypeInfo<K> keyType, ValueTypeInfo<V> valueType) {
    this.keyType = keyType;
    this.valueType = valueType;
  }

  @Override
  public <E extends Exception> void visit(ImmutableMap<K, V> value, ValueVisitor<E> visitor)
      throws E {
    visitor.visitMap(value, keyType, valueType);
  }

  @Override
  public <E extends Exception> ImmutableMap<K, V> create(ValueCreator<E> creator) throws E {
    return creator.createMap(keyType, valueType);
  }
}
