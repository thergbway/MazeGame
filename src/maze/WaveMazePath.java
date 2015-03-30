package maze;

import java.util.LinkedList;
import java.util.List;

/**
 * Класс, использующий при поиске пути волновой алгоритм
 */
public class WaveMazePath implements MazePath {
    private final boolean isPathExists;
    private List<Point> pathList = new LinkedList<Point>();

    public WaveMazePath(Maze maze, int startX, int startY, int finishX, int finishY) {
        isPathExists = generate(maze, startX, startY, finishX, finishY);
    }

    @Override
    public boolean isExist() {
        return isPathExists;
    }

    public List<Point> getPathList() {
        return pathList;
    }

    private boolean generate(Maze maze, int startX, int startY, int finishX, int finishY) {
        int width = maze.getWidth();
        int height = maze.getHeight();

        int[][] lengthMap = new int[height][width];
        //-1 - not visited
        //-2 - wall
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (maze.getBlockAt(i, j) == MazeBlock.WALL)
                    lengthMap[i][j] = -2;
                else
                    lengthMap[i][j] = -1;
            }
        }

        lengthMap[startX][startY] = 0;
        int currLength = 0;
        while (lengthMap[finishX][finishY] == -1 &&
                isPossibleToContinue(lengthMap)) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (lengthMap[i][j] == currLength)
                        addNewWavePart(lengthMap, i, j);
                }
            }
            ++currLength;
        }

        if (lengthMap[finishX][finishY] != -1) {
            int currX = finishX;
            int currY = finishY;

            pathList.add(new Point(currX, currY));

            while (currX != startX || currY != startY) {
                Point prevPoint = findLessClosestPoint(lengthMap, currX, currY);
                pathList.add(0, prevPoint);

                currX = prevPoint.x;
                currY = prevPoint.y;
            }

            return true;
        }
        return false;
    }

    private Point findLessClosestPoint(int[][] lengthMap, int x, int y) {
        if (x > 0)
            if (lengthMap[x - 1][y] == lengthMap[x][y] - 1)
                return new Point(x - 1, y);

        if (x < lengthMap.length - 1)
            if (lengthMap[x + 1][y] == lengthMap[x][y] - 1)
                return new Point(x + 1, y);


        if (y > 0)
            if (lengthMap[x][y - 1] == lengthMap[x][y] - 1)
                return new Point(x, y - 1);


        if (y < lengthMap[0].length - 1)
            if (lengthMap[x][y + 1] == lengthMap[x][y] - 1)
                return new Point(x, y + 1);

        return null;
    }

    private void addNewWavePart(int[][] lengthMap, int x, int y) {
        int newLengthValue = lengthMap[x][y] + 1;

        if (x > 0)
            if (lengthMap[x - 1][y] == -1)
                lengthMap[x - 1][y] = newLengthValue;

        if (x < lengthMap.length - 1)
            if (lengthMap[x + 1][y] == -1)
                lengthMap[x + 1][y] = newLengthValue;


        if (y > 0)
            if (lengthMap[x][y - 1] == -1)
                lengthMap[x][y - 1] = newLengthValue;


        if (y < lengthMap[0].length - 1)
            if (lengthMap[x][y + 1] == -1)
                lengthMap[x][y + 1] = newLengthValue;

    }

    private boolean isPossibleToContinue(int[][] lengthMap) {
        for (int i = 0; i < lengthMap.length; i++) {
            for (int j = 0; j < lengthMap[0].length; j++) {
                if (lengthMap[i][j] >= 0 &&
                        hasNotVisitedNeighbours(lengthMap, i, j))
                    return true;
            }
        }

        return false;
    }

    private boolean hasNotVisitedNeighbours(int[][] lengthMap, int x, int y) {
        if (x > 0)
            if (lengthMap[x - 1][y] == -1)
                return true;

        if (x < lengthMap.length - 1)
            if (lengthMap[x + 1][y] == -1)
                return true;

        if (y > 0)
            if (lengthMap[x][y - 1] == -1)
                return true;

        if (y < lengthMap[0].length - 1)
            if (lengthMap[x][y + 1] == -1)
                return true;

        return false;
    }
}
