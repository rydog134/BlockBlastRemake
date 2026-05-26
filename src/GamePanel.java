import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int CELL_SIZE = 50;
    private static final int PREVIEW_CELL_SIZE = 35;

    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 8;

    // 1-Square
    private static final int[][] MONOMINO = {{0,0}};

    // 2-Squares (Dominoes)
    private static final int[][] DOMINO_H = {{0,0}, {0,1}};
    private static final int[][] DOMINO_V = {{0,0}, {1,0}};

    // 3-Squares (Originals & alternative orientations)
    private static final int[][] BLOCK_1  = {{0,0}, {0,1}, {0,2}};
    private static final int[][] TROMINO_V = {{0,0}, {1,0}, {2,0}};
    private static final int[][] BLOCK_2  = {{0,0}, {1,0}, {2,0}, {2,1}};

    // 4-Squares (Tetrominoes & orientations)
    private static final int[][] BLOCK_3   = {{0,0}, {0,1}, {1,0}, {1,1}};
    private static final int[][] TETRIS_I_V = {{0,0}, {1,0}, {2,0}, {3,0}};
    private static final int[][] TETRIS_T   = {{0,0}, {0,1}, {0,2}, {1,1}};
    private static final int[][] TETRIS_L   = {{0,0}, {1,0}, {2,0}, {0,1}};

    // 5-Squares (Pentominoes)
    private static final int[][] PENTO_I_H = {{0,0}, {0,1}, {0,2}, {0,3}, {0,4}};
    private static final int[][] PENTO_U   = {{0,0}, {2,0}, {0,1}, {1,1}, {2,1}};
    private static final int[][] PENTO_X   = {{1,0}, {0,1}, {1,1}, {2,1}, {1,2}};

    private static final int[][][] SHAPE_POOL = {
            MONOMINO,
            DOMINO_H, DOMINO_V,
            BLOCK_1, TROMINO_V, BLOCK_2,
            BLOCK_3, TETRIS_I_V, TETRIS_T, TETRIS_L,
            PENTO_I_H, PENTO_U, PENTO_X
    };

    // --- NEW FOR VERSION 11: STATIC CHOSEN PALETTE ARRAY ---
    // Cyan is used for blue to visually contrast clearly against the window's solid blue background
    private static final Color[] COLOR_PALETTE = {
            Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.PINK
    };

    // --- UPDATED FOR VERSION 11: BOOLEAN CONVERTED TO COLOR INSTANCE MATRIX ---
    // null = empty cell, non-null Color object = filled cell holding that specific color
    private Color[][] grid = new Color[GRID_ROWS][GRID_COLS];

    private final int block1HomeX = 75;
    private final int block2HomeX = 250;
    private final int block3HomeX = 425;
    private int blockHomeY;

    private int selectedBlock = -1;
    private int dragX = 0;
    private int dragY = 0;

    private int mouseOffsetX = 0;
    private int mouseOffsetY = 0;

    private boolean[] blockPlaced = new boolean[4];

    private int[][] activeBlock1;
    private int[][] activeBlock2;
    private int[][] activeBlock3;

    // --- NEW FOR VERSION 11: TRACKING STORAGE FOR RANDOMLY ASSIGNED SLOT COLORS ---
    private Color colorBlock1;
    private Color colorBlock2;
    private Color colorBlock3;

    private int score = 0;

    public GamePanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
        randomizeBlocks();
    }

    private void randomizeBlocks() {
        int index1 = (int) (Math.random() * SHAPE_POOL.length);
        int index2 = (int) (Math.random() * SHAPE_POOL.length);
        int index3 = (int) (Math.random() * SHAPE_POOL.length);

        activeBlock1 = SHAPE_POOL[index1];
        activeBlock2 = SHAPE_POOL[index2];
        activeBlock3 = SHAPE_POOL[index3];

        // --- NEW FOR VERSION 11: CHOOSE AND RETAIN INDEPENDENT RANDOM COLORS ---
        colorBlock1 = COLOR_PALETTE[(int) (Math.random() * COLOR_PALETTE.length)];
        colorBlock2 = COLOR_PALETTE[(int) (Math.random() * COLOR_PALETTE.length)];
        colorBlock3 = COLOR_PALETTE[(int) (Math.random() * COLOR_PALETTE.length)];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLUE);
        g.fillRect(0, 0, getWidth(), getHeight());

        int gridWidth = GRID_COLS * CELL_SIZE;
        int gridHeight = GRID_ROWS * CELL_SIZE;
        int startX = (getWidth() - gridWidth) / 2;
        int startY = (getHeight() - gridHeight) / 2;

        blockHomeY = startY + gridHeight + 20;

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("Score: " + score, startX, startY - 30);

        // --- UPDATED FOR VERSION 11: READ COLOR OBJECT FROM GRID CELL OR RENDER WHITE ---
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int x = startX + col * CELL_SIZE;
                int y = startY + row * CELL_SIZE;

                if (grid[row][col] != null) {
                    g.setColor(grid[row][col]); // Use the exact color object stored in this cell
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                } else {
                    g.setColor(Color.WHITE);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // --- UPDATED FOR VERSION 11: PASS SLOT SPECIFIC COLOR VALUES TO DRAWBLOCK ---
        if (!blockPlaced[1]) {
            if (selectedBlock == 1) {
                drawBlock(g, activeBlock1, dragX, dragY, CELL_SIZE, colorBlock1);
            } else {
                drawBlock(g, activeBlock1, block1HomeX, blockHomeY, PREVIEW_CELL_SIZE, colorBlock1);
            }
        }

        if (!blockPlaced[2]) {
            if (selectedBlock == 2) {
                drawBlock(g, activeBlock2, dragX, dragY, CELL_SIZE, colorBlock2);
            } else {
                drawBlock(g, activeBlock2, block2HomeX, blockHomeY, PREVIEW_CELL_SIZE, colorBlock2);
            }
        }

        if (!blockPlaced[3]) {
            if (selectedBlock == 3) {
                drawBlock(g, activeBlock3, dragX, dragY, CELL_SIZE, colorBlock3);
            } else {
                drawBlock(g, activeBlock3, block3HomeX, blockHomeY, PREVIEW_CELL_SIZE, colorBlock3);
            }
        }
    }

    // --- UPDATED FOR VERSION 11: ADDED DYNAMIC COLOR ARGUMENT ---
    private void drawBlock(Graphics g, int[][] shape, int anchorX, int anchorY, int size, Color blockColor) {
        for (int[] cell : shape) {
            int x = anchorX + cell[1] * size;
            int y = anchorY + cell[0] * size;
            g.setColor(blockColor); // Render with assigned custom palette color instead of forced orange
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, size, size);
        }
    }

    private boolean isBlockClicked(int[][] shape, int anchorX, int anchorY, int mx, int my, int size) {
        for (int[] cell : shape) {
            int cellX = anchorX + cell[1] * size;
            int cellY = anchorY + cell[0] * size;

            if (mx >= cellX && mx < cellX + size && my >= cellY && my < cellY + size) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        if (!blockPlaced[1] && isBlockClicked(activeBlock1, block1HomeX, blockHomeY, mx, my, PREVIEW_CELL_SIZE)) {
            selectedBlock = 1;
            mouseOffsetX = (mx - block1HomeX) * CELL_SIZE / PREVIEW_CELL_SIZE;
            mouseOffsetY = (my - blockHomeY) * CELL_SIZE / PREVIEW_CELL_SIZE;
        }
        else if (!blockPlaced[2] && isBlockClicked(activeBlock2, block2HomeX, blockHomeY, mx, my, PREVIEW_CELL_SIZE)) {
            selectedBlock = 2;
            mouseOffsetX = (mx - block2HomeX) * CELL_SIZE / PREVIEW_CELL_SIZE;
            mouseOffsetY = (my - blockHomeY) * CELL_SIZE / PREVIEW_CELL_SIZE;
        }
        else if (!blockPlaced[3] && isBlockClicked(activeBlock3, block3HomeX, blockHomeY, mx, my, PREVIEW_CELL_SIZE)) {
            selectedBlock = 3;
            mouseOffsetX = (mx - block3HomeX) * CELL_SIZE / PREVIEW_CELL_SIZE;
            mouseOffsetY = (my - blockHomeY) * CELL_SIZE / PREVIEW_CELL_SIZE;
        }

        if (selectedBlock != -1) {
            dragX = mx - mouseOffsetX;
            dragY = my - mouseOffsetY;
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedBlock != -1) {
            dragX = e.getX() - mouseOffsetX;
            dragY = e.getY() - mouseOffsetY;
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (selectedBlock == -1) return;

        int gridWidth = GRID_COLS * CELL_SIZE;
        int gridHeight = GRID_ROWS * CELL_SIZE;
        int startX = (getWidth() - gridWidth) / 2;
        int startY = (getHeight() - gridHeight) / 2;

        int[][] currentShape = (selectedBlock == 1) ? activeBlock1 : (selectedBlock == 2) ? activeBlock2 : activeBlock3;
        // --- NEW FOR VERSION 11: GET THE RELEVANT ACTIVE SLOT COLOR VALUE ---
        Color currentBlockColor = (selectedBlock == 1) ? colorBlock1 : (selectedBlock == 2) ? colorBlock2 : colorBlock3;

        int targetCol = Math.round((float) (dragX - startX) / CELL_SIZE);
        int targetRow = Math.round((float) (dragY - startY) / CELL_SIZE);

        boolean canPlace = true;

        for (int[] cell : currentShape) {
            int checkRow = targetRow + cell[0];
            int checkCol = targetCol + cell[1];

            if (checkRow < 0 || checkRow >= GRID_ROWS || checkCol < 0 || checkCol >= GRID_COLS) {
                canPlace = false;
                break;
            }
            // --- UPDATED FOR VERSION 11: CHECK IF CELL IS FILLED BY COMPARING TO NULL ---
            if (grid[checkRow][checkCol] != null) {
                canPlace = false;
                break;
            }
        }

        if (canPlace) {
            for (int[] cell : currentShape) {
                int fillRow = targetRow + cell[0];
                int fillCol = targetCol + cell[1];
                // --- UPDATED FOR VERSION 11: SAVE COLOR OBJECT INTO CELL RATHER THAN TRUE ---
                grid[fillRow][fillCol] = currentBlockColor;
            }

            score += currentShape.length;

            blockPlaced[selectedBlock] = true;

            boolean[] rowsToClear = new boolean[GRID_ROWS];
            boolean[] colsToClear = new boolean[GRID_COLS];

            for (int r = 0; r < GRID_ROWS; r++) {
                boolean rowFull = true;
                for (int c = 0; c < GRID_COLS; c++) {
                    // --- UPDATED FOR VERSION 11: CHECK FOR HOLES COMPARING TO NULL ---
                    if (grid[r][c] == null) {
                        rowFull = false;
                        break;
                    }
                }
                if (rowFull) rowsToClear[r] = true;
            }

            for (int c = 0; c < GRID_COLS; c++) {
                boolean colFull = true;
                for (int r = 0; r < GRID_ROWS; r++) {
                    // --- UPDATED FOR VERSION 11: CHECK FOR HOLES COMPARING TO NULL ---
                    if (grid[r][c] == null) {
                        colFull = false;
                        break;
                    }
                }
                if (colFull) colsToClear[c] = true;
            }

            for (int r = 0; r < GRID_ROWS; r++) {
                if (rowsToClear[r]) {
                    score += 10;
                }
            }
            for (int c = 0; c < GRID_COLS; c++) {
                if (colsToClear[c]) {
                    score += 10;
                }
            }

            for (int r = 0; r < GRID_ROWS; r++) {
                for (int c = 0; c < GRID_COLS; c++) {
                    if (rowsToClear[r] || colsToClear[c]) {
                        // --- UPDATED FOR VERSION 11: CLEAR CELL CODES BACK TO NULL ---
                        grid[r][c] = null;
                    }
                }
            }

            if (blockPlaced[1] && blockPlaced[2] && blockPlaced[3]) {
                blockPlaced[1] = false;
                blockPlaced[2] = false;
                blockPlaced[3] = false;

                randomizeBlocks();
            }
        }

        selectedBlock = -1;
        repaint();
    }

    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}