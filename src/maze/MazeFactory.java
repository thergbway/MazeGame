package maze;

import settings.Settings;

public class MazeFactory {
    private MazeFactory() {}

    public static Maze getMaze() {
        Maze maze = null;
        try {
            maze = (Maze) Class.forName(Settings.getMazeImplementationClassName()).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return maze;
    }
}
