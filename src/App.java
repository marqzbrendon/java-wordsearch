import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        int tileSize = 32;
        int squareRowsCols = 15; // square
        int screenHeight = tileSize * squareRowsCols + 24;
        int screenWidth = tileSize * squareRowsCols + tileSize * 5;

        JFrame board = new JFrame("Word-Search");
        board.setSize(screenWidth, screenHeight);
        board.setLocationRelativeTo(null);
        board.setResizable(true);
        board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        WordSearch wordSearch = new WordSearch();

        board.add(wordSearch);
        // board.pack();
        board.requestFocus();
        board.setVisible(true);
    }
}
