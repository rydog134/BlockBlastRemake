import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private static final int CELL_SIZE = 50;
    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 8;

    // Block shapes defined as [row, col] offsets
    private static final int[][] BLOCK_1 = {{0,0}, {0,1}, {0,2}};         // horizontal line of 3
    private static final int[][] BLOCK_2 = {{0,0}, {1,0}, {2,0}, {2,1}}; // L-shape
    private static final int[][] BLOCK_3 = {{0,0}, {0,1}, {1,0}, {1,1}}; // 2x2 square

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

        // Draw each cell of the grid
        g.setColor(Color.WHITE);
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int x = startX + col * CELL_SIZE;
                int y = startY + row * CELL_SIZE;
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        // Draw three blocks below the grid
        int blockY = startY + gridHeight + 20; // 20px gap below the grid
        drawBlock(g, BLOCK_1, 75,  blockY);    // left zone
        drawBlock(g, BLOCK_2, 250, blockY);    // middle zone
        drawBlock(g, BLOCK_3, 425, blockY);    // right zone
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
}