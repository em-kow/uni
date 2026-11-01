#include "recursive_graph_collections.h"

#include <algorithm>
#include <cassert>
#include <climits>
#include <cstring>
#include <functional>
#include <map>
#include <queue>
#include <regex>
#include <set>
#include <string>
#include <tuple>
#include <unordered_map>
#include <utility>
#include <vector>

#ifndef LEVEL_MAX
#define LEVEL_MAX 4
#endif

namespace {

constexpr ssize_t MAX_LEVEL = LEVEL_MAX;

// Node represents a node of a graph.
using Node = int;
// Edge represents an undirected edge in a graph. It contains a destination node
// and a level of the edge. Each edge is split into exactly 2 instances of
// Edge. They differ on the first coordinate with one containing one node and
// the second containing the other node.
using Edge = std::pair<Node, ssize_t>;
// Edges represents the set of all edges with a specific common node.
using Edges = std::set<Edge>;
// Graph represents a recursively created graph. Keys of the map represent nodes
// and the values are edges incident to the node (key).
using Graph = std::map<Node, Edges>;
// Min_node_queue is a helper container. It stores removed nodes from a graph in
// increasing order. It is used for calculating the id of a newly added node in
// a graph, which is the MEX of the nodes in the graph. Each Min_node_queue
// instance is linked to exactly one Graph instance.
using Min_node_queue =
    std::priority_queue<Node, std::vector<Node>, std::greater<Node>>;
// Descending_keys_map is a helper container. It is used for calculating the
// level of a graph, which is the maximal level of edges in a graph (or 0 if the
// graph doesn't have any edges). Each Descending_keys_map instance is linked to
// exactly one Graph instance.
using Descending_keys_map = std::map<ssize_t, ssize_t, std::greater<ssize_t>>;
// Graph_info is a container which has all information about a graph available
// fast. That is its nodes, edges, graph's level and the smallest id of a node
// not present in the graph.
using Graph_info = std::tuple<Graph, Descending_keys_map, Min_node_queue>;
// Collection represents a collection of graphs ordered lexicographically by
// their name (the key of the map).
using Collection = std::map<std::string, Graph_info>;

void change_map_value(Descending_keys_map &map, ssize_t key, ssize_t how_much) {
    if (how_much > 0) {
        map[key] += how_much;
    }
    else if (how_much < 0) {
        auto it_level = map.find(key);

        // Remove levels with no remaining edges, so the first map entry always
        // gives the highest level still present in the graph.
        if (it_level->second + how_much <= 0) {
            map.erase(it_level);
        }
        else {
            it_level->second += how_much;
        }
    }
}

std::string extract_and_validate_graph_name(char const *name) {
    // "" is an invalid graph name.
    if (name == nullptr) {
        return "";
    }

    static const std::regex GRAPH_NAME(R"([a-zA-Z0-9_]+)");
    // A correct graph's name is a non-empty string of small and large letters
    // of the English alphabet, digits and underscores.

    char const *end_of_name = std::strchr(name, 0);
    if (not std::regex_match(name, end_of_name, GRAPH_NAME)) {
        return "";
    }

    return std::string(name);
}

// Create the collections when first needed, so they are ready even if the
// library is called before main().
auto &get_collections() {
    // "collections" represents the set of all collections created by the user.
    static std::unordered_map<long, Collection> collections;

    return collections;
}

Collection *get_collection(long id) {
    auto &collections = get_collections();

    auto it = collections.find(id);

    if (it == collections.end()) {
        return nullptr;
    }

    return &(it->second);
}

Graph_info *get_graph_info(long id, char const *name,
                           Collection *collection = nullptr) {
    // Providing the optional "collection" pointer optimizes lookups when
    // querying multiple graphs from the same collection.

    if (collection == nullptr) {
        collection = get_collection(id);
    }
    std::string graph_name = extract_and_validate_graph_name(name);

    if (collection == nullptr or graph_name.empty()) {
        return nullptr;
    }

    auto it = collection->find(graph_name);

    if (it == collection->end()) {
        return nullptr;
    }

    return &(it->second);
}

Node graph_new_node(Graph_info *graph_info) {
    auto &[graph, level_data, deleted_nodes] = *graph_info;

    Node new_node;
    if (deleted_nodes.empty()) {
        new_node = static_cast<Node>(graph.size());
    }
    else {
        new_node = deleted_nodes.top();
        deleted_nodes.pop();
    }

    graph.insert({new_node, {}});

    return new_node;
}

bool graph_add_edge(Graph_info *graph_info, Node x, Node y) {
    auto &[graph, level_data, deleted_nodes] = *graph_info;

    auto it_x = graph.find(x);
    auto it_y = graph.find(y);

    if (x == y or it_x == graph.end() or it_y == graph.end()) {
        return false;
    }

    auto &edges_x = it_x->second;
    auto &edges_y = it_y->second;

    auto it_edge_x = edges_x.lower_bound(Edge{y, -1});
    if (it_edge_x != edges_x.end() and it_edge_x->first == y) {
        return false;
    }

    edges_x.insert({y, 0});
    edges_y.insert({x, 0});

    change_map_value(level_data, 0, 1);

    return true;
}

bool graph_delete_edge(Graph_info *graph_info, Node x, Node y) {
    auto &[graph, level_data, deleted_nodes] = *graph_info;

    auto it_x = graph.find(x);
    auto it_y = graph.find(y);

    if (x == y or it_x == graph.end() or it_y == graph.end()) {
        return false;
    }

    auto &edges_x = it_x->second;
    auto &edges_y = it_y->second;

    auto it_edge_x = edges_x.lower_bound(Edge{y, -1});
    auto it_edge_y = edges_y.lower_bound(Edge{x, -1});

    if (it_edge_x == edges_x.end() or it_edge_x->first != y) {
        return false;
    }

    assert(it_edge_x->second == it_edge_y->second);

    change_map_value(level_data, it_edge_x->second, -1);

    edges_x.erase(it_edge_x);
    edges_y.erase(it_edge_y);

    return true;
}

ssize_t edge_level(Graph_info const *graph_info, Node x, Node y) {
    auto &[graph, level_data, deleted_nodes] = *graph_info;

    auto it_x = graph.find(x);
    if (it_x == graph.end()) {
        return -1;
    }

    auto const &edges = it_x->second;
    auto it_y = edges.lower_bound({y, -1});
    if (it_y == edges.end() or it_y->first != y) {
        return -1;
    }

    return it_y->second;
}

ssize_t graph_level(Graph_info const *graph_info) {
    auto &[graph, level_data, deleted_nodes] = *graph_info;

    if (level_data.empty()) {
        return 0;
    }

    return level_data.begin()->first;
}

bool has_node(Graph const &graph, Node node) {
    return graph.find(node) != graph.end();
}

size_t size_of_joined_graph(Graph const &recipient_graph,
                            Graph const &donor_graph) {
    return recipient_graph.size() + donor_graph.size() - 2;
}

bool valid_replace_edge_data(Collection *collection,
                             std::string const &parsed_result_name,
                             Graph_info const *recipient_graph_info, Node w_1,
                             Node w_2, Graph_info const *donor_graph_info,
                             Node v_1, Node v_2) {
    if (collection == nullptr or recipient_graph_info == nullptr or
        donor_graph_info == nullptr or parsed_result_name.empty() or
        collection->find(parsed_result_name) != collection->end()) {
        return false;
    }

    auto &[recipient_graph, recipient_level_data, recipient_deleted_nodes] =
        *recipient_graph_info;
    auto &[donor_graph, donor_level_data, donor_deleted_nodes] =
        *donor_graph_info;

    ssize_t donor_level = graph_level(donor_graph_info);

    ssize_t replaced_edge_level = edge_level(recipient_graph_info, w_1, w_2);

    // The recipient must contain the edge to replace. The donor must contain
    // two distinct vertices to identify with the endpoints of that edge.
    // The resulting graph must have at most INT_MAX vertices.
    // Each new edge gets a level one higher than the maximum of the donor's
    // level and the replaced edge's level. This maximum must therefore be
    // strictly below the highest allowed edge level.
    // A donor with no edges adds no new edges, so no level check is needed.
    return (replaced_edge_level != -1) and v_1 != v_2 and
           has_node(donor_graph, v_1) and has_node(donor_graph, v_2) and
           (size_of_joined_graph(recipient_graph, donor_graph) <= INT_MAX) and
           (donor_level_data.empty() or
            std::max(donor_level, replaced_edge_level) < cxx::rgc_max_level());
}

// Add the donor's vertices and edges to the result graph. The result is already
// a copy of the recipient with the replaced edge removed.
void add_nodes_and_edges(Graph_info *result_graph_info, Node w_1, Node w_2,
                         Graph_info *donor_graph_info, Node v_1, Node v_2,
                         ssize_t new_edge_level) {
    auto &[donor_graph, donor_level_data, donor_deleted_nodes] =
        *donor_graph_info;
    auto &[result_graph, result_level_data, result_deleted_nodes] =
        *result_graph_info;

    // Put the selected vertices first and the remaining ones in increasing
    // order, as required by the replacement procedure.
    std::vector<Node> donor_nodes = {v_1, v_2};
    for (auto &[node, edges] : donor_graph) {
        if (node != v_1 && node != v_2) {
            donor_nodes.push_back(node);
        }
    }

    // Map v_1 and v_2 to w_1 and w_2. For each remaining donor vertex,
    // create a new vertex with the smallest unused number in the result.
    std::vector<Node> result_nodes = {w_1, w_2};
    for (size_t i = 3; i <= donor_nodes.size(); i++) {
        result_nodes.push_back(graph_new_node(result_graph_info));
    }

    std::unordered_map<Node, Node> nodes_mapping;
    for (size_t i = 0; i < donor_nodes.size(); i++) {
        nodes_mapping[donor_nodes[i]] = result_nodes[i];
    }

    ssize_t new_edges = 0;

    for (auto &[donor_v, donor_edge] : donor_graph) {
        Node result_v = nodes_mapping[donor_v];
        auto &result_edges = result_graph[result_v];

        for (auto &[donor_u, old_level] : donor_edge) {
            Node result_u = nodes_mapping[donor_u];

            result_edges.insert(Edge{result_u, new_edge_level});

            // Each undirected edge appears twice, but we count it only once.
            if (result_u < result_v) {
                new_edges++;
            }
        }
    }

    change_map_value(result_level_data, new_edge_level, new_edges);
}

} // namespace

