# Assignment 3 — Maze Escape

**Course:** Design and Analysis of Algorithms  
**Language:** Java 17+

## 1. Project Overview

This project solves the Maze Escape assignment using Disjoint Set Union (DSU), minimum spanning tree algorithms, Depth-First Search (DFS), strongly connected components (SCCs), and dynamic programming on a directed acyclic graph (DAG).

The program processes two input files:

- `walls.txt` — the cells and walls connecting them, including wall costs.
- `slides.txt` — directed connections between cells, including travel times.

It prints the results of all missions and generates a maze visualization.

## 2. How to Run

Compile the Java source files:

```bash
javac -d out src/*.java
```

Generate the personal input files using your student ID:

```bash
java tools/MakeVariant.java YOUR_STUDENT_ID data
```

Replace `YOUR_STUDENT_ID` with your assigned student ID.

Run the program:

```bash
java -cp out Main
```

Run the provided checks:

```bash
java tools/Check.java
```

Run the Kruskal and Prim benchmark:

```bash
java -cp out Bench
```

The program generates the following visualization files:

- `output/maze.png` — an image of the maze and escape path.
- `output/maze.html` — an HTML visualization.

## 3. Implementation

### Mission 0 — Disjoint Set Union

The `DisjointSet` class maintains a collection of disjoint sets.

It implements:

- `find(x)` — returns the representative of the set containing `x`.
- `union(a, b)` — merges two sets and returns `false` if the elements are already connected.
- `connected(a, b)` — checks whether two elements belong to the same set.
- `count()` — returns the current number of disjoint sets.

The implementation uses **path compression** and **union by size**. Path compression shortens parent chains during `find`, while union by size attaches the smaller tree to the larger one.

With both optimizations, operations take amortized \(O(\alpha(V))\) time, where \(\alpha\) is the inverse Ackermann function and \(V\) is the number of cells.

### Mission 1 — Building the Maze

The `Maze.breakWalls()` method processes walls in their original input order. It uses DSU to check whether the two cells connected by a wall are already connected.

If `union()` returns `true`, the wall is broken. Otherwise, the wall is kept.

This procedure constructs a spanning tree of the grid graph.

**Why are exactly \(R \times C - 1\) walls broken?**

A connected graph with \(V\) vertices needs exactly \(V-1\) edges to form a spanning tree. Here, \(V=R \times C\). Each successfully broken wall connects two previously disconnected components, reducing the number of components by one. Therefore, exactly \(R \times C-1\) walls are broken.

**Why are exactly \((R-1)(C-1)\) walls kept?**

The rectangular grid contains

\[
E=R(C-1)+(R-1)C
\]

possible walls. The algorithm breaks \(V-1=RC-1\) of them. The remaining number is

\[
E-(V-1)=(R-1)(C-1).
\]

### Mission 2 — Cheapest Maze

The goal is to find a minimum spanning tree (MST) with the lowest total wall cost.

Two algorithms are implemented:

- **Kruskal's algorithm:** sorts the walls by cost and uses DSU to add an edge only when it connects two different components.
- **Prim's algorithm:** grows the spanning tree from the first cell, using a priority queue to select the lowest-cost available edge.

Both algorithms calculate the minimum spanning tree cost. The program compares their results and prints a warning if they differ.

### Mission 3 — Escape Path

The `Escape` class uses an iterative Depth-First Search (DFS) to find a path from the first cell to the last cell.

An explicit stack is used instead of recursive calls. This avoids stack overflow on large mazes, including a 1000 × 1000 maze.

The algorithm also tracks visited cells, counts dead ends, and reconstructs the path using a parent array.

**Why is there exactly one simple path between the entrance and exit?**

The maze produced in Mission 1 is a spanning tree: it is connected and contains no cycles. In a tree, there is exactly one simple path between any pair of vertices. Therefore, the entrance and exit have exactly one simple path.

### Mission 4 — Strongly Connected Components

The `Zones.kosaraju()` method implements Kosaraju's algorithm to identify strongly connected components (zones) in the directed slide graph.

It uses two iterative DFS passes:

1. The first pass explores the original graph and records vertices in finishing order.
2. The second pass processes vertices in reverse finishing order on the reversed graph. Each traversal identifies one strongly connected component.

The implementation uses explicit stacks, avoiding recursive DFS.

**Why is the graph reversed in the second pass?**

The first pass determines the order in which components should be processed. Traversing the reversed graph in decreasing finishing-time order allows each second-pass DFS to identify one complete strongly connected component without incorrectly merging it with another component.

**Why is the graph a DAG exactly when the number of zones equals the number of cells?**

A directed graph is acyclic if and only if every strongly connected component contains exactly one vertex, assuming there are no self-loops. If a directed cycle exists, its vertices belong to the same strongly connected component. Thus, the graph is a DAG exactly when the number of zones equals the number of cells.

