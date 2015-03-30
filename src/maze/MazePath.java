package maze;

import java.util.List;

public interface MazePath {
    List<Point> getPathList();

    boolean isExist();
}
