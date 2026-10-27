import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Ready for you: Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph). The lists hold slide numbers i; the cell at the other end is s.to()[i]
 * or s.from()[i].
 */
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] out = Graphs.slidesFrom(s);
        int[][] in = Graphs.slidesInto(s);

        boolean[] seen = new boolean[n];
        int[] order = new int[n];
        int orderSize = 0;

        // First pass: DFS on the original graph, without recursion.
        int[] stack = new int[n];
        int[] nextIndex = new int[n];

        for (int start = 0; start < n; start++) {
            if (seen[start]) {
                continue;
            }

            int top = 0;
            stack[0] = start;
            nextIndex[0] = 0;
            seen[start] = true;

            while (top >= 0) {
                int u = stack[top];

                if (nextIndex[top] < out[u].length) {
                    int edge = out[u][nextIndex[top]++];
                    int v = s.to()[edge];

                    if (!seen[v]) {
                        seen[v] = true;
                        top++;
                        stack[top] = v;
                        nextIndex[top] = 0;
                    }
                } else {
                    order[orderSize++] = u;
                    top--;
                }
            }
        }

        // Second pass: process cells in reverse finishing order on the reversed graph.
        int[] zone = new int[n];
        Arrays.fill(zone, -1);

        int zoneCount = 0;
        int[] todo = new int[n];

        for (int i = orderSize - 1; i >= 0; i--) {
            int start = order[i];

            if (zone[start] != -1) {
                continue;
            }

            int top = 0;
            todo[0] = start;
            zone[start] = zoneCount;

            while (top >= 0) {
                int u = todo[top--];

                for (int edge : in[u]) {
                    int v = s.from()[edge];

                    if (zone[v] == -1) {
                        zone[v] = zoneCount;
                        todo[++top] = v;
                    }
                }
            }

            zoneCount++;
        }

        return zone;
    }

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        // Keep the cheapest slide for each ordered pair of different zones.
        Map<Long, Integer> cheapest = new HashMap<>();

        for (int i = 0; i < s.count(); i++) {
            int a = zoneOf[s.from()[i]];
            int b = zoneOf[s.to()[i]];

            if (a == b) {
                continue;
            }

            long key = ((long) a << 32) | (b & 0xffffffffL);
            Integer old = cheapest.get(key);

            if (old == null || s.time()[i] < old) {
                cheapest.put(key, s.time()[i]);
            }
        }

        @SuppressWarnings("unchecked")
        ArrayList<long[]>[] outgoing = new ArrayList[zoneCount];

        for (int i = 0; i < zoneCount; i++) {
            outgoing[i] = new ArrayList<>();
        }

        for (Map.Entry<Long, Integer> entry : cheapest.entrySet()) {
            long key = entry.getKey();
            int from = (int) (key >>> 32);
            int to = (int) key;

            outgoing[from].add(new long[] {to, entry.getValue()});
        }

        // Zone IDs are already a topological order.
        // stages[z] is the longest chain ending at zone z.
        // dist[z] is the cheapest cost from the start zone to zone z.
        long[] stages = new long[zoneCount];
        long[] dist = new long[zoneCount];

        Arrays.fill(stages, 1);
        Arrays.fill(dist, Long.MAX_VALUE / 4);

        int start = zoneOf[0];
        int finish = zoneOf[s.n() - 1];
        dist[start] = 0;

        long maxStages = zoneCount == 0 ? 0 : 1;

        for (int a = 0; a < zoneCount; a++) {
            maxStages = Math.max(maxStages, stages[a]);

            for (long[] edge : outgoing[a]) {
                int to = (int) edge[0];
                long cost = edge[1];

                stages[to] = Math.max(stages[to], stages[a] + 1);

                if (dist[a] < Long.MAX_VALUE / 4) {
                    dist[to] = Math.min(dist[to], dist[a] + cost);
                }
            }
        }

        for (long stage : stages) {
            maxStages = Math.max(maxStages, stage);
        }

        long fastestCost = dist[finish] >= Long.MAX_VALUE / 4
                ? 0
                : dist[finish];

        return new long[] {maxStages, fastestCost};
    }
}
