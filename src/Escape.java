
import java.util.Arrays;

/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The ready method Graphs.open(w, broken) gives you the open neighbours of every cell, in no particular order:
 * to visit them in the required order, go through the four cells above one by one and take those that are in the list.
 * The program must not overflow the stack on a maze of 1000 by 1000 cells.
 */
public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells();
        int cols = w.cols();
        int exit = n - 1;

        int[][] graph = Graphs.open(w, broken);

        int deadEnds = 0;
        for (int[] neighbours : graph) {
            if (neighbours.length == 1) {
                deadEnds++;
            }
        }

        boolean[] seen = new boolean[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        int[] stackCell = new int[n];
        int[] stackNext = new int[n];

        int top = 0;
        int visited = 1;

        stackCell[0] = 0;
        seen[0] = true;

        boolean found = exit == 0;

        while (top >= 0 && !found) {
            int cell = stackCell[top];
            int next = -1;

            while (stackNext[top] < 4 && next == -1) {
                int dir = stackNext[top]++;
                int r = cell / cols;
                int c = cell % cols;

                if (dir == 0 && r > 0) {
                    next = cell - cols;
                } else if (dir == 1 && c + 1 < cols) {
                    next = cell + 1;
                } else if (dir == 2 && r + 1 < w.rows()) {
                    next = cell + cols;
                } else if (dir == 3 && c > 0) {
                    next = cell - 1;
                }

                if (next != -1) {
                    boolean isOpen = false;

                    for (int v : graph[cell]) {
                        if (v == next) {
                            isOpen = true;
                            break;
                        }
                    }

                    if (!isOpen || seen[next]) {
                        next = -1;
                    }
                }
            }

            if (next == -1) {
                top--;
            } else {
                seen[next] = true;
                visited++;
                parent[next] = cell;

                if (next == exit) {
                    found = true;
                    break;
                }

                top++;
                stackCell[top] = next;
                stackNext[top] = 0;
            }
        }

        if (!found) {
            return new Result(new int[0], visited, deadEnds);
        }

        int length = 1;
        for (int x = exit; x != 0; x = parent[x]) {
            length++;
        }

        int[] path = new int[length];
        int x = exit;

        for (int i = length - 1; i >= 0; i--) {
            path[i] = x;

            if (x != 0) {
                x = parent[x];
            }
        }

        return new Result(path, visited, deadEnds);
    }
}
