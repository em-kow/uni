// Author: Mikołaj Kowalski (479520)



#include <stdio.h>
#include <errno.h>
#include <stdbool.h>
#include <stdlib.h>
#include <ctype.h>
#include <inttypes.h>
#include "rstack.h"



// Stack element types: TYPE_VALUE stores a number; TYPE_STACK stores a
// reference to another stack.
typedef enum {
    TYPE_VALUE,
    TYPE_STACK
} elem_type_t;


// A node in the linked list of stack elements.
typedef struct elem {
    // Element type.
    elem_type_t type;

    union {
        // Numeric value for a TYPE_VALUE element.
        uint64_t value;

        // Reference to another stack for a TYPE_STACK element.
        struct rstack *stack;
    } data;

    // Pointer to the next element below this one.
    struct elem* next;
} elem_t;

// States used to detect and reclaim unreachable stacks, including cycles kept
// alive only by references from other unreachable stacks.
typedef enum {
    // Not a candidate for collection.
    COLOR_BLACK,

    // Currently being examined.
    COLOR_GRAY,

    // Unreachable stack.
    COLOR_WHITE,

    // Unreachable stack already queued for collection.
    COLOR_PURPLE
} color_t;


// Recursive stack representation.
struct rstack {
    // Total number of references to this stack.
    size_t refcount;

    // Head of the linked list of stack elements.
    elem_t *top;

    // Visited flag used when traversing the stack graph with DFS.
    bool visited;

    // Current state in the cycle collection algorithm.
    color_t color;

    // Whether this stack is on the current file-writing recursion path.
    bool in_write;

    // Next unreachable stack queued for collection.
    struct rstack *collect_next;
};


rstack_t *rstack_new() {
    rstack_t *rs = malloc(sizeof(rstack_t));

    if (rs == nullptr) {
        errno = ENOMEM;
        return nullptr;
    }

    // The caller owns the initial reference.
    rs->refcount = 1;

    // The stack initially has no elements.
    rs->top = nullptr;

    // Initially unvisited by DFS.
    rs->visited = false;

    // The initial collection state is COLOR_BLACK.
    rs->color = COLOR_BLACK;

    // Initially outside the file-writing recursion path.
    rs->in_write = false;

    // Initially not queued for collection.
    rs->collect_next = nullptr;

    return rs;
}


int rstack_push_value(rstack_t *rs, uint64_t value) {
    if (rs == nullptr) {
        errno = EINVAL;
        return -1;
    }

    // Allocate a new stack element.
    elem_t *e = malloc(sizeof(elem_t));
    if (e == nullptr) {
        errno = ENOMEM;
        
        return -1;
    }

    // The new element stores a numeric value.
    e->type = TYPE_VALUE;
    e->data.value = value;

    // Link the new element to the previous top of the stack.
    e->next = rs->top;

    // Update the top of the stack.
    rs->top = e;

    return 0;
}


int rstack_push_rstack(rstack_t *rs1, rstack_t *rs2) {
    if (rs1 == nullptr || rs2 == nullptr) {
        errno = EINVAL;
        return -1;
    }

    // Allocate a new stack element.
    elem_t *e = malloc(sizeof(elem_t));
    if (e == nullptr) {
        errno = ENOMEM;
        return -1;
    }

    // The new element stores a reference to another stack.
    e->type = TYPE_STACK;
    e->data.stack = rs2;

    // Link the new element to the previous top of rs1.
    e->next = rs1->top;

    // Update the top of rs1.
    rs1->top = e;

    // The pushed stack rs2 gains a reference.
    rs2->refcount++;

    return 0;
}

// rstack_empty_dfs() searches the reference graph using DFS.
// rstack_clear_visited() resets the visited flags afterwards.

static bool rstack_empty_dfs(rstack_t *rs) {
    if (rs == nullptr) {
        return true;
    }

    // Mark this stack as visited to avoid revisiting it.
    rs->visited = true;

    // Iterate over the stack elements.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        // A numeric element means the stack is not recursively empty.
        if (e->type == TYPE_VALUE) {
            return false;
        }

        // Recursively explore referenced stacks that have not been visited yet.
        if (e->type == TYPE_STACK) {
            // The referenced child stack.
            rstack_t *child = e->data.stack;

            // Skip visited stacks to avoid looping through cycles.
            if (child != nullptr && !child->visited) {
                // A number in a child stack also makes this stack nonempty.
                if (!rstack_empty_dfs(child)) {
                    return false;
                }
            }
        }
    }

    // No number was found in this stack or in any reachable child stack.
    return true;
}

