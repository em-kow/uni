# Recursive Stacks

A C23 library where a stack element can hold either a `uint64_t` value or a reference to another stack. Several stacks may reference the same stack, and references can form cycles.

Each stack stores its own elements as a linked list, with the most recently pushed element at the top. A reference is not a copy: changing a referenced stack is visible from everywhere that points to it.

## Operations

The API in `rstack.h` provides stack creation and deletion, `rstack_push_value`, `rstack_push_rstack`, and `rstack_pop`. Popping removes the top-level element of the given stack, even if that element happens to reference another stack.

Two operations look through nested stacks:

- `rstack_front` searches for the first numeric value, traversing elements from top to bottom and following stack references recursively. Its result indicates whether a value was found.
- `rstack_empty` checks whether there is **any numeric value** reachable from the stack. A stack that only references other empty stacks can therefore be considered empty even when it has top-level elements.

Both traversals mark visited stacks so that circular references cannot cause infinite recursion.

## Memory management

Sharing and cycles make deletion less straightforward than freeing a linked list. Each stack has a reference counter: pushing a reference increments it; removing one decrements it. Ordinary reference counting alone would leave unreachable cycles allocated.

The implementation also performs cycle collection when needed. It temporarily discounts references between reachable stacks, identifies which stacks still have outside references, restores those live parts, and frees the remaining unreachable stacks. This is handled by the deletion helpers in `rstack.c`.

## File operations

`rstack_read` reads unsigned decimal values separated by whitespace and pushes them onto a new stack. `rstack_write` traverses a recursive stack and writes the numeric values to a text file, one per line, in bottom-to-top order. Referenced stacks are expanded during writing; traversal stops if it encounters a cycle on the active path.

These functions handle invalid input, allocation failures and file errors using return values and `errno`. The text format contains values, not the original graph of stack references.

The public interface is declared in `rstack.h`, with the implementation in `rstack.c`.

## Compilation

Compile the library with a C23-compatible compiler:

```bash
gcc -std=c23 -Wall -Wextra -Wpedantic -c rstack.c
```

This produces an object file that can be linked with a program using the library.