namespace cxx {

long rgc_new_collection() {
    static long last_collection_index = -1;

    auto &collections = get_collections();

    if (last_collection_index == LONG_MAX) {
        return -1;
    }

    last_collection_index++;
    collections.insert({last_collection_index, {}});

    return last_collection_index;
}

void rgc_delete_collection(long id) {
    auto &collections = get_collections();

    collections.erase(id);
}

bool rgc_new_graph(long id, char const *name, Node n) {
    auto collection = get_collection(id);

    if (collection == nullptr or n < 0 or name == nullptr) {
        return false;
    }

    auto parsed_name = extract_and_validate_graph_name(name);

    if (parsed_name.empty() or collection->contains(parsed_name)) {
        return false;
    }

    Graph graph;
    for (Node i = 0; i < n; i++) {
        graph.insert({i, {}});
    }

    collection->insert({
        parsed_name,
        Graph_info{std::move(graph), Descending_keys_map{}, Min_node_queue{}}
    });

    return true;
}

void rgc_delete_graph(long id, char const *name) {
    auto collection = get_collection(id);

    std::string parsed_name = extract_and_validate_graph_name(name);

    if (collection != nullptr and not parsed_name.empty()) {
        collection->erase(parsed_name);
    }
}

Node rgc_graph_new_node(long id, char const *name) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return -1;
    }

    auto &[graph, level_data, deleted_nodes] = *graph_info;

    if (graph.size() >= static_cast<size_t>(INT_MAX)) {
        return -1;
    }

    return graph_new_node(graph_info);
}