static void rstack_clear_visited(rstack_t *rs) {
    if (rs == nullptr) {
        return;
    }

    // Reset this stack's visited flag.
    rs->visited = false;

    // Iterate over the stack elements.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        if (e->type == TYPE_STACK) {
            // The referenced child stack.
            rstack_t *child = e->data.stack;

            // Recursively clear the flag of each visited child stack.
            if (child != nullptr && child->visited) {
                rstack_clear_visited(e->data.stack);
            }
        }
    }
}

bool rstack_empty(rstack_t *rs) {
    // Search recursively for a number.
    bool result = rstack_empty_dfs(rs);

    // Reset the visited flags throughout the traversed graph.
    rstack_clear_visited(rs);

    // Return the search result.
    return result;
}


// rstack_front_dfs() searches the reference graph; rstack_clear_visited(), also
// used by rstack_empty(), resets the visited flags afterwards.
static result_t rstack_front_dfs(rstack_t *rs) {
    if (rs == nullptr) {
        return (result_t){ .flag = false, .value = 0 };
    }

    // Mark this stack as visited to avoid revisiting it.
    rs->visited = true;

    // Iterate over the stack elements.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        // Return the first numeric element encountered.
        if (e->type == TYPE_VALUE) {
            return (result_t){ .flag = true, .value = e->data.value };
        }

        // Recursively explore referenced stacks that have not been visited yet.
        if (e->type == TYPE_STACK) {
            // The referenced child stack.
            rstack_t *child = e->data.stack;

            // Skip visited stacks to avoid looping through cycles.
            if (child != nullptr && !child->visited) {
                result_t sub = rstack_front_dfs(child);

                // Return the first number found in the child stack's DFS
                // traversal.
                if (sub.flag) {
                    return sub;
                }
            }
        }
    }

    // No number was found in this stack or in any reachable child stack.
    return (result_t){ .flag = false, .value = 0 };
}

result_t rstack_front(rstack_t *rs) {
    // Find the first number in a top-first DFS traversal.
    result_t result = rstack_front_dfs(rs);

    // Reset the visited flags throughout the traversed graph.
    rstack_clear_visited(rs);

    // Return the search result.
    return result;
}

// The helpers below implement destruction and cycle collection.
// rstack_destroy() frees a stack and releases its references to child stacks.
static void rstack_destroy(rstack_t *rs);
// rstack_trial_unlink() performs phase 1: temporarily decrement the reference
// count for each edge in the reachable graph and mark visited stacks gray.
static void rstack_trial_unlink(rstack_t *rs);
// rstack_check() performs phase 2: a positive remaining reference count
// triggers restoration of the reachable subgraph. Otherwise, mark the stack
// white and recursively examine its children.
static void rstack_check(rstack_t *rs);
// rstack_restore() supports phase 2 by restoring temporarily decremented
// reference counts in the live subgraph and marking its stacks black.
static void rstack_restore(rstack_t *rs);
// rstack_collect_mark() supports phase 3 by traversing white stacks and linking
// them into the collection list.
static void rstack_collect_mark(rstack_t *rs, rstack_t **head);
// rstack_collect() performs phase 3: reclaim unreachable stacks marked white.
static void rstack_collect(rstack_t *rs);


void rstack_delete(rstack_t *rs) {
    if (rs == nullptr) { 
        return;
    }

    // Release one reference.
    rs->refcount--;

    // Destroy the stack immediately if no references remain.
    if (rs->refcount == 0) {
        rstack_destroy(rs);
    } 
    
    else {
        // Phase 1: temporarily remove internal references.
        rstack_trial_unlink(rs);

        // Phase 2: identify live and unreachable stacks.
        rstack_check(rs);

        // Phase 3: reclaim unreachable stacks.
        rstack_collect(rs);
    }
}

