package maze;

public interface Maze {

    int getWidth();

    int getHeight();

    MazeBlock getBlockAt(int indexX, int indexY);

    MazeBlock getUpBlock(int indexX, int indexY);

    MazeBlock getDownBlock(int indexX, int indexY);

    MazeBlock getRightBlock(int indexX, int indexY);

    MazeBlock getLeftBlock(int indexX, int indexY);

    Point getFirstEmptyPoint();

    Point getLastEmptyPoint();

    Point getNextEmptyPoint(Point point);

    Point getPreviousEmptyPoint(Point point);

    boolean isCorrect();

}
