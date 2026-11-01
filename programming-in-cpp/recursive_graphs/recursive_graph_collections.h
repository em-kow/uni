#ifndef RECURSIVE_GRAPH_COLLECTIONS_HEADER
#define RECURSIVE_GRAPH_COLLECTIONS_HEADER

#include <sys/types.h>

#ifdef __cplusplus
namespace cxx {
extern "C" {
#endif

/// Creates a new, empty collection of named recursive graphs. Returns the
/// identifier of the collection. Each call returns a new identifier, starting
/// from 0 up to LONG_MAX. Returns -1 if all identifiers have been exhausted.
long rgc_new_collection();

/// If a collection of named recursive graphs with the given identifier id
/// exists, deletes it. Otherwise, does nothing.
void rgc_delete_collection(long id);

/// If a collection with identifier id exists, name is not nullptr and points
/// to a valid graph name not already present in the collection, and n is
/// non-negative, creates a new graph with the given name, n vertices, and no
/// edges. If n is positive, the vertices are numbered from 0 to n - 1.
/// Returns true if the graph was created, and false otherwise.
bool rgc_new_graph(long id, char const *name, int n);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name, deletes that graph.
/// Otherwise, does nothing.
void rgc_delete_graph(long id, char const *name);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name with fewer than INT_MAX vertices,
/// adds a new isolated vertex to the graph. The new vertex is assigned the
/// smallest non-negative number not currently used in the graph.
/// Returns the number of the new vertex if it was created, or -1 otherwise.
int rgc_graph_new_node(long id, char const *name);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name with a vertex numbered x, deletes
/// that vertex along with all its incident edges. The numbers of the remaining
/// vertices are unchanged. Returns true if the vertex was deleted, and false
/// otherwise.
bool rgc_graph_delete_node(long id, char const *name, int x);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name with two distinct vertices numbered
/// x and y that are not connected by an edge, adds the edge {x, y} with
/// level 0. Returns true if the edge was added, and false otherwise.
bool rgc_graph_add_edge(long id, char const *name, int x, int y);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name with an edge {x, y}, deletes that
/// edge. Returns true if the edge was deleted, and false otherwise.
bool rgc_graph_delete_edge(long id, char const *name, int x, int y);

/// If a non-empty collection with identifier id exists, returns a pointer
/// to the name of the first graph in the collection. Otherwise, returns
/// nullptr.
char const *rgc_first_graph(long id);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph that comes after the graph named name in the
/// ordering, returns a pointer to the name of that graph. Otherwise, returns
/// nullptr.
char const *rgc_next_graph(long id, char const *name);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name with at least one vertex, returns
/// the smallest vertex number present in that graph. Otherwise, returns -1.
int rgc_first_node(long id, char const *name);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name with a vertex numbered greater
/// than x, returns the smallest number of such a vertex.
/// Otherwise, returns -1.
int rgc_next_node(long id, char const *name, int x);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name with a vertex numbered x that has
/// at least one neighbor, returns the smallest number among its neighbors.
/// Otherwise, returns -1.
int rgc_first_edge(long id, char const *name, int x);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name with a vertex numbered x that has
/// a neighbor numbered greater than y, returns the smallest number greater
/// than y among the neighbors of x. Otherwise, returns -1.
int rgc_next_edge(long id, char const *name, int x, int y);

/// If a collection with identifier id exists, result_name, recipient_name,
/// and donor_name are not nullptr, result_name points to a valid graph name
/// not already present in the collection, the collection contains a recipient
/// graph named recipient_name and a donor graph named donor_name, the recipient
/// has an edge {w_1, w_2}, the donor has two distinct vertices v_1 and v_2,
/// the level of the resulting graph will be less than LEVEL_MAX, and the
/// number of vertices in the resulting graph will not exceed INT_MAX, creates
/// a new graph named result_name using the edge replacement procedure.
/// Returns true if the graph was created, and false otherwise.
bool rgc_replace_edge(long id, char const *result_name,
                      char const *recipient_name, int w_1, int w_2,
                      char const *donor_name, int v_1, int v_2);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name with an edge {x, y}, returns the
/// level of that edge. Otherwise, returns -1.
ssize_t rgc_edge_level(long id, char const *name, int x, int y);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name, returns the level of that graph.
/// Otherwise, returns -1.
ssize_t rgc_graph_level(long id, char const *name);

/// Returns the maximum allowed edge level, which is LEVEL_MAX - 1.
ssize_t rgc_max_level();

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name, returns the number of vertices
/// in that graph. Otherwise, returns -1.
ssize_t rgc_count_nodes(long id, char const *name);

/// If a collection with identifier id exists, name is not nullptr, and the
/// collection contains a graph named name, returns the number of edges
/// in that graph. Otherwise, returns -1.
ssize_t rgc_count_edges(long id, char const *name);

/// If a collection with identifier id exists, returns the number of graphs
/// in that collection. Otherwise, returns -1.
ssize_t rgc_size(long id);

#ifdef __cplusplus
}
} // namespace cxx
#endif

#endif