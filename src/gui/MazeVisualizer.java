package gui;

import maze.*;
import maze.Point;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MazeVisualizer extends JPanel {
    private Maze maze;
    private MazePath mazePath;
    private BufferedImage wallImg;
    private BufferedImage emptyImg;
    private BufferedImage pathImg;
    private BufferedImage startStopPathImg;
    private Point firstPoint;
    private Point secondPoint;
    private boolean isNextPointIsFirst = true;

    private int imageSize;

    public MazeVisualizer() {
        try {
            wallImg = ImageIO.read(getClass().getResourceAsStream("/res/wall.png"));
            emptyImg = ImageIO.read(getClass().getResourceAsStream("/res/empty.png"));
            pathImg = ImageIO.read(getClass().getResourceAsStream("/res/path.png"));
            startStopPathImg = ImageIO.read(getClass().getResourceAsStream("/res/path_start_stop.png"));

            if (wallImg.getWidth() != wallImg.getHeight() || emptyImg.getWidth() != emptyImg.getHeight()
                    || pathImg.getWidth() != pathImg.getHeight()) {
                imageSize = -1;
                throw new IOException("Images must be square");
            } else
                imageSize = wallImg.getWidth();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);

        if (maze == null)
            printInfo(graphics);
        else {
            paintMaze(maze, graphics);

            if (mazePath != null)
                paintPath(mazePath, graphics);

            paintStartFinishFlags(graphics);
        }
    }

    private void printInfo(Graphics graphics) {
        graphics.drawString("Добро пожаловать!", 5, 15);
        graphics.drawString("Для генерации нового лабиринта нажмите соответствующую кнопку.", 5, 35);
        graphics.drawString("Если вас устраивают выбранные автоматически точки для нахождения пути" +
                ", нажмите соответствующую кнопку,", 5, 55);
        graphics.drawString("иначе вы можете сами указать точку нажатием ЛКМ на соотвутствующей клетке", 5, 75);
    }

    private void paintMaze(Maze m, Graphics g) {
        int mazeHeight = m.getHeight();
        int mazeWidth = m.getWidth();

        for (int i = 0; i < mazeWidth; i++) {
            for (int j = 0; j < mazeHeight; j++) {
                MazeBlock block = m.getBlockAt(j, i);
                BufferedImage imgToDraw = null;
                switch (block) {
                    case EMPTY:
                        imgToDraw = emptyImg;
                        break;
                    case WALL:
                        imgToDraw = wallImg;
                        break;
                }

                g.drawImage(imgToDraw, i * imageSize, j * imageSize, null);
            }
        }
    }

    private void paintPath(MazePath mp, Graphics graphics) {
        java.util.List<Point> pathList = mp.getPathList();

        for (int i = 0; i < pathList.size(); i++) {
            Point point = pathList.get(i);
            graphics.drawImage(pathImg, point.y * imageSize, point.x * imageSize, null);
        }
    }

    private void paintStartFinishFlags(Graphics graphics) {
        graphics.drawImage(startStopPathImg, secondPoint.y * imageSize, secondPoint.x * imageSize, null);
        graphics.drawImage(startStopPathImg, firstPoint.y * imageSize, firstPoint.x * imageSize, null);
    }

    public void generateNewMaze() {
        maze = MazeFactory.getMaze();
        while (!maze.isCorrect()) {
            maze = MazeFactory.getMaze();
        }

        firstPoint = maze.getFirstEmptyPoint();
        secondPoint = maze.getLastEmptyPoint();

        mazePath = null;
        repaint();
    }

    public void generateNewPath() {
        if (maze == null)
            return;

        if (firstPoint == null)
            firstPoint = maze.getFirstEmptyPoint();

        if (secondPoint == null) {
            secondPoint = maze.getLastEmptyPoint();
        }

        mazePath = new WaveMazePath(maze, firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y);

        repaint();
    }

    public void addAppropriateFlagIfPossible(int x, int y) {

        if (maze == null)
            return;

        int indexX = x / imageSize;
        int indexY = y / imageSize;

        if (maze.getBlockAt(indexX, indexY) == MazeBlock.EMPTY) {
            if (isNextPointIsFirst) {
                firstPoint = new Point(indexX, indexY);
                isNextPointIsFirst = false;
            } else {
                secondPoint = new Point(indexX, indexY);
                isNextPointIsFirst = true;
            }
        }
        mazePath = null;

        repaint();
    }
}
