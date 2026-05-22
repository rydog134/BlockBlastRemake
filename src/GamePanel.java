import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int CELL_SIZE = 50;
    // --- NEW FOR VERSION 8: CELL SIZE FOR BLOCKS AT HALF THE AREA OF GRID CELLS (50x50/2 = 1250 -> sqrt ≈ 35) ---
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

    private boolean[][] grid = new boolean[GRID_ROWS][GRID_COLS];

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

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int x = startX + col * CELL_SIZE;
                int y = startY + row * CELL_SIZE;

                if (grid[row][col]) {
                    g.setColor(Color.ORANGE);
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                } else {
                    g.setColor(Color.WHITE);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // --- UPDATED FOR VERSION 8: Use CELL_SIZE if dragged, PREVIEW_CELL_SIZE if resting ---
        // Block 1
        if (!blockPlaced[1]) {
            if (selectedBlock == 1) {
                drawBlock(g, activeBlock1, dragX, dragY, CELL_SIZE);
            } else {
                drawBlock(g, activeBlock1, block1HomeX, blockHomeY, PREVIEW_CELL_SIZE);
            }
        }

        // Block 2
        if (!blockPlaced[2]) {
            if (selectedBlock == 2) {
                drawBlock(g, activeBlock2, dragX, dragY, CELL_SIZE);
            } else {
                drawBlock(g, activeBlock2, block2HomeX, blockHomeY, PREVIEW_CELL_SIZE);
            }
        }

        // Block 3
        if (!blockPlaced[3]) {
            if (selectedBlock == 3) {
                drawBlock(g, activeBlock3, dragX, dragY, CELL_SIZE);
            } else {
                drawBlock(g, activeBlock3, block3HomeX, blockHomeY, PREVIEW_CELL_SIZE);
            }
        }
    }

    // --- UPDATED FOR VERSION 8: Added size argument to control square rendering scale ---
    private void drawBlock(Graphics g, int[][] shape, int anchorX, int anchorY, int size) {
        for (int[] cell : shape) {
            int x = anchorX + cell[1] * size;
            int y = anchorY + cell[0] * size;
            g.setColor(Color.ORANGE);
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, size, size);
        }
    }

    // --- UPDATED FOR VERSION 8: Added size argument to match the active visual dimensions ---
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

        // --- UPDATED FOR VERSION 8: Check hits using PREVIEW_CELL_SIZE and anchor offsets to center on drag ---
        if (!blockPlaced[1] && isBlockClicked(activeBlock1, block1HomeX, blockHomeY, mx, my, PREVIEW_CELL_SIZE)) {
            selectedBlock = 1;
            // Center tracking: offset calculations map to full size immediately to prevent off-center snapping
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
            if (grid[checkRow][checkCol]) {
                canPlace = false;
                break;
            }
        }

        if (canPlace) {
            for (int[] cell : currentShape) {
                int fillRow = targetRow + cell[0];
                int fillCol = targetCol + cell[1];
                grid[fillRow][fillCol] = true;
            }

            blockPlaced[selectedBlock] = true;

            boolean[] rowsToClear = new boolean[GRID_ROWS];
            boolean[] colsToClear = new boolean[GRID_COLS];

            for (int r = 0; r < GRID_ROWS; r++) {
                boolean rowFull = true;
                for (int c = 0; c < GRID_COLS; c++) {
                    if (!grid[r][c]) {
                        rowFull = false;
                        break;
                    }
                }
                if (rowFull) rowsToClear[r] = true;
            }

            for (int c = 0; c < GRID_COLS; c++) {
                boolean colFull = true;
                for (int r = 0; r < GRID_ROWS; r++) {
                    if (!grid[r][c]) {
                        colFull = false;
                        break;
                    }
                }
                if (colFull) colsToClear[c] = true;
            }

            for (int r = 0; r < GRID_ROWS; r++) {
                for (int c = 0; c < GRID_COLS; c++) {
                    if (rowsToClear[r] || colsToClear[c]) {
                        grid[r][c] = false;
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