static void rstack_destroy(rstack_t *rs) {
    if (rs == nullptr) { 
        return;
    }

    // Detach the list of elements before freeing it.
    elem_t *e = rs->top;
    rs->top = nullptr;

    // Free each element, starting from the top.
    while (e != nullptr) {
        // Save the next element before freeing the current one.
        elem_t *next = e->next;

        // A referenced child stack loses one reference when this element is
        // removed.

        if (e->type == TYPE_STACK) {
            rstack_delete(e->data.stack);
        }

        free(e);
        e = next;
    }

    // After freeing its elements, free the stack itself.
    free(rs);
}


// Phase 1 of cycle collection: temporarily remove internal references.
static void rstack_trial_unlink(rstack_t *rs) {
    if (rs == nullptr) {
        return;
    }

    // Do not revisit gray stacks; this prevents infinite recursion through
    // cycles.
    if (rs->color == COLOR_GRAY) {
        return;
    }

    // Mark the stack gray.
    rs->color = COLOR_GRAY;

    // Iterate over the stack elements.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        if (e->type == TYPE_STACK) {
            // Temporarily remove this reference to the child stack, then
            // explore it recursively.
            rstack_t *child = e->data.stack;
            child->refcount--;
            rstack_trial_unlink(child);
        }
    }
}


static void rstack_restore(rstack_t *rs) {
    if (rs == nullptr) {
        return;
    }

    // A black stack has already been restored.
    if (rs->color == COLOR_BLACK) {
        return;
    }

    // Restore the stack's black state.
    rs->color = COLOR_BLACK;

    // Iterate over the stack elements.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        if (e->type == TYPE_STACK) {
            rstack_t *child = e->data.stack;
            child->refcount++;
            rstack_restore(child);
        }
    }
}


static void rstack_check(rstack_t *rs) {
    if(rs == nullptr) {
        return;
    }

    // A stack that is no longer gray has already been processed.
    if (rs->color != COLOR_GRAY) {
        return;
    }

    // A positive remaining reference count means this subgraph must be
    // restored.
    if (rs->refcount > 0) {
        rstack_restore(rs);
        return;
    }

    // Otherwise, mark the stack white as a collection candidate.
    rs->color = COLOR_WHITE;

    // Continue phase 2 through the stack's outgoing references.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        // Recursively examine each referenced stack.
        if (e->type == TYPE_STACK) {
            rstack_check(e->data.stack);
        }
    }
}


static void rstack_collect_mark(rstack_t *rs, rstack_t **head) {
    if (rs == nullptr || rs->color != COLOR_WHITE) {
        return;
    }

    // Mark the stack as queued to avoid revisiting it through a cycle.
    rs->color = COLOR_PURPLE;

    // Prepend the stack to the collection list.
    rs->collect_next = *head;
    *head = rs;

    // Iterate over the stack elements.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        if (e->type == TYPE_STACK) {
            // The referenced child stack.
            rstack_t *child = e->data.stack;

            // Recursively collect each existing white child stack.
            if (child != nullptr && child->color == COLOR_WHITE) {
                rstack_collect_mark(child, head);
            }
        }
    }
}


static void rstack_collect(rstack_t *rs) {
    // Head of the list of stacks to reclaim.
    rstack_t *head = nullptr;

    // Gather all reachable white stacks into the collection list.
    rstack_collect_mark(rs, &head);

    // Iterate over the stacks queued for collection.
    for (rstack_t *p = head; p != nullptr; p = p->collect_next) {
        // The first element of the current stack.
        elem_t *e = p->top;
        
        // Iterate over the current stack's elements.
        while (e != nullptr) {
            // Save the next element.
            elem_t *next = e->next;

            // Free the current element.
            free(e);

            // Advance to the next element.
            e = next;
        }
    }

    // Iterate over the stacks queued for collection.
    while (head != nullptr) {
        // Save the next stack in the collection list.
        rstack_t *next = head->collect_next;

        // Free the current stack.
        free(head);

        // Advance to the next stack.
        head = next;
    }
}

