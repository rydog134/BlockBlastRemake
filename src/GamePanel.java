import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int CELL_SIZE = 50;
    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 8;

    // Block shapes defined as [row, col] offsets
    private static final int[][] BLOCK_1 = {{0,0}, {0,1}, {0,2}};         // horizontal line of 3
    private static final int[][] BLOCK_2 = {{0,0}, {1,0}, {2,0}, {2,1}}; // L-shape
    private static final int[][] BLOCK_3 = {{0,0}, {0,1}, {1,0}, {1,1}}; // 2x2 square

    // --- NEW FOR VERSION 2: STATE TRACKING ---
    private boolean[][] grid = new boolean[GRID_ROWS][GRID_COLS]; // false = empty, true = filled

    // Default home anchors for the three preview blocks
    private final int block1HomeX = 75;
    private final int block2HomeX = 250;
    private final int block3HomeX = 425;
    private int blockHomeY; // Calculated dynamically based on grid position

    // Dragging state
    private int selectedBlock = -1; // -1 = none, 1 = BLOCK_1, 2 = BLOCK_2, 3 = BLOCK_3
    private int dragX = 0;          // Current X of mouse/drag anchor
    private int dragY = 0;          // Current Y of mouse/drag anchor

    // Offsets to make sure the block doesn't awkwardly jump its top-left corner to your mouse cursor
    private int mouseOffsetX = 0;
    private int mouseOffsetY = 0;

    public GamePanel() {
        // Register mouse listeners so this panel listens to user inputs
        addMouseListener(this);
        addMouseMotionListener(this);
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

        // Update home Y coordinate dynamically
        blockHomeY = startY + gridHeight + 20;

        // Draw each cell of the grid
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int x = startX + col * CELL_SIZE;
                int y = startY + row * CELL_SIZE;

                // --- NEW LOGIC: Check grid state ---
                if (grid[row][col]) {
                    // Block is permanently snapped here
                    g.setColor(Color.ORANGE);
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                } else {
                    // Empty grid cell
                    g.setColor(Color.WHITE);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // --- NEW LOGIC: Conditional rendering of preview blocks ---
        // Block 1
        if (selectedBlock == 1) {
            drawBlock(g, BLOCK_1, dragX, dragY);
        } else {
            drawBlock(g, BLOCK_1, block1HomeX, blockHomeY);
        }

        // Block 2
        if (selectedBlock == 2) {
            drawBlock(g, BLOCK_2, dragX, dragY);
        } else {
            drawBlock(g, BLOCK_2, block2HomeX, blockHomeY);
        }

        // Block 3
        if (selectedBlock == 3) {
            drawBlock(g, BLOCK_3, dragX, dragY);
        } else {
            drawBlock(g, BLOCK_3, block3HomeX, blockHomeY);
        }
    }

    // Draws a block at the given anchor position (anchorX, anchorY)
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

    // --- NEW FOR VERSION 2: MOUSE INTERACTION METHUDS ---

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        // Check if mouse is inside Block 1 bounding area roughly (3 columns wide, 1 row high)
        if (mx >= block1HomeX && mx <= block1HomeX + (3 * CELL_SIZE) &&
                my >= blockHomeY && my <= blockHomeY + CELL_SIZE) {
            selectedBlock = 1;
            mouseOffsetX = mx - block1HomeX;
            mouseOffsetY = my - blockHomeY;
        }
        // Check if mouse is inside Block 2 bounding area (2 columns wide, 3 rows high)
        else if (mx >= block2HomeX && mx <= block2HomeX + (2 * CELL_SIZE) &&
                my >= blockHomeY && my <= blockHomeY + (3 * CELL_SIZE)) {
            selectedBlock = 2;
            mouseOffsetX = mx - block2HomeX;
            mouseOffsetY = my - blockHomeY;
        }
        // Check if mouse is inside Block 3 bounding area (2 columns wide, 2 rows high)
        else if (mx >= block3HomeX && mx <= block3HomeX + (2 * CELL_SIZE) &&
                my >= blockHomeY && my <= blockHomeY + (2 * CELL_SIZE)) {
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
            // Update positions keeping the relative mouse grab offset point stable
            dragX = e.getX() - mouseOffsetX;
            dragY = e.getY() - mouseOffsetY;
            repaint(); // Forces paintComponent to draw the block at its new dynamic location
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (selectedBlock == -1) return;

        // Recalculate grid start layout constants locally for board mapping math
        int gridWidth = GRID_COLS * CELL_SIZE;
        int gridHeight = GRID_ROWS * CELL_SIZE;
        int startX = (getWidth() - gridWidth) / 2;
        int startY = (getHeight() - gridHeight) / 2;

        // Determine which shape matrix we are dropping
        int[][] currentShape = (selectedBlock == 1) ? BLOCK_1 : (selectedBlock == 2) ? BLOCK_2 : BLOCK_3;

        // Map the current dragging visual anchor to closest rows and columns of our underlying grid
        int targetCol = Math.round((float) (dragX - startX) / CELL_SIZE);
        int targetRow = Math.round((float) (dragY - startY) / CELL_SIZE);

        boolean canPlace = true;

        // Validation pass: check if every individual block segment fits into empty board territory
        for (int[] cell : currentShape) {
            int checkRow = targetRow + cell[0];
            int checkCol = targetCol + cell[1];

            // Standard boundary checks out of standard AP CSA index requirements
            if (checkRow < 0 || checkRow >= GRID_ROWS || checkCol < 0 || checkCol >= GRID_COLS) {
                canPlace = false;
                break;
            }
            // Check if grid slot is already taken
            if (grid[checkRow][checkCol]) {
                canPlace = false;
                break;
            }
        }

        // Mutate real game grid state if completely validated
        if (canPlace) {
            for (int[] cell : currentShape) {
                int fillRow = targetRow + cell[0];
                int fillCol = targetCol + cell[1];
                grid[fillRow][fillCol] = true;
            }
        }

        // Clean slate reset for your next round
        selectedBlock = -1;
        repaint();
    }

    // Unused mandatory listener methods required by Interfaces
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}