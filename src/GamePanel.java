import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int CELL_SIZE = 50;
    private static final int PREVIEW_CELL_SIZE = 28;

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

    private static final int[][] SQUARE_3X3 = {
            {0,0}, {0,1}, {0,2},
            {1,0}, {1,1}, {1,2},
            {2,0}, {2,1}, {2,2}
    };
    private static final int[][] ZIGZAG = {{0,0}, {0,1}, {1,1}, {1,2}};

    private static final int[][][] BASE_SHAPE_POOL = {
            MONOMINO,
            DOMINO_H, DOMINO_V,
            BLOCK_1, TROMINO_V, BLOCK_2,
            BLOCK_3, TETRIS_I_V, TETRIS_T, TETRIS_L,
            PENTO_I_H, PENTO_U, PENTO_X,
            SQUARE_3X3, ZIGZAG
    };

    private static final int[][][] SHAPE_POOL = generateRotatedPool(BASE_SHAPE_POOL);

    private static final Color[] COLOR_PALETTE = {
            Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.PINK
    };

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

    private Color colorBlock1;
    private Color colorBlock2;
    private Color colorBlock3;

    private int score = 0;

    private int multiplier = 1;
    private int movesSinceLastClear = 0;
    private int rowsClearedInWindow = 0;

    private boolean gameOver = false;

    private String username = "Guest";
    private int highScore = 0;
    private boolean isNewHighScore = false;
    private static final String FILE_NAME = "scores.txt";

    // --- NEW FOR VERSION 21: EASTER EGG TRACKING CONDITIONS FOR LOW-SCORE MODIFIER ---
    private boolean isGabe = false;
    private boolean isNewLowScore = false;

    private final int buttonX = 225;
    private final int buttonY = 450;
    private final int buttonWidth = 150;
    private final int buttonHeight = 50;

    private int[][] animSizes = new int[GRID_ROWS][GRID_COLS];
    private int[][] trailOpacities = new int[GRID_ROWS][GRID_COLS];
    private int[][] trailDelays = new int[GRID_ROWS][GRID_COLS];
    private Color[][] animColors = new Color[GRID_ROWS][GRID_COLS];
    private Timer animationTimer;

    private ArrayList<Particle> particles = new ArrayList<>();

    public GamePanel() {
        String input = JOptionPane.showInputDialog(null, "Enter your username:", "Sign In", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            username = input.trim();
        }

        // --- NEW FOR VERSION 21: EVALUATES USERNAME STRING FOR EASTER EGG TRIGGER MATCHES ---
        if (username.toLowerCase().contains("gabe")) {
            isGabe = true;
        }

        loadOrCreateUser(username);

        addMouseListener(this);
        addMouseMotionListener(this);
        randomizeBlocks();
        setupAnimationEngine();
    }

    private static int[][][] generateRotatedPool(int[][][] basePool) {
        ArrayList<int[][]> expanded = new ArrayList<>();
        for (int[][] shape : basePool) {
            for (int turn = 0; turn < 4; turn++) {
                expanded.add(rotateShape(shape, turn));
            }
        }
        return expanded.toArray(new int[expanded.size()][][]);
    }

    private static int[][] rotateShape(int[][] shape, int turns) {
        if (turns == 0) return shape;

        int[][] current = shape;
        for (int t = 0; t < turns; t++) {
            int[][] next = new int[current.length][2];
            for (int i = 0; i < current.length; i++) {
                next[i][0] = current[i][1];
                next[i][1] = -current[i][0];
            }
            current = next;
        }

        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;
        for (int[] cell : current) {
            if (cell[0] < minRow) minRow = cell[0];
            if (cell[1] < minCol) minCol = cell[1];
        }
        for (int[] cell : current) {
            cell[0] -= minRow;
            cell[1] -= minCol;
        }

        return current;
    }

    private void loadOrCreateUser(String targetUser) {
        try {
            File scoreFile = new File(FILE_NAME);
            if (!scoreFile.exists()) {
                scoreFile.createNewFile();
                this.highScore = 0;
                return;
            }

            Scanner fileScanner = new Scanner(scoreFile);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.contains(",")) {
                    String[] parts = line.split(",");
                    if (parts[0].equalsIgnoreCase(targetUser)) {
                        this.highScore = Integer.parseInt(parts[1]);
                        fileScanner.close();
                        return;
                    }
                }
            }
            fileScanner.close();
            this.highScore = 0;
        } catch (Exception e) {
            System.out.println("Error reading score file: " + e.getMessage());
            this.highScore = 0;
        }
    }

    private void saveHighScore() {
        ArrayList<String> fileLines = new ArrayList<>();
        boolean userFound = false;

        try {
            File scoreFile = new File(FILE_NAME);
            if (scoreFile.exists()) {
                Scanner fileScanner = new Scanner(scoreFile);
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    if (line.contains(",")) {
                        String[] parts = line.split(",");
                        if (parts[0].equalsIgnoreCase(username)) {
                            fileLines.add(username + "," + highScore);
                            userFound = true;
                        } else {
                            fileLines.add(line);
                        }
                    }
                }
                fileScanner.close();
            }

            if (!userFound) {
                fileLines.add(username + "," + highScore);
            }

            PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, false));
            for (String savedLine : fileLines) {
                writer.println(savedLine);
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("Error saving score file: " + e.getMessage());
        }
    }

    private Color getDarkerShade(Color c) {
        if (c == null) return Color.BLACK;
        int r = (int)(c.getRed() * 0.70);
        int g = (int)(c.getGreen() * 0.70);
        int b = (int)(c.getBlue() * 0.70);
        return new Color(r, g, b);
    }

    private Color getLighterShade(Color c) {
        if (c == null) return Color.WHITE;
        int r = (int)(c.getRed() * 0.85);
        int g = (int)(c.getGreen() * 0.85);
        int b = (int)(c.getBlue() * 0.85);
        return new Color(r, g, b);
    }

    private void setupAnimationEngine() {
        animationTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean dynamicActive = false;

                for (int r = 0; r < GRID_ROWS; r++) {
                    for (int c = 0; c < GRID_COLS; c++) {
                        if (animSizes[r][c] > 0) {
                            animSizes[r][c] -= 4;
                            if (animSizes[r][c] < 0) animSizes[r][c] = 0;
                            dynamicActive = true;
                        }

                        if (trailDelays[r][c] > 0) {
                            trailDelays[r][c] -= 30;
                            dynamicActive = true;
                        } else {
                            if (trailOpacities[r][c] > 0) {
                                trailOpacities[r][c] -= 8;
                                if (trailOpacities[r][c] < 0) trailOpacities[r][c] = 0;
                                dynamicActive = true;
                            }
                        }
                    }
                }

                for (int i = particles.size() - 1; i >= 0; i--) {
                    Particle p = particles.get(i);
                    p.x += p.dx;
                    p.y += p.dy;
                    p.opacity -= 8;
                    if (p.opacity <= 0) {
                        particles.remove(i);
                    } else {
                        dynamicActive = true;
                    }
                }

                if (dynamicActive) {
                    repaint();
                } else {
                    animationTimer.stop();
                }
            }
        });
    }

    private void randomizeBlocks() {
        int index1 = (int) (Math.random() * SHAPE_POOL.length);
        int index2 = (int) (Math.random() * SHAPE_POOL.length);
        int index3 = (int) (Math.random() * SHAPE_POOL.length);

        activeBlock1 = SHAPE_POOL[index1];
        activeBlock2 = SHAPE_POOL[index2];
        activeBlock3 = SHAPE_POOL[index3];

        colorBlock1 = COLOR_PALETTE[(int) (Math.random() * COLOR_PALETTE.length)];
        colorBlock2 = COLOR_PALETTE[(int) (Math.random() * COLOR_PALETTE.length)];
        colorBlock3 = COLOR_PALETTE[(int) (Math.random() * COLOR_PALETTE.length)];
    }

    private boolean canShapeFitAt(int[][] shape, int startRow, int startCol) {
        for (int[] cell : shape) {
            int checkRow = startRow + cell[0];
            int checkCol = startCol + cell[1];

            if (checkRow < 0 || checkRow >= GRID_ROWS || checkCol < 0 || checkCol >= GRID_COLS) {
                return false;
            }
            if (grid[checkRow][checkCol] != null) {
                return false;
            }
        }
        return true;
    }

    private void checkGameOver() {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (!blockPlaced[1] && canShapeFitAt(activeBlock1, row, col)) return;
                if (!blockPlaced[2] && canShapeFitAt(activeBlock2, row, col)) return;
                if (!blockPlaced[3] && canShapeFitAt(activeBlock3, row, col)) return;
            }
        }
        gameOver = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(15, 25, 65));
        g.fillRect(0, 0, getWidth(), getHeight());

        int gridWidth = GRID_COLS * CELL_SIZE;
        int gridHeight = GRID_ROWS * CELL_SIZE;
        int startX = (getWidth() - gridWidth) / 2;
        int startY = (getHeight() - gridHeight) / 2;

        blockHomeY = startY + gridHeight + 20;

        g.setColor(new Color(195, 195, 195));
        g.fillRect(startX, startY, gridWidth, gridHeight);

        g.setColor(new Color(145, 145, 145));
        for (int i = 0; i < 5; i++) {
            g.drawRect(startX + i, startY + i, gridWidth - (2 * i), gridHeight - (2 * i));
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("Score: " + score + "  (x" + multiplier + ")", startX, startY - 30);

        int topLabelsRightBoundaryX = startX + gridWidth;
        g.setFont(new Font("Arial", Font.BOLD, 18));
        String nameString = "Player: " + username;

        // --- NEW FOR VERSION 21: CONVERTS RECORD HOOK TAG FROM BEST TO WORST FOR EASTER EGG PROFILES ---
        String bestString = isGabe ? "Worst: " + highScore : "Best: " + highScore;

        int nameWidth = g.getFontMetrics().stringWidth(nameString);
        int bestWidth = g.getFontMetrics().stringWidth(bestString);

        g.drawString(nameString, topLabelsRightBoundaryX - nameWidth, startY - 42);
        g.drawString(bestString, topLabelsRightBoundaryX - bestWidth, startY - 20);

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int x = startX + col * CELL_SIZE;
                int y = startY + row * CELL_SIZE;

                g.setColor(new Color(145, 145, 145));
                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                g.setColor(new Color(195, 195, 195));
                g.fillRect(x + 3, y + 3, CELL_SIZE - 6, CELL_SIZE - 6);

                g.setColor(new Color(175, 175, 175));
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);

                if (grid[row][col] != null) {
                    Color baseColor = grid[row][col];

                    g.setColor(getLighterShade(baseColor));
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                    g.setColor(getDarkerShade(baseColor));
                    g.fillRect(x + 5, y + 5, CELL_SIZE - 5, CELL_SIZE - 5);

                    g.setColor(baseColor);
                    g.fillRect(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10);

                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
                else if (animSizes[row][col] > 0) {
                    int size = Math.min(animSizes[row][col], CELL_SIZE);
                    int offset = (CELL_SIZE - size) / 2;

                    Color baseColor = animColors[row][col];
                    int shadowOffset = (int)(size * 0.10);

                    g.setColor(getLighterShade(baseColor));
                    g.fillRect(x + offset, y + offset, size, size);

                    g.setColor(getDarkerShade(baseColor));
                    g.fillRect(x + offset + shadowOffset, y + offset + shadowOffset, size - shadowOffset, size - shadowOffset);

                    g.setColor(baseColor);
                    g.fillRect(x + offset + shadowOffset, y + offset + shadowOffset, size - (2 * shadowOffset), size - (2 * shadowOffset));

                    g.setColor(Color.BLACK);
                    g.drawRect(x + offset, y + offset, size, size);
                }

                if (trailDelays[row][col] <= 0 && trailOpacities[row][col] > 0) {
                    g.setColor(new Color(173, 216, 230, trailOpacities[row][col]));
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        for (Particle p : particles) {
            g.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), p.opacity));
            g.fillRect((int)p.x, (int)p.y, p.size, p.size);
        }

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

        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 42));
            g.drawString("Game Over", 190, 320);

            if (isNewHighScore) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("High score!", 218, 250);
            }
            // --- NEW FOR VERSION 21: LOW SCORE BANNER CONTEXT REPLACEMENT FOR EASTER EGG ROUNDS ---
            else if (isNewLowScore) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("Low score!", 223, 250);
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Final Score: " + score, 205, 380);

            g.setColor(Color.GREEN);
            g.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);

            g.setColor(Color.BLACK);
            g.drawRect(buttonX, buttonY, buttonWidth, buttonHeight);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString("Play Again", buttonX + 22, buttonY + 32);
        }
    }

    private void drawBlock(Graphics g, int[][] shape, int anchorX, int anchorY, int size, Color blockColor) {
        int shadowOffset = (int)(size * 0.10);
        Color shadowColor = getDarkerShade(blockColor);
        Color highlightColor = getLighterShade(blockColor);

        for (int[] cell : shape) {
            int x = anchorX + cell[1] * size;
            int y = anchorY + cell[0] * size;

            g.setColor(highlightColor);
            g.fillRect(x, y, size, size);

            g.setColor(shadowColor);
            g.fillRect(x + shadowOffset, y + shadowOffset, size - shadowOffset, size - shadowOffset);

            g.setColor(blockColor);
            g.fillRect(x + shadowOffset, y + shadowOffset, size - (2 * shadowOffset), size - (2 * shadowOffset));

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

        if (gameOver) {
            if (mx >= buttonX && mx <= buttonX + buttonWidth && my >= buttonY && my <= buttonY + buttonHeight) {
                score = 0;
                multiplier = 1;
                movesSinceLastClear = 0;
                rowsClearedInWindow = 0;

                blockPlaced[1] = false;
                blockPlaced[2] = false;
                blockPlaced[3] = false;
                isNewHighScore = false;
                // --- NEW FOR VERSION 21: RESET EASTER EGG NOTIFICATION STATE ---
                isNewLowScore = false;

                for (int r = 0; r < GRID_ROWS; r++) {
                    for (int c = 0; c < GRID_COLS; c++) {
                        grid[r][c] = null;
                        animSizes[r][c] = 0;
                        trailOpacities[r][c] = 0;
                        trailDelays[r][c] = 0;
                    }
                }
                particles.clear();

                randomizeBlocks();
                gameOver = false;
                repaint();
            }
            return;
        }

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
            if (grid[checkRow][checkCol] != null) {
                canPlace = false;
                break;
            }
        }

        if (canPlace) {
            for (int[] cell : currentShape) {
                int fillRow = targetRow + cell[0];
                int fillCol = targetCol + cell[1];
                grid[fillRow][fillCol] = currentBlockColor;
            }

            int pointsGainedThisTurn = currentShape.length;

            blockPlaced[selectedBlock] = true;

            boolean[] rowsToClear = new boolean[GRID_ROWS];
            boolean[] colsToClear = new boolean[GRID_COLS];

            for (int r = 0; r < GRID_ROWS; r++) {
                boolean rowFull = true;
                for (int c = 0; c < GRID_COLS; c++) {
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
                    if (grid[r][c] == null) {
                        colFull = false;
                        break;
                    }
                }
                if (colFull) colsToClear[c] = true;
            }

            int clearedLinesThisTurn = 0;
            for (int r = 0; r < GRID_ROWS; r++) {
                if (rowsToClear[r]) {
                    pointsGainedThisTurn += 10;
                    clearedLinesThisTurn++;
                }
            }
            for (int c = 0; c < GRID_COLS; c++) {
                if (colsToClear[c]) {
                    pointsGainedThisTurn += 10;
                    clearedLinesThisTurn++;
                }
            }

            if (clearedLinesThisTurn > 0) {
                rowsClearedInWindow += clearedLinesThisTurn;
                movesSinceLastClear = 0;

                if (rowsClearedInWindow >= 2) {
                    multiplier += clearedLinesThisTurn;
                }
            } else {
                movesSinceLastClear++;
                if (movesSinceLastClear >= 3) {
                    multiplier = 1;
                    rowsClearedInWindow = 0;
                }
            }

            // --- NEW FOR VERSION 21: SUBTRACTS INSTEAD OF ADDS SCORE QUANTITIES IF IN EASTER EGG MODE ---
            if (isGabe) {
                score -= pointsGainedThisTurn * multiplier;
            } else {
                score += pointsGainedThisTurn * multiplier;
            }

            for (int r = 0; r < GRID_ROWS; r++) {
                for (int c = 0; c < GRID_COLS; c++) {
                    if (rowsToClear[r] || colsToClear[c]) {
                        animColors[r][c] = grid[r][c];
                        trailOpacities[r][c] = 255;

                        double distFromCenter = rowsToClear[r] ? Math.abs(c - 3.5) : Math.abs(r - 3.5);
                        trailDelays[r][c] = (int)(distFromCenter * 45);

                        int indexOffset = rowsToClear[r] ? c : r;
                        animSizes[r][c] = CELL_SIZE + (indexOffset * 10);

                        int cellCenterX = startX + (c * CELL_SIZE) + (CELL_SIZE / 2);
                        int cellCenterY = startY + (r * CELL_SIZE) + (CELL_SIZE / 2);

                        for (int k = 0; k < 3; k++) {
                            Particle p = new Particle();
                            p.x = cellCenterX + (Math.random() * 20 - 10);
                            p.y = cellCenterY + (Math.random() * 20 - 10);
                            p.size = (int)(Math.random() * 8) + 2;
                            p.color = Color.CYAN;

                            if (rowsToClear[r]) {
                                p.dx = (c >= 4) ? (Math.random() * 4 + 2) : -(Math.random() * 4 + 2);
                                p.dy = Math.random() * 4 - 2;
                            } else {
                                p.dx = Math.random() * 4 - 2;
                                p.dy = (r >= 4) ? (Math.random() * 4 + 2) : -(Math.random() * 4 + 2);
                            }
                            particles.add(p);
                        }

                        grid[r][c] = null;
                    }
                }
            }

            if (clearedLinesThisTurn > 0) {
                animationTimer.start();
            }

            if (blockPlaced[1] && blockPlaced[2] && blockPlaced[3]) {
                blockPlaced[1] = false;
                blockPlaced[2] = false;
                blockPlaced[3] = false;

                randomizeBlocks();
            }

            checkGameOver();

            // --- NEW FOR VERSION 21: FILE LOGGING REVERSAL AND RECORD LOWERING TARGET CHECKS ---
            if (gameOver) {
                if (isGabe) {
                    if (score < highScore) {
                        highScore = score;
                        isNewLowScore = true;
                        saveHighScore();
                    }
                } else {
                    if (score > highScore) {
                        highScore = score;
                        isNewHighScore = true;
                        saveHighScore();
                    }
                }
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

class Particle {
    double x, y;
    double dx, dy;
    int size;
    int opacity = 255;
    Color color;
}