### Mission 5 — Routes Between Zones

The `Zones.routes()` method builds a graph whose vertices are strongly connected components.

Edges inside the same zone are ignored. If multiple slides connect the same ordered pair of zones, only the slide with the lowest travel time is retained.

The resulting component graph is a DAG. Kosaraju's component numbering provides a topological order, so the algorithm processes zones in numerical order without an additional topological sort or priority queue.

During this pass, the program calculates:

- **Flow stages:** the maximum number of zones along a directed route, according to the implementation's stage count.
- **Fastest route cost:** the minimum total travel time from the zone containing the first cell to the zone containing the last cell.

Dynamic programming computes both values by updating reachable zones through their outgoing edges.

## 4. Correctness Summary

- **DSU:** maintains connected components and merges them only when necessary.
- **Maze construction:** each broken wall connects two different components, so no cycle is introduced. Processing the connected grid graph results in a spanning tree.
- **Kruskal:** processes edges in nondecreasing cost order and accepts only edges that do not create cycles, producing an MST.
- **Prim:** repeatedly adds the cheapest edge connecting the current tree to an unvisited vertex, producing an MST.
- **DFS:** searches the maze without revisiting cells and reconstructs the path using parent links.
- **Kosaraju:** identifies every strongly connected component using two DFS passes.
- **Zone routes:** processes the component DAG in topological order, allowing dynamic programming to calculate the route values.

## 5. Complexity Analysis

Let:

- \(V\) be the number of vertices (cells).
- \(E\) be the number of edges (walls or slides, depending on the mission).
- \(\alpha(V)\) be the inverse Ackermann function.

| Algorithm | Time complexity | Space complexity |
|---|---|---|
| DSU `find` / `union` | Amortized \(O(\alpha(V))\) per operation | \(O(V)\) for the DSU structure |
| Mission 1 — Wall breaking | \(O(E\alpha(V))\) | \(O(V)\) auxiliary space, excluding input and result |
| Kruskal — MST | \(O(E\log E)\) | \(O(V+E)\) |
| Prim — MST cost | \(O(E\log V)\) | \(O(V+E)\) |
| Mission 3 — Iterative DFS | \(O(V+E)\) | \(O(V)\) |
| Mission 4 — Kosaraju SCC | \(O(V+E)\) | \(O(V+E)\) |
| Mission 5 — Zone routes | \(O(V+E)\) | \(O(V+E)\) |

For the rectangular grid, the number of cells is \(V=R \times C\), and the number of possible walls is \(E=R(C-1)+(R-1)C\). Therefore, \(E=O(V)\) for a grid, and the graph traversal algorithms run in linear time with respect to the number of cells.

The MST complexities assume the standard adjacency and priority-queue operations used by the implementation.

## 6. Benchmark Results

The `Bench` program generates square mazes of three sizes. Each algorithm is executed five times, and the average runtime is reported.

| Maze size | Kruskal average (ms) | Prim average (ms) |
|---|---:|---:|
| 100 × 100 | 28 | 13 |
| 300 × 300 | 140 | 95 |
| 1000 × 1000 | 1255 | 1348 |

### Observations

Both algorithms take longer as the maze size increases because the number of cells and walls grows substantially.

Prim was faster in the measured runs for the 100 × 100 and 300 × 300 mazes, while Kruskal was slightly faster for the 1000 × 1000 maze. These results are specific to the tested environment and may vary with hardware, JVM behavior, and system load.

## 7. Output Visualization

After running `Main`, the program saves the maze image to:

`output/maze.png`

The HTML visualization is saved to:

`output/maze.html`

The PNG image should be committed to the repository as required by the assignment.

## 8. Testing

The project was compiled using Java and checked with the provided `Check.java` script.

The checks reported:

- Successful compilation.
- All `DisjointSetTest` checks passed.
- All provided examples (`tiny`, `small`, and `dag`) passed.

The benchmark was run for 100 × 100, 300 × 300, and 1000 × 1000 mazes, with five measurements per algorithm at each size.

## 9. AI Use

ChatGPT was used as a supporting tool to help understand the assignment requirements, discuss algorithm design and complexity, troubleshoot implementation issues, and organize the README documentation.

The implementation was compiled and tested locally, and the benchmark measurements reported above were collected by running the provided `Bench` program. The final submission was reviewed against the assignment requirements.

## 10. Conclusion

This project applies DSU, Kruskal's and Prim's minimum spanning tree algorithms, iterative DFS, Kosaraju's strongly connected components algorithm, and dynamic programming on a DAG.

The implementation avoids recursive graph traversals where large inputs could cause stack overflow, compares two MST approaches, and generates visual output for the constructed maze.
