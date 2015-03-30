package settings;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Settings {
    private static Settings instance = null;
    private static final int ROWS = 28;
    private static final int COLUMNS = 28;
    private static final int IMAGE_SIZE = 25;
    private Properties props;

    static {
        instance = new Settings();
    }

    private Settings() {
        props = new Properties();

        try {
            props.load(getClass().getResourceAsStream("/properties.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static int getImageSize() {
        return IMAGE_SIZE;
    }

    public static int getMazeRows() {
        return ROWS;
    }

    public static int getMazeColumns() {
        return COLUMNS;
    }

    public static String getMazeImplementationClassName() {
        return instance.props.getProperty("maze.maze_implementation_class_name");
    }

    public static String getMazePathImplementationClassName() {
        return instance.props.getProperty("maze.maze_path_implementation_class_name");
    }

    public static int getWallPercentage() {
        return Integer.parseInt(instance.props.getProperty("maze.wall_percentage"));
    }

    public static String getMainFrameTitle() {
        return instance.props.getProperty("main_frame.title");
    }
}