bool rgc_graph_delete_node(long id, char const *name, Node x) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return false;
    }

    auto &[graph, level_data, deleted_nodes] = *graph_info;

    auto it_x = graph.find(x);

    if (it_x == graph.end()) {
        return false;
    }

    auto &edges_x = it_x->second;

    for (auto it_edge = edges_x.begin(); it_edge != edges_x.end(); it_edge++) {
        auto &[y, level] = *it_edge;

        change_map_value(level_data, level, -1);

        graph[y].erase(Edge{x, level});
    }

    deleted_nodes.emplace(x);

    graph.erase(it_x);

    // An empty graph can start numbering from zero again, so the stored
    // deleted vertex numbers are no longer needed.
    if (graph.empty()) {
        deleted_nodes = Min_node_queue{};
    }

    return true;
}

bool rgc_graph_add_edge(long id, char const *name, Node x, Node y) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return false;
    }

    return graph_add_edge(graph_info, x, y);
}

bool rgc_graph_delete_edge(long id, char const *name, Node x, Node y) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return false;
    }

    return graph_delete_edge(graph_info, x, y);
}

char const *rgc_first_graph(long id) {
    auto collection = get_collection(id);

    if (collection == nullptr or collection->empty()) {
        return nullptr;
    }

    return collection->begin()->first.c_str();
}

