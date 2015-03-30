package maze;

import settings.Settings;

import java.util.Arrays;
import java.util.Random;

class EllerMaze implements Maze {
    private final int blockAreaRows;
    private final int blockAreaColumns;
    private final int rows;
    private final int columns;
    private final Random rand = new Random();
    private final int wallPercentage;
    private final MazeBlock[][] blocks;

    public EllerMaze() {
        blockAreaRows = Settings.getMazeRows() / 2;
        blockAreaColumns = Settings.getMazeColumns() / 2;
        rows = blockAreaRows * 2 - 1;
        columns = blockAreaColumns * 2 - 1;
        wallPercentage = Settings.getWallPercentage();
        blocks = new MazeBlock[rows][columns];

        generateMaze();
    }

    @Override
    public boolean isCorrect() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if(blocks[i][j] == MazeBlock.WALL)
                    continue;

                Point startPoint = new Point(i, j);
                for (int k = i; k < rows; k++) {
                    for (int l = j + 1; l < columns; l++) {
                        if(blocks[k][l] == MazeBlock.WALL)
                            continue;

                        Point finishPoint = new Point(k, l);
                        MazePath mp = new WaveMazePath(this, startPoint.x, startPoint.y, finishPoint.x, finishPoint.y);
                        if(!mp.isExist())
                            return false;
                    }
                }
            }
        }

        return true;
    }

    private void generateMaze() {
        MazeBlockArea[][] tempBlocks = new MazeBlockArea[blockAreaRows][blockAreaColumns];

        GeneratedRowProps currentRowProps = generateFirstRowProps();

        for (int i = 0; i < blockAreaRows; i++) {
            if (i == 0) {//first row
                tempBlocks[i] = currentRowProps.getContentAsMazeBlockRow();
            } else {
                if (i == blockAreaRows - 1) {//the last row
                    currentRowProps = generateLastRowProps(currentRowProps);
                    tempBlocks[i] = currentRowProps.getContentAsMazeBlockRow();
                } else {//boundary row
                    currentRowProps = generateNextRowProps(currentRowProps);
                    tempBlocks[i] = currentRowProps.getContentAsMazeBlockRow();
                }
            }
        }

        for (int i = 0; i < tempBlocks.length; i++) {
            MazeBlockArea[] mazeBlockAreas = tempBlocks[i];
            for (int j = 0; j < mazeBlockAreas.length; j++) {
                MazeBlockArea mazeBlockArea = mazeBlockAreas[j];

                if (mazeBlockArea.getRightSide() == MazeBlock.WALL) {
                    blocks[i * 2][j * 2] = MazeBlock.EMPTY;
                    if (j != mazeBlockAreas.length - 1) {
                        blocks[i * 2][j * 2 + 1] = MazeBlock.WALL;
                        if (i != tempBlocks.length - 1) {
                            blocks[i * 2 + 1][j * 2 + 1] = MazeBlock.WALL;
                        }
                    }
                } else {
                    blocks[i * 2][j * 2] = MazeBlock.EMPTY;
                    if (j != mazeBlockAreas.length - 1)
                        blocks[i * 2][j * 2 + 1] = MazeBlock.EMPTY;
                }

                if (mazeBlockArea.getDownSide() == MazeBlock.WALL) {
                    if (i != tempBlocks.length - 1)
                        blocks[i * 2 + 1][j * 2] = MazeBlock.WALL;
                    if (i != tempBlocks.length - 1 && j != mazeBlockAreas.length - 1)
                        blocks[i * 2 + 1][j * 2 + 1] = MazeBlock.WALL;
                } else {
                    if (i != tempBlocks.length - 1)
                        blocks[i * 2 + 1][j * 2] = MazeBlock.EMPTY;
                }

                if (i != tempBlocks.length - 1 && j != mazeBlockAreas.length - 1
                        && blocks[i * 2 + 1][j * 2 + 1] == null)
                    blocks[i * 2 + 1][j * 2 + 1] = MazeBlock.WALL;
            }
        }

        deleteAllCycles();
    }

    private GeneratedRowProps generateFirstRowProps() {
        int[] rowSetIdentifiers = new int[blockAreaColumns];
        setNewSetIdentifiersIfNotInAnySet(rowSetIdentifiers);
        boolean[] rightSideWallFlags = generateRightSideWallFlags(rowSetIdentifiers);
        boolean[] downSideWallFlags = generateDownSideWallFlags(rowSetIdentifiers);

        return new GeneratedRowProps(rowSetIdentifiers, rightSideWallFlags, downSideWallFlags);
    }

    private GeneratedRowProps generateNextRowProps(GeneratedRowProps previousRowProps) {
        int[] rowSetIdentifiers = previousRowProps.rowSetIdentifiers.clone();
        boolean[] downSideWallFlags = previousRowProps.downSideWallFlags.clone();

        for (int i = 0; i < downSideWallFlags.length; i++) {
            boolean wallFlag = downSideWallFlags[i];

            if (wallFlag)
                rowSetIdentifiers[i] = 0;
        }

        setNewSetIdentifiersIfNotInAnySet(rowSetIdentifiers);
        boolean[] rightSideWallFlags = generateRightSideWallFlags(rowSetIdentifiers);
        downSideWallFlags = generateDownSideWallFlags(rowSetIdentifiers);

        return new GeneratedRowProps(rowSetIdentifiers, rightSideWallFlags, downSideWallFlags);
    }

    private GeneratedRowProps generateLastRowProps(GeneratedRowProps previousRowProps) {
        boolean[] downSideWallFlags = new boolean[blockAreaColumns];
        for (int i = 0; i < downSideWallFlags.length; i++) {
            downSideWallFlags[i] = true;
        }

        boolean[] rightSideWallFlags = previousRowProps.rightSideWallFlags.clone();
        int[] rowSetIdentifiers = previousRowProps.rowSetIdentifiers.clone();

        int currIdentifier = rowSetIdentifiers[0];
        for (int i = 1; i < rowSetIdentifiers.length; i++) {
            if (currIdentifier != rowSetIdentifiers[i]) {
                rightSideWallFlags[i - 1] = false;

                int identifierToReplace = rowSetIdentifiers[i];
                for (int j = i; j < rowSetIdentifiers.length; j++) {
                    if (rowSetIdentifiers[j] == identifierToReplace)
                        rowSetIdentifiers[j] = currIdentifier;
                }

                currIdentifier = rowSetIdentifiers[i];
            }
        }

        return new GeneratedRowProps(rowSetIdentifiers, rightSideWallFlags, downSideWallFlags);
    }

    private void setNewSetIdentifiersIfNotInAnySet(final int[] row) {
        for (int i = 0; i < row.length; i++) {
            if (row[i] == 0)
                row[i] = getNextSetIdentifier(row);
        }
    }

    private int getNextSetIdentifier(final int[] row) {
        int[] sortedIdentifiers = row.clone();
        Arrays.sort(sortedIdentifiers);
        int nextSetIdentifier = sortedIdentifiers[sortedIdentifiers.length - 1] + 1;

        if (nextSetIdentifier <= 0)
            throw new RuntimeException("Maze is too big");

        return nextSetIdentifier;
    }

    private boolean[] generateRightSideWallFlags(int[] row) {
        boolean[] flags = new boolean[blockAreaColumns];

        for (int i = 0; i < row.length - 1; i++) {
            int leftIdentifier = row[i];
            int rightIdentifier = row[i + 1];
            if (leftIdentifier == rightIdentifier)//if in the same set
                flags[i] = true;
            else {
                if (rand.nextInt(100) < wallPercentage)
                    flags[i] = true;
                else
                    row[i + 1] = leftIdentifier;//unite sets
            }
        }

        return flags;
    }

    private boolean[] generateDownSideWallFlags(int[] row) {
        boolean[] flags = new boolean[blockAreaColumns];

        int currSetIdentifier = row[0];
        boolean isOpened = false;
        for (int i = 0; i < flags.length; i++) {
            if (currSetIdentifier != row[i]) {//switched to next set
                if (!isOpened)
                    flags[i - 1] = false;
                currSetIdentifier = row[i];
                isOpened = false;
            }
            if (rand.nextInt(100) < wallPercentage) {
                flags[i] = true;
            } else {
                flags[i] = false;
                isOpened = true;
            }
        }
        if (!isOpened)//if the last set was closed
            flags[flags.length - 1] = false;

        return flags;
    }

    private void deleteAllCycles() {
        while (deleteOneCycle()) ;
    }

    private boolean deleteOneCycle() {
        VisitedBlock[][] visitedBlocks = new VisitedBlock[rows][columns];
        for (int i = 0; i < visitedBlocks.length; i++) {
            for (int j = 0; j < visitedBlocks[i].length; j++) {
                visitedBlocks[i][j] = VisitedBlock.NOT_VISITED;
            }
        }
        markAllWalls(visitedBlocks);

        visitedBlocks[0][0] = VisitedBlock.VISITED;

        Point pointToDelete = tryToFindCycle(visitedBlocks, new Point(0, 0));
        if (pointToDelete != null) {
            blocks[pointToDelete.x][pointToDelete.y] = MazeBlock.WALL;
            return true;
        } else
            return false;
    }

    private Point tryToFindCycle(VisitedBlock[][] visitedBlocks, Point p) {
        int indexX = p.x;
        int indexY = p.y;
        int visitedPointsFound = 0;
        Point firstPointToCheck = null;
        Point secondPointToCheck = null;
        Point thirdPointToCheck = null;
        Point fourthPointToCheck = null;

        if (indexY > 0)
            switch (visitedBlocks[indexX][indexY - 1]) {
                case WALL:
                    break;
                case VISITED:
                    ++visitedPointsFound;
                    if (visitedPointsFound == 2) {
                        return new Point(indexX, indexY - 1);
                    }
                    break;
                case NOT_VISITED:
                    visitedBlocks[indexX][indexY - 1] = VisitedBlock.VISITED;
                    firstPointToCheck = new Point(indexX, indexY - 1);
                    break;
            }

        if (indexY < columns - 1)
            switch (visitedBlocks[indexX][indexY + 1]) {
                case WALL:
                    break;
                case VISITED:
                    ++visitedPointsFound;
                    if (visitedPointsFound == 2) {
                        return new Point(indexX, indexY + 1);
                    }
                    break;
                case NOT_VISITED:
                    visitedBlocks[indexX][indexY + 1] = VisitedBlock.VISITED;
                    secondPointToCheck = new Point(indexX, indexY + 1);
                    break;
            }

        if (indexX > 0)
            switch (visitedBlocks[indexX - 1][indexY]) {
                case WALL:
                    break;
                case VISITED:
                    ++visitedPointsFound;
                    if (visitedPointsFound == 2) {
                        return new Point(indexX - 1, indexY);
                    }
                    break;
                case NOT_VISITED:
                    visitedBlocks[indexX - 1][indexY] = VisitedBlock.VISITED;
                    thirdPointToCheck = new Point(indexX - 1, indexY);
                    break;
            }

        if (indexX < rows - 1)
            switch (visitedBlocks[indexX + 1][indexY]) {
                case WALL:
                    break;
                case VISITED:
                    ++visitedPointsFound;
                    if (visitedPointsFound == 2) {
                        return new Point(indexX + 1, indexY);
                    }
                    break;
                case NOT_VISITED:
                    visitedBlocks[indexX + 1][indexY] = VisitedBlock.VISITED;
                    fourthPointToCheck = new Point(indexX + 1, indexY);
                    break;
            }

        //try to find cycles with the nearest blocks
        if (firstPointToCheck != null) {
            Point pointToResult = tryToFindCycle(visitedBlocks, firstPointToCheck);
            if (pointToResult != null)
                return pointToResult;
        }

        if (secondPointToCheck != null) {
            Point pointToResult = tryToFindCycle(visitedBlocks, secondPointToCheck);
            if (pointToResult != null)
                return pointToResult;
        }

        if (thirdPointToCheck != null) {
            Point pointToResult = tryToFindCycle(visitedBlocks, thirdPointToCheck);
            if (pointToResult != null)
                return pointToResult;
        }

        if (fourthPointToCheck != null) {
            Point pointToResult = tryToFindCycle(visitedBlocks, fourthPointToCheck);
            if (pointToResult != null)
                return pointToResult;
        }

        return null;
    }

    private void markAllWalls(VisitedBlock[][] visitedBlocks) {
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++) {
                if (blocks[i][j] == MazeBlock.WALL)
                    visitedBlocks[i][j] = VisitedBlock.WALL;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < blocks.length; i++) {
            MazeBlock[] blockRow = blocks[i];
            for (int j = 0; j < blockRow.length; j++) {
                MazeBlock block = blockRow[j];

                if (block == MazeBlock.WALL)
                    sb.append("X");
                else
                    sb.append("_");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public int getWidth() {
        return columns;
    }

    @Override
    public int getHeight() {
        return rows;
    }

    @Override
    public MazeBlock getBlockAt(int indexX, int indexY) {
        MazeBlock block = blocks[indexX][indexY];

        return block;
    }

    @Override
    public MazeBlock getUpBlock(int indexX, int indexY) {
        return blocks[indexX - 1][indexY];
    }

    @Override
    public MazeBlock getDownBlock(int indexX, int indexY) {
        return blocks[indexX + 1][indexY];
    }

    @Override
    public MazeBlock getRightBlock(int indexX, int indexY) {
        return blocks[indexX][indexY + 1];
    }

    @Override
    public MazeBlock getLeftBlock(int indexX, int indexY) {
        return blocks[indexX][indexY - 1];
    }

    @Override
    public Point getFirstEmptyPoint() {
        if (getBlockAt(0, 0) == MazeBlock.EMPTY)
            return new Point(0, 0);

        return getNextEmptyPoint(new Point(0, 0));
    }

    @Override
    public Point getNextEmptyPoint(Point p) {
        for (int i = p.x; i < blocks.length; i++) {
            MazeBlock[] mazeBlocks = blocks[i];
            for (int j = p.y + 1; j < mazeBlocks.length; j++) {
                MazeBlock mazeBlock = mazeBlocks[j];
                if (mazeBlock == MazeBlock.EMPTY)
                    return new Point(i, j);
            }
        }

        return null;
    }

    @Override
    public Point getLastEmptyPoint() {
        if (getBlockAt(rows - 1, columns - 1) == MazeBlock.EMPTY)
            return new Point(rows - 1, columns - 1);

        return getNextEmptyPoint(new Point(rows - 1, columns - 1));
    }

    @Override
    public Point getPreviousEmptyPoint(Point p) {
        for (int i = p.x; i >= 0; i--) {
            MazeBlock[] mazeBlocks = blocks[i];
            for (int j = p.y - 1; j >= 0; j--) {
                MazeBlock mazeBlock = mazeBlocks[j];
                if (mazeBlock == MazeBlock.EMPTY)
                    return new Point(i, j);
            }
        }

        return null;
    }

    private enum VisitedBlock {
        WALL, VISITED, NOT_VISITED
    }

    private class GeneratedRowProps {
        private final int[] rowSetIdentifiers;
        private final boolean[] rightSideWallFlags;
        private final boolean[] downSideWallFlags;

        private GeneratedRowProps(int[] rowSetIdentifiers, boolean[] rightSideWallFlags, boolean[] downSideWallFlags) {
            this.rowSetIdentifiers = rowSetIdentifiers;
            this.rightSideWallFlags = rightSideWallFlags;
            this.downSideWallFlags = downSideWallFlags;
        }

        private MazeBlockArea[] getContentAsMazeBlockRow() {
            MazeBlockArea[] mazeBlockAreas = new MazeBlockArea[blockAreaColumns];

            for (int i = 0; i < mazeBlockAreas.length; i++) {
                MazeBlock rightSide;
                MazeBlock downSide;

                if (rightSideWallFlags[i])
                    rightSide = MazeBlock.WALL;
                else
                    rightSide = MazeBlock.EMPTY;

                if (downSideWallFlags[i])
                    downSide = MazeBlock.WALL;
                else
                    downSide = MazeBlock.EMPTY;

                mazeBlockAreas[i] = new MazeBlockArea(rightSide, downSide);
            }

            mazeBlockAreas[mazeBlockAreas.length - 1].setRightSide(MazeBlock.WALL);

            return mazeBlockAreas;
        }
    }

    /**
     * Class that represents single block in maze.
     * It holds only information about its right and down side.
     */
    private class MazeBlockArea {
        private MazeBlock rightSide = MazeBlock.EMPTY;
        private MazeBlock downSide = MazeBlock.EMPTY;

        public MazeBlockArea(MazeBlock rightSide, MazeBlock downSide) {

            this.rightSide = rightSide;
            this.downSide = downSide;
        }

        public MazeBlock getRightSide() {
            return rightSide;
        }

        public void setRightSide(MazeBlock rightSide) {
            this.rightSide = rightSide;
        }

        public MazeBlock getDownSide() {
            return downSide;
        }

        public void setDownSide(MazeBlock downSide) {
            this.downSide = downSide;
        }
    }
}