void rstack_pop(rstack_t *rs) {
    // Do nothing if the pointer is null or the stack has no top-level elements.
    if (rs == nullptr || rs->top == nullptr) {
        return;
    }

    // Save the current top element.
    elem_t *e = rs->top;
    
    // Make the following element the new top.
    rs->top = e->next;

    // If the removed element references a stack, release that reference.
    if (e->type == TYPE_STACK) {
        rstack_delete(e->data.stack);
    }

    free(e);
}



rstack_t *rstack_read(char const *path) {
    // Reject a null path and set errno accordingly.
    if (path == nullptr) {
        errno = EINVAL;

        return nullptr;
    }

    // Preserve the incoming errno on success.
    int entry_errno = errno;

    // Open the file for reading in text mode.
    FILE *f = fopen(path, "r");
    if (f == nullptr) {
        return nullptr;
    }

    // Create an empty stack for the values read from the file.
    rstack_t *rs = rstack_new();

    // If allocation fails, close the file and preserve the error from
    // rstack_new().
    if (rs == nullptr) {
        int saved_errno = errno;
        fclose(f);
        errno = saved_errno;
        return nullptr;
    }

    // The current character read from the file.
    int c;

    // Whether a number is currently being parsed.
    bool in_number = false;

    // Value of the number being parsed.
    uint64_t value = 0;

    // Clear errno to detect an I/O failure that does not set it.
    errno = 0;

    // Read the file one character at a time.
    while ((c = fgetc(f)) != EOF) {
        // Convert to unsigned char for character classification.
        unsigned char uc = (unsigned char)c;

        // Handle whitespace.
        if (isspace(uc)) {
            // Whitespace terminates the current number; push its value onto the
            // stack.
            if (in_number) {
                // Handle a failed push.
                if (rstack_push_value(rs, value) == -1) {
                    // Preserve the allocation error reported by the push
                    // operation.
                    int saved_errno = errno;
                    
                    // Close the file.
                    fclose(f);

                    // Delete the partially built stack.
                    rstack_delete(rs);

                    // Restore the original error code.
                    errno = saved_errno;

                    return nullptr;
                }

                // The current number is complete.
                in_number = false;

                // Prepare to parse the next number.
                value = 0;
            }
        }
        
        // Handle a decimal digit.
        else if (isdigit(uc)) {
            // Numeric value of the current digit.
            uint64_t digit = (uint64_t)(uc - '0');

            // The first digit starts a new number.
            if (!in_number) {
                in_number = true;

                // Initialize the value with this digit.
                value = digit;
            } 

            // Append a digit to the number being parsed.
            else {
                // Appending a digit computes value * 10 + digit. Check for
                // uint64_t overflow before performing the operation.
                if (value > (UINT64_MAX - digit) / 10) {
                    // Close the file.
                    fclose(f);

                    // Delete the partially built stack.
                    rstack_delete(rs);

                    // Report an out-of-range value.
                    errno = ERANGE;

                    return nullptr;
                }

                // Append the digit once overflow has been ruled out.
                value = value * 10 + digit;
            }
        }

        // A character that is neither whitespace nor a digit makes the input
        // invalid.
        else {
            // Close the file.
            fclose(f);

            // Delete the partially built stack.
            rstack_delete(rs);

            // Report invalid input.
            errno = EINVAL;

            return nullptr;
        }
    }

    // Check whether reading stopped because of an I/O error.
    if (ferror(f)) {
        // Preserve the error code during cleanup.
        int saved_errno = errno;

        // Use a generic I/O error if the library did not set errno.
        if (saved_errno == 0) {
            saved_errno = EIO;
        }

        // Close the file.
        fclose(f);

        // Delete the partially built stack.
        rstack_delete(rs);

        // Restore the error code.
        errno = saved_errno;

        return nullptr;
    }

    // If EOF ends a number, push that final value onto the stack.
    if (in_number) {
        // Handle a failed push.
        if (rstack_push_value(rs, value) == -1) {
            // Preserve the error code during cleanup.
            int saved_errno = errno;

            // Close the file.
            fclose(f);

            // Delete the partially built stack.
            rstack_delete(rs);

            // Restore the error code.
            errno = saved_errno;

            return nullptr;
        }
    }

    // Close the file after reading and handle any failure.

    // Clear errno to detect a close failure that does not set it.
    errno = 0;

    if (fclose(f) != 0) {
        // Preserve the error code during cleanup.
        int saved_errno = errno;

        // Use a generic I/O error if the library did not set errno.
        if (saved_errno == 0) {
            saved_errno = EIO;
        }

        // Delete the newly built stack.
        rstack_delete(rs);

        // Restore the error code.
        errno = saved_errno;

        return nullptr;
    }
    
    // On success, restore the incoming errno and return the newly built stack.
    errno = entry_errno;

    return rs;
}



