# Recursive Graph Collections

A library written in C++23, with a C-compatible interface, for managing named graphs and building new graphs by replacing edges with other graphs.

The graphs are undirected. Each edge has a *level*, which records how deeply it was introduced through replacements. Ordinary edges start at level 0.

## Graph operations

A collection has a numeric ID and stores graphs under unique names. The API lets you create and delete collections or individual graphs, add and remove vertices and edges, and query graph sizes and levels.

Vertices are identified by non-negative integers. Deleting a vertex also removes its incident edges, but does not renumber the remaining vertices. When adding a new vertex, the library reuses the smallest available ID. There are also functions for iterating over graph names, vertex IDs and neighbors in sorted order (`rgc_first_*` / `rgc_next_*`).

## Replacing an edge

`rgc_replace_edge` is the main operation. It takes a *recipient* graph, an edge `(w1, w2)` to replace, and a *donor* graph with two chosen vertices `(v1, v2)`.

The function creates a **new** graph rather than modifying either input:

1. Copy the recipient graph and remove `(w1, w2)`.
2. Identify donor vertices `v1` and `v2` with `w1` and `w2`, respectively.
3. Create new vertices for the remaining donor vertices and copy the donor's edges using this mapping.
4. Assign the inserted edges level `max(replaced edge level, donor graph level) + 1`.

The operation is rejected if its arguments are invalid, the result name is already taken, or the new graph would exceed the vertex or level limits. The maximum edge level is `LEVEL_MAX - 1` (3 by default).

## Implementation

The internal representation uses `std::map` for vertices and graph names, and `std::set` for adjacency lists. An undirected edge is stored at both endpoints. Additional structures keep counts of edges by level and track deleted vertex IDs, so these do not have to be recomputed from scratch for every operation.

The implementation is in `recursive_graph_collections.cpp`. Public declarations are in `recursive_graph_collections.h`, which exposes C linkage and can be included from C or C++ code. In C++, the API is available in the `cxx` namespace.

## Compilation

Compile the library with a C++23-compatible compiler:

```bash
g++ -std=c++23 -Wall -Wextra -Wpedantic -c recursive_graph_collections.cpp
```

To change the maximum number of levels, for example to 16:

```bash
g++ -std=c++23 -DLEVEL_MAX=16 -c recursive_graph_collections.cpp
```

The command produces an object file that can be linked with a program using the library.