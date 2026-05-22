import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int CELL_SIZE = 50;
    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 8;

    // Static master blueprints
    private static final int[][] BLOCK_1 = {{0,0}, {0,1}, {0,2}};         // horizontal line of 3
    private static final int[][] BLOCK_2 = {{0,0}, {1,0}, {2,0}, {2,1}}; // L-shape
    private static final int[][] BLOCK_3 = {{0,0}, {0,1}, {1,0}, {1,1}}; // 2x2 square

    // --- NEW FOR VERSION 5: SHAPE BLUEPRINT POOL ---
    private static final int[][][] SHAPE_POOL = { BLOCK_1, BLOCK_2, BLOCK_3 };

    private boolean[][] grid = new boolean[GRID_ROWS][GRID_COLS]; // false = empty, true = filled

    // Default home anchors for the three preview blocks
    private final int block1HomeX = 75;
    private final int block2HomeX = 250;
    private final int block3HomeX = 425;
    private int blockHomeY; // Calculated dynamically based on grid position

    // Dragging state
    private int selectedBlock = -1; // -1 = none, 1 = activeBlock1, 2 = activeBlock2, 3 = activeBlock3
    private int dragX = 0;          // Current X of mouse/drag anchor
    private int dragY = 0;          // Current Y of mouse/drag anchor

    private int mouseOffsetX = 0;
    private int mouseOffsetY = 0;

    // Inventory State
    private boolean[] blockPlaced = new boolean[4];

    // --- NEW FOR VERSION 5: ACTIVE INTERACTION SLOTS ---
    private int[][] activeBlock1;
    private int[][] activeBlock2;
    private int[][] activeBlock3;

    public GamePanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
        // --- NEW LOGIC: Generate the initial layout on startup ---
        randomizeBlocks();
    }

    // --- NEW FOR VERSION 5: RANDOMIZATION HELPER METHOD ---
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

        // Draw blue background
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Calculate top-left corner to centre the grid
        int gridWidth = GRID_COLS * CELL_SIZE;   // 400px
        int gridHeight = GRID_ROWS * CELL_SIZE;  // 400px
        int startX = (getWidth() - gridWidth) / 2;
        int startY = (getHeight() - gridHeight) / 2;

        blockHomeY = startY + gridHeight + 20;

        // Draw each cell of the grid
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

        // --- NEW LOGIC: Render dynamic slots instead of hardcoded master shapes ---
        // Block 1
        if (!blockPlaced[1]) {
            if (selectedBlock == 1) {
                drawBlock(g, activeBlock1, dragX, dragY);
            } else {
                drawBlock(g, activeBlock1, block1HomeX, blockHomeY);
            }
        }

        // Block 2
        if (!blockPlaced[2]) {
            if (selectedBlock == 2) {
                drawBlock(g, activeBlock2, dragX, dragY);
            } else {
                drawBlock(g, activeBlock2, block2HomeX, blockHomeY);
            }
        }

        // Block 3
        if (!blockPlaced[3]) {
            if (selectedBlock == 3) {
                drawBlock(g, activeBlock3, dragX, dragY);
            } else {
                drawBlock(g, activeBlock3, block3HomeX, blockHomeY);
            }
        }
    }

    private void drawBlock(Graphics g, int[][] shape, int anchorX, int anchorY) {
        for (int[] cell : shape) {
            int x = anchorX + cell[1] * CELL_SIZE;
            int y = anchorY + cell[0] * CELL_SIZE;
            g.setColor(Color.ORANGE);
            g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        // --- NEW LOGIC: Use generous 3x3 bounding boundaries to safely allow for random shape variations ---
        // Check Slot 1
        if (!blockPlaced[1] && mx >= block1HomeX && mx <= block1HomeX + (3 * CELL_SIZE) &&
                my >= blockHomeY && my <= blockHomeY + (3 * CELL_SIZE)) {
            selectedBlock = 1;
            mouseOffsetX = mx - block1HomeX;
            mouseOffsetY = my - blockHomeY;
        }
        // Check Slot 2
        else if (!blockPlaced[2] && mx >= block2HomeX && mx <= block2HomeX + (3 * CELL_SIZE) &&
                my >= blockHomeY && my <= blockHomeY + (3 * CELL_SIZE)) {
            selectedBlock = 2;
            mouseOffsetX = mx - block2HomeX;
            mouseOffsetY = my - blockHomeY;
        }
        // Check Slot 3
        else if (!blockPlaced[3] && mx >= block3HomeX && mx <= block3HomeX + (3 * CELL_SIZE) &&
                my >= blockHomeY && my <= blockHomeY + (3 * CELL_SIZE)) {
            selectedBlock = 3;
            mouseOffsetX = mx - block3HomeX;
            mouseOffsetY = my - blockHomeY;
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

        // --- NEW LOGIC: Target current dynamic active blocks ---
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

            // Line clearing matrix evaluations
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

            // If all three blocks are used, regenerate them
            if (blockPlaced[1] && blockPlaced[2] && blockPlaced[3]) {
                blockPlaced[1] = false;
                blockPlaced[2] = false;
                blockPlaced[3] = false;

                // --- NEW LOGIC: Reroll random shapes from pool into interaction slots ---
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