char const *rgc_next_graph(long id, char const *name) {
    auto collection = get_collection(id);

    if (collection == nullptr or collection->empty()) {
        return nullptr;
    }

    auto parsed_name = extract_and_validate_graph_name(name);

    auto it = collection->upper_bound(parsed_name);

    if (parsed_name.empty() or it == collection->end()) {
        return nullptr;
    }

    return it->first.c_str();
}

Node rgc_first_node(long id, char const *name) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return -1;
    }

    auto &[graph, level_data, deleted_nodes] = *graph_info;

    if (graph.empty()) {
        return -1;
    }

    return graph.begin()->first;
}

Node rgc_next_node(long id, char const *name, Node x) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return -1;
    }

    auto &[graph, level_data, deleted_nodes] = *graph_info;

    auto it = graph.upper_bound(x);

    if (it == graph.end()) {
        return -1;
    }

    return it->first;
}

Node rgc_first_edge(long id, char const *name, Node x) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return -1;
    }

    auto &[graph, level_data, deleted_nodes] = *graph_info;

    auto it_x = graph.find(x);

    if (it_x == graph.end()) {
        return -1;
    }

    auto &edges_x = it_x->second;

    if (edges_x.empty()) {
        return -1;
    }

    return edges_x.begin()->first;
}

Node rgc_next_edge(long id, char const *name, Node x, Node y) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return -1;
    }

    auto &[graph, level_data, deleted_nodes] = *graph_info;

    auto it_x = graph.find(x);

    if (it_x == graph.end() or y == INT_MAX) {
        return -1;
    }

    // Edge levels are non-negative, so {y + 1, -1} comes before every edge
    // whose neighbor is greater than y.
    auto &edges_x = it_x->second;
    auto it_y = edges_x.upper_bound({y + 1, -1});

    if (it_y == edges_x.end()) {
        return -1;
    }

    return it_y->first;
}

bool rgc_replace_edge(long id, char const *result_name,
                      char const *recipient_name, Node w_1, Node w_2,
                      char const *donor_name, Node v_1, Node v_2) {

    auto collection = get_collection(id);
    auto parsed_result_name = extract_and_validate_graph_name(result_name);

    auto recipient_graph_info = get_graph_info(id, recipient_name, collection);
    auto donor_graph_info = get_graph_info(id, donor_name, collection);

    if (not valid_replace_edge_data(collection, parsed_result_name,
                                    recipient_graph_info, w_1, w_2,
                                    donor_graph_info, v_1, v_2)) {
        return false;
    }

    // Creates a pair <parsed_result_name, {}> in the collection and then
    // changes the second component to a copy of the recipient graph.
    auto result_graph_info = &(*collection)[parsed_result_name];
    *result_graph_info = *recipient_graph_info;

    ssize_t replaced_edge_level = edge_level(result_graph_info, w_1, w_2);
    graph_delete_edge(result_graph_info, w_1, w_2);

    ssize_t donor_level = graph_level(donor_graph_info);
    ssize_t new_edge_level = std::max(replaced_edge_level, donor_level) + 1;

    add_nodes_and_edges(result_graph_info, w_1, w_2, donor_graph_info, v_1, v_2,
                        new_edge_level);

    return true;
}

ssize_t rgc_edge_level(long id, char const *name, Node x, Node y) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return -1;
    }

    return edge_level(graph_info, x, y);
}

ssize_t rgc_graph_level(long id, char const *name) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return -1;
    }

    return graph_level(graph_info);
}

ssize_t rgc_max_level() {
    return MAX_LEVEL - 1;
}

ssize_t rgc_count_nodes(long id, char const *name) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return -1;
    }

    auto &[graph, level_data, deleted_nodes] = *graph_info;

    return static_cast<ssize_t>(graph.size());
}

ssize_t rgc_count_edges(long id, char const *name) {
    auto graph_info = get_graph_info(id, name);

    if (graph_info == nullptr) {
        return -1;
    }

    auto &[graph, level_data, deleted_nodes] = *graph_info;

    ssize_t total = 0;
    for (auto &[level, count] : level_data) {
        total += count;
    }

    return total;
}

ssize_t rgc_size(long id) {
    auto collection = get_collection(id);

    if (collection == nullptr) {
        return -1;
    }

    return static_cast<ssize_t>(collection->size());
}

} // namespace cxx