// The two helpers below implement the traversal.

// rstack_write_elems_from_bottom() writes a single stack's elements from bottom
// to top.
static int rstack_write_elems_from_bottom(FILE *f, elem_t *e, bool *stop);

// rstack_write_dfs() recursively traverses referenced stacks and writes their
// elements in bottom-to-top order.
static int rstack_write_dfs(FILE *f, rstack_t *rs, bool *stop);

// Write elements recursively, starting at the bottom of the list.
static int rstack_write_elems_from_bottom(FILE *f, elem_t *e, bool *stop) {
    // Do nothing at the end of the list or after a cycle has stopped writing.
    if (e == nullptr || *stop) {
        return 0;
    }

    // Visit the next element first to produce bottom-to-top output.
    if (e->next != nullptr) {
        if (rstack_write_elems_from_bottom(f, e->next, stop) == -1) {
            return -1;
        }
    }

    // Stop writing if a cycle was detected earlier.
    if (*stop) {
        return 0;
    }

    // Write each numeric value on a separate line.
    if (e->type == TYPE_VALUE) {
        // Preserve the incoming errno on success.
        int saved_errno = errno;

        // Clear errno to detect a library failure that does not set it.
        errno = 0;

        // Handle a failed write.
        if (fprintf(f, "%" PRIu64 "\n", e->data.value) < 0) {
            // Use a generic I/O error if the library did not set errno.
            if (errno == 0) {
                errno = EIO;
            }

            // Report failure.
            return -1;
        }

        // Restore the previous errno on success.
        errno = saved_errno;
    } 
    
    // For a stack reference, recursively write the child's elements in the same
    // order.
    else {
        // The referenced child stack.
        rstack_t *child = e->data.stack;

        if (rstack_write_dfs(f, child, stop) == -1) {
            return -1;
        }
    }

    return 0;
}


static int rstack_write_dfs(FILE *f, rstack_t *rs, bool *stop) {
    // Do nothing for a null stack or after a cycle has stopped writing.
    if (rs == nullptr || *stop) {
        return 0;
    }

    // A stack already on the active recursion path indicates a cycle. Stop all
    // further writing.
    if (rs->in_write) {
        *stop = true;
        return 0;
    }

    // Mark this stack as being written on the current recursion path.
    rs->in_write = true;

    // Write the stack's elements.
    int result = rstack_write_elems_from_bottom(f, rs->top, stop);

    // Remove this stack from the active writing path.
    rs->in_write = false;

    return result;
}


int rstack_write(char const *path, rstack_t *rs) {
    // Reject a null path or stack.
    if (path == nullptr || rs == nullptr) {
        // Report an invalid argument.
        errno = EINVAL;

        // Report failure.
        return -1;
    }

    // Preserve the incoming errno on success.
    int entry_errno = errno;

    // Open the file for writing.
    FILE *f = fopen(path, "w");

    // On failure, return the error reported by fopen().
    if (f == nullptr) {
        return -1;
    }

    // Whether a detected cycle has stopped writing.
    bool stop = false;

    // Start DFS from the supplied stack.
    int result = rstack_write_dfs(f, rs, &stop);

    // Handle an error during writing.
    if (result != 0) {
        // Preserve the error code.
        int saved_errno = errno;

        // Close the file.
        fclose(f);

        // Restore the original error code.
        errno = saved_errno;

        // Report failure.
        return -1;
    }

    // Clear errno to detect a fclose() failure that does not set it.
    errno = 0;

    // Close the file and handle any failure.
    if (fclose(f) != 0) {
        // Use a generic I/O error if the library did not set errno.
        if (errno == 0) {
            errno = EIO;
        }

        // Report failure.
        return -1;
    }

    // Restore the previous errno on success.
    errno = entry_errno;

    return 0;

}
