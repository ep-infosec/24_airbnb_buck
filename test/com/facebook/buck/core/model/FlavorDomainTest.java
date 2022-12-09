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

package com.facebook.buck.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.junit.Test;

public class FlavorDomainTest {

  @Test
  public void getFlavor() {
    Flavor flavor = InternalFlavor.of("hello");
    FlavorDomain<String> domain = new FlavorDomain<>("test", ImmutableMap.of(flavor, "something"));
    BuildTarget target = BuildTargetFactory.newInstance("//:test#hello");
    assertEquals(Optional.of(flavor), domain.getFlavor(target));
    target = BuildTargetFactory.newInstance("//:test#invalid");
    assertEquals(Optional.empty(), domain.getFlavor(target));
  }

  @Test
  public void multipleFlavorsForSameDomainShouldThrow() {
    Flavor hello = InternalFlavor.of("hello");
    Flavor goodbye = InternalFlavor.of("goodbye");
    FlavorDomain<String> domain =
        new FlavorDomain<>(
            "test",
            ImmutableMap.of(
                hello, "something",
                goodbye, "something"));
    BuildTarget target =
        BuildTargetFactory.newInstance("//:test").withAppendedFlavors(hello, goodbye);
    try {
      domain.getFlavor(target);
      fail("should have thrown");
    } catch (FlavorDomainException e) {
      assertTrue(e.getMessage().contains("multiple \"test\" flavors"));
    }
  }

  @Test
  public void getValue() throws FlavorDomainException {
    Flavor flavor = InternalFlavor.of("hello");
    FlavorDomain<String> domain = new FlavorDomain<>("test", ImmutableMap.of(flavor, "something"));
    String val = domain.getValue(flavor);
    assertEquals("something", val);
    try {
      domain.getValue(InternalFlavor.of("invalid"));
      fail("should have thrown");
    } catch (FlavorDomainException e) {
      assertTrue(e.getMessage().contains("has no flavor"));
    }
  }

  @Test
  public void map() {
    Flavor hello = InternalFlavor.of("hello");
    Flavor goodbye = InternalFlavor.of("goodbye");

    FlavorDomain<String> domain =
        new FlavorDomain<>("test", ImmutableMap.of(hello, "hello", goodbye, "goodbye"));

    FlavorDomain<String> mapped = domain.map(s -> s + " world!");

    assertEquals("test", mapped.getName());
    assertEquals("hello world!", mapped.getValue(hello));
    assertEquals("goodbye world!", mapped.getValue(goodbye));

    FlavorDomain<Integer> lengths = mapped.map("lengths", String::length);

    assertEquals("lengths", lengths.getName());
    assertEquals((Integer) 12, lengths.getValue(hello));
    assertEquals((Integer) 14, lengths.getValue(goodbye));
  }

  class SimpleFlavorConvertible implements FlavorConvertible {
    final String value;
    final String flavor;

    SimpleFlavorConvertible(String value, String flavor) {
      this.value = value;
      this.flavor = flavor;
    }

    @Override
    public Flavor getFlavor() {
      return InternalFlavor.of(flavor);
    }
  }

  @Test
  public void convert() {
    Flavor hello = InternalFlavor.of("hello");
    Flavor goodbye = InternalFlavor.of("goodbye");

    FlavorDomain<String> domain =
        new FlavorDomain<>("test", ImmutableMap.of(hello, "hello", goodbye, "goodbye"));

    FlavorDomain<SimpleFlavorConvertible> converted =
        domain.convert("newname", s -> new SimpleFlavorConvertible(s, "flavor-" + s));

    assertEquals("newname", converted.getName());
    assertEquals("hello", converted.getValue(InternalFlavor.of("flavor-hello")).value);
    assertEquals("goodbye", converted.getValue(InternalFlavor.of("flavor-goodbye")).value);
  }
}
