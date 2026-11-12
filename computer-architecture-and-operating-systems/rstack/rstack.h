#ifndef _RSTACK_H
#define _RSTACK_H

#include <stddef.h>
#include <inttypes.h>

typedef struct rstack rstack_t;

typedef struct {
  // Whether value contains a result.
  bool     flag;
  // The resulting numeric value.
  uint64_t value;
} result_t;

/// Create an empty stack with one caller-owned reference.
/// Return nullptr on allocation failure and set errno.
rstack_t* rstack_new();

/// Release one reference and reclaim stacks that become unreachable,
/// including cycles. Do nothing if rs is nullptr.
void rstack_delete(rstack_t *rs);

/// Push value onto rs. Return 0 on success or -1 on failure and set errno.
int rstack_push_value(rstack_t *rs, uint64_t value);

/// Push a reference to rs2 onto rs1, incrementing rs2's reference count.
/// Return 0 on success or -1 on failure and set errno.
int rstack_push_rstack(rstack_t *rs1, rstack_t *rs2);

/// Remove the top-level element without descending into child stacks.
/// Release its reference if it is a stack. Do nothing if rs is nullptr
/// or has no top-level elements.
void rstack_pop(rstack_t *rs);

/// Return true if no numeric value is reachable from rs, or rs is nullptr.
/// Search recursively, visiting each stack at most once to handle cycles.
bool rstack_empty(rstack_t *rs);

/// Return the first number found by top-first depth-first traversal.
/// Return a result with flag set to false if no number is reachable,
/// including when rs is nullptr. Leave the stack contents unchanged.
result_t rstack_front(rstack_t *rs);

/// Create a stack by pushing whitespace-separated unsigned decimal integers
/// from the file at path. The last number read becomes the top value.
/// Return nullptr on failure and set errno; preserve errno on success.
rstack_t* rstack_read(char const *path);

/// Write reachable values to path in bottom-to-top traversal order,
/// one number per line, replacing the file's previous contents.
/// Stop all further output at the first cycle; this is not an error.
/// Return 0 on success or -1 on failure and set errno.
/// Preserve errno on success. The file does not preserve stack references.
int rstack_write(char const *path, rstack_t *rs);

#endif
