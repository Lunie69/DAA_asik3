/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
import java.util.Arrays;
import java.util.PriorityQueue;

public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet dsu = new DisjointSet(w.cells());
        int[] result = new int[Math.max(0, w.cells() - 1)];
        int used = 0;

        for (int k = 0; k < w.count(); k++) {
            if (dsu.union(w.a()[k], w.b()[k])) {
                result[used++] = k;
            }
        }

        return Arrays.copyOf(result, used);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        Integer[] order = new Integer[w.count()];

        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }

        Arrays.sort(order, (x, y) ->
                Integer.compare(w.cost()[x], w.cost()[y])
        );

        DisjointSet dsu = new DisjointSet(w.cells());
        int[] result = new int[Math.max(0, w.cells() - 1)];
        int used = 0;

        for (int k : order) {
            if (dsu.union(w.a()[k], w.b()[k])) {
                result[used++] = k;

                if (used == w.cells() - 1) {
                    break;
                }
            }
        }

        return Arrays.copyOf(result, used);
    }

    private record Edge(int from, int to, int cost) {}

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();
        int[] degree = new int[n];

        for (int i = 0; i < w.count(); i++) {
            degree[w.a()[i]]++;
            degree[w.b()[i]]++;
        }

        int[][] edgeIds = new int[n][];

        for (int i = 0; i < n; i++) {
            edgeIds[i] = new int[degree[i]];
        }

        Arrays.fill(degree, 0);

        for (int i = 0; i < w.count(); i++) {
            int a = w.a()[i];
            int b = w.b()[i];

            edgeIds[a][degree[a]++] = i;
            edgeIds[b][degree[b]++] = i;
        }

        PriorityQueue<Edge> pq = new PriorityQueue<>(
                (x, y) -> Integer.compare(x.cost(), y.cost())
        );

        boolean[] used = new boolean[n];
        used[0] = true;

        for (int id : edgeIds[0]) {
            int to = w.a()[id] == 0 ? w.b()[id] : w.a()[id];
            pq.add(new Edge(0, to, w.cost()[id]));
        }

        long total = 0;
        int visited = 1;

        while (!pq.isEmpty() && visited < n) {
            Edge e = pq.poll();

            if (used[e.to()]) {
                continue;
            }

            used[e.to()] = true;
            visited++;
            total += e.cost();

            for (int id : edgeIds[e.to()]) {
                int to = w.a()[id] == e.to()
                        ? w.b()[id]
                        : w.a()[id];

                if (!used[to]) {
                    pq.add(new Edge(e.to(), to, w.cost()[id]));
                }
            }
        }

        return total;
    }
}
