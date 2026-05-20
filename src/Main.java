import javax.swing.*;

public class Main {
  public static void main(String[] args) {
    JFrame frame = new JFrame("Block Blast");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(600, 800);
    frame.setResizable(false);

    GamePanel panel = new GamePanel();
    frame.add(panel);

    frame.setVisible(true);
  }
}
