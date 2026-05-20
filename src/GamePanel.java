import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private static final int CELL_SIZE = 50;
    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 8;

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
    }
}