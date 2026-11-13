# Recursive Stacks

A C23 library implementing stacks whose elements can hold either a `uint64_t` value or a reference to another stack. Multiple stacks can reference the same stack, and circular references are allowed.

Each stack stores its elements as a linked list. Stack references are shared rather than copied, so modifying a referenced stack affects every structure that contains a reference to it.

## Stack operations

The public API provides functions for creating and deleting stacks, pushing values or stack references, and removing the top element.

Two operations require traversing the nested structure:

- `rstack_front` returns the first numeric value encountered during a depth-first traversal, visiting elements from top to bottom.
- `rstack_empty` checks whether any numeric value is reachable, even through multiple levels of references.

Both operations use DFS with visited markers to avoid infinite recursion when cycles are present. The markers are cleared after the traversal.

Unlike these recursive operations, `rstack_pop` removes only the top-level element of the given stack. If that element references another stack, the reference is released.

## Memory management

Memory management is more complicated than in an ordinary stack because references can be shared and may form cycles.

Each stack maintains a reference counter. Creating a stack gives it one reference, while pushing it onto another stack increases its counter. Removing a reference decreases the counter.

When `rstack_delete` is called, it first decrements the reference count. If the count reaches zero, `rstack_destroy` immediately frees the stack and releases its references to other stacks.

If the count remains positive, the library runs a cycle collection algorithm. This is necessary because reference counting alone cannot reclaim cycles: two stacks referencing each other may remain allocated even when no external references exist.

The collector works in three phases.

### 1. Trial deletion — `rstack_trial_unlink`

The algorithm starts from the stack whose reference was released and traverses all stacks reachable from it.

Each visited stack is marked `GRAY`. For every reference to another stack, the referenced stack's counter is temporarily decremented, and the traversal continues recursively.

The decrement happens **even if the referenced stack was already visited**. The `GRAY` marker only prevents processing the same stack's outgoing references more than once.

This distinction matters when several references point to the same stack, including references forming cycles. Every internal reference must be subtracted, but each stack's outgoing references should only be examined once.

After this phase, the remaining reference counts represent references coming from outside the traversed subgraph.

### 2. Checking references — `rstack_check` and `rstack_restore`

The algorithm examines the stacks marked `GRAY`.

If a stack has a positive reference count after trial deletion, it must still be referenced from outside the traversed subgraph. It therefore needs to be preserved, along with all stacks reachable from it.

`rstack_restore` traverses that part of the graph, restores the temporarily decremented reference counts and marks the visited stacks `BLACK`.

If a `GRAY` stack has no remaining references, `rstack_check` marks it `WHITE` and recursively examines its children.

A `WHITE` stack is only a candidate for deletion at this point. It may later be reached by `rstack_restore` from another, externally referenced stack. In that case, its references are restored and its color changes to `BLACK`.

This is why `rstack_restore` also processes `WHITE` stacks and stops recursion only when it encounters a stack already marked `BLACK`.

By the end of this phase, the stacks that must remain allocated are `BLACK`, with their original reference counts restored. The unreachable stacks remain `WHITE`.

### 3. Collection — `rstack_collect`

Starting from the stack passed to the collector, `rstack_collect_mark` traverses the remaining `WHITE` stacks.

Each encountered stack is added to a linked list using its `collect_next` field and marked `PURPLE`. This prevents the same stack from being collected multiple times, even when there are cycles or shared references.

Once all unreachable stacks have been gathered, memory is released in two passes:

1. Free every element in the linked lists belonging to the collected stacks.
2. Free the stack structures themselves.

The collector does not call `rstack_delete` on references between collected stacks. Those references have already been accounted for during trial deletion, and all their owners are being freed together.

Freeing the elements and stack structures separately also avoids accessing stack structures that have already been deallocated.

This procedure reclaims unreachable cyclic structures while preserving stacks that are still referenced from outside the collected subgraph.

## File operations

The library also supports reading and writing stacks as text files.

`rstack_read` reads whitespace-separated unsigned integers and pushes them onto a newly created stack. It validates the input and handles invalid characters, numeric overflow and file errors.

`rstack_write` traverses a recursive stack in bottom-to-top order, expanding referenced stacks and writing numeric values one per line.

During writing, stacks on the current recursion path are marked to detect cycles. If a cycle is encountered, the traversal stops.

Both functions report errors through return values and `errno`.

## Compilation

Compile the library with a C23-compatible compiler:

```bash
gcc -std=c23 -Wall -Wextra -Wpedantic -c rstack.c
```

The public interface is declared in `rstack.h`, and its implementation is in `rstack.c`. Compilation produces an object file that can be linked with a program using the library.