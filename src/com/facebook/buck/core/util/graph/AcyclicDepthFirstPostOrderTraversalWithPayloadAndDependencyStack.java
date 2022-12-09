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

package com.facebook.buck.core.util.graph;

import com.facebook.buck.core.exceptions.DependencyStack;
import com.facebook.buck.util.types.Pair;
import com.google.common.base.Preconditions;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Performs a depth-first, post-order traversal over a DAG.
 *
 * <p>This version of algorithm tracks traversal path in {@link DependencyStack} structure and
 * provides it to user in {@link GraphTraversableWithPayloadAndDependencyStack} callback and in
 * result of {@link #traverse(Iterable)} operation.
 *
 * <p>If a cycle is encountered, a {@link CycleException} is thrown by {@link #traverse(Iterable)}.
 *
 * @param <T> the type of node in the graph
 */
public class AcyclicDepthFirstPostOrderTraversalWithPayloadAndDependencyStack<T, P> {

  private final GraphTraversableWithPayloadAndDependencyStack<T, P> traversable;
  private final BiFunction<DependencyStack, T, DependencyStack> dependencyStackChild;

  /**
   * @param dependencyStackChild a function to construct a child stack from a stack and a new graph
   *     node. In the most cases it is just an invocation of {@link
   *     DependencyStack#child(DependencyStack.Element)}.
   */
  public AcyclicDepthFirstPostOrderTraversalWithPayloadAndDependencyStack(
      GraphTraversableWithPayloadAndDependencyStack<T, P> traversable,
      BiFunction<DependencyStack, T, DependencyStack> dependencyStackChild) {
    this.traversable = traversable;
    this.dependencyStackChild = dependencyStackChild;
  }

  /**
   * Performs a depth-first, post-order traversal over a DAG.
   *
   * @param initialNodes The nodes from which to perform the traversal. Not allowed to contain
   *     {@code null}.
   * @throws CycleException if a cycle is found while performing the traversal.
   */
  // explicitly return LinkedHashMap to:
  // * signal it is ordered
  // * avoid overhead of converting into a different type here or on the call site
  // so mark this method with `PMD.LooseCoupling`
  @SuppressWarnings("PMD.LooseCoupling")
  public LinkedHashMap<T, Pair<P, DependencyStack>> traverse(Iterable<? extends T> initialNodes)
      throws CycleException {
    return traverse(initialNodes, node -> true);
  }

  /**
   * Performs a depth-first, post-order traversal over a DAG.
   *
   * @param initialNodes The nodes from which to perform the traversal. Not allowed to contain
   *     {@code null}.
   * @param shouldExploreChildren Whether or not to explore a particular node's children. Used to
   *     support short circuiting in the traversal.
   * @throws CycleException if a cycle is found while performing the traversal.
   */
  @SuppressWarnings({"PMD.PrematureDeclaration", "PMD.LooseCoupling"})
  public LinkedHashMap<T, Pair<P, DependencyStack>> traverse(
      Iterable<? extends T> initialNodes, Predicate<T> shouldExploreChildren)
      throws CycleException {
    // This corresponds to the current chain of nodes being explored. Enforcing this invariant makes
    // this data structure useful for debugging.
    Deque<Explorable> toExplore = new LinkedList<>();
    for (T node : initialNodes) {
      toExplore.add(new Explorable(node, dependencyStackChild.apply(DependencyStack.root(), node)));
    }

    Set<T> inProgress = new HashSet<>();
    LinkedHashMap<T, Pair<P, DependencyStack>> explored = new LinkedHashMap<>();

    while (!toExplore.isEmpty()) {
      Explorable explorable = toExplore.peek();
      T node = explorable.node;

      // This could happen if one of the initial nodes is a dependency of the other, for example.
      if (explored.containsKey(node)) {
        toExplore.removeFirst();
        continue;
      }

      inProgress.add(node);

      // Find children that need to be explored to add to the stack.
      int stackSize = toExplore.size();
      if (shouldExploreChildren.test(node)) {
        for (Iterator<? extends T> iter = explorable.children; iter.hasNext(); ) {
          T child = iter.next();
          if (inProgress.contains(child)) {
            throw createCycleException(child, toExplore);
          }
          if (!explored.containsKey(child)) {
            toExplore.addFirst(
                new Explorable(
                    child, dependencyStackChild.apply(explorable.dependencyStack, child)));

            // Without this break statement:
            // (1) Children will be explored in reverse order instead of the specified order.
            // (2) CycleException may contain extra nodes.
            // Comment out the break statement and run the unit test to verify this for yourself.
            break;
          }
        }
      }

      if (stackSize == toExplore.size()) {
        // Nothing was added to toExplore, so the current node can be popped off the stack and
        // marked as explored.
        toExplore.removeFirst();
        inProgress.remove(node);
        explored.put(node, new Pair<>(explorable.payload, explorable.dependencyStack));
      }
    }

    Preconditions.checkState(inProgress.isEmpty(), "No more nodes should be in progress.");

    return explored;
  }

  /**
   * A node that needs to be explored, paired with a (possibly paused) iteration of its children.
   */
  private class Explorable {
    private final T node;
    private final DependencyStack dependencyStack;
    private final P payload;
    private final Iterator<? extends T> children;

    Explorable(T node, DependencyStack dependencyStack) {
      this.node = node;
      this.dependencyStack = dependencyStack;
      Pair<P, Iterator<? extends T>> x = traversable.findNodeAndChildren(node, dependencyStack);
      this.payload = x.getFirst();
      this.children = x.getSecond();
    }
  }

  private CycleException createCycleException(
      T collisionNode, Iterable<Explorable> currentExploration) {
    Deque<T> chain = new LinkedList<>();
    chain.add(collisionNode);

    boolean foundStartOfCycle = false;
    for (Explorable explorable : currentExploration) {
      T node = explorable.node;
      chain.addFirst(node);
      if (collisionNode.equals(node)) {
        // The start of the cycle has been reached!
        foundStartOfCycle = true;
        break;
      }
    }

    Preconditions.checkState(
        foundStartOfCycle,
        "Start of cycle %s should appear in traversal history %s.",
        collisionNode,
        chain);

    return new CycleException(chain);
  }
}
