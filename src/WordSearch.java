import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;


public final class WordSearch extends JPanel {
    Random random = new Random();
    enum BlockType {
        WORD,
        FILLER
    }
    public enum Direction {
        HORIZONTAL,
        VERTICAL,
        UPHILL,
        DOWNHILL,
        INVERSED_HORIZONTAL,
        INVERSED_VERTICAL,
        INVERSED_UPHILL,
        INVERSED_DOWNHILL;
    
        public static Direction inverseDirection(Direction direction) {
            switch (direction) {
                case HORIZONTAL: return INVERSED_HORIZONTAL;
                case VERTICAL: return INVERSED_VERTICAL;
                case UPHILL: return INVERSED_UPHILL;
                case DOWNHILL: return INVERSED_DOWNHILL;
                case INVERSED_HORIZONTAL: return HORIZONTAL;
                case INVERSED_VERTICAL: return VERTICAL;
                case INVERSED_UPHILL: return UPHILL;
                case INVERSED_DOWNHILL: return DOWNHILL;
                default: throw new IllegalArgumentException("Unknown direction: " + direction);
            }
        }
    }
    class Point {
        int x;
        int y;
        
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    ArrayList<Integer> heights = new ArrayList<>();
    
    class Block {
        Point point;
        static final int WIDTH = 32;
        static final int HEIGHT = 32;
        char value;
        String word = null;
        boolean found;
        // Direction direction;
        BlockType type;
        boolean firstLetter = false;
        Point lastLetterLocation;
        int randomlyAssignedWordRow;
        
        Block(Point point, char value, BlockType type) {
            this.point = point;
            this.value = value;
            this.type = type;
        }
    }
    class FittingObject {
        Point firstPoint;
        boolean fitFound;

        FittingObject(Point firstPoint, boolean fitFound) {
            this.firstPoint = firstPoint;
            this.fitFound = fitFound;
        }
    }
    
    Block[][] blocks = new Block[15][15];
    final int rowsColumns = 15;

    WordSearch() {
        try {
            for (int i = 0; i < 15; i++) {
                heights.add(i);
            }
            loadEmptyMap();
            loadWords();
            fillEmptySpaces();
            paintFoundWords();
        } catch (UnableToFitWordException e) {
            System.err.print(e);
            System.exit(1);
        }
    }
    
    public void loadEmptyMap() {
        for (int x = 0; x < rowsColumns; x++) {
            for (int y = 0; y < rowsColumns; y++) {
                Block fillerBlock = new Block(new Point(x, y), '_', BlockType.FILLER);
                blocks[x][y] = fillerBlock;
            }
        }
    }

    String[] tempWords = {
        "spaghetti", "pizza", "cheese", "apple", "banana",
        "milk", "strawberry", "cookies", "juice", "lasagna",
        "burger", "lemonade", "soda", "cereal", "pancakes"
    };
    public void loadWords() throws UnableToFitWordException {
        int idx = 0;
        for (String tempWord : tempWords) {
            int attempts = 0;
            Point fittedPoint = new Point(0, 0);
            Direction fittedDirection = null;
            boolean fitFound = false;
            while (!fitFound) {
                boolean makeItCross;
                makeItCross = random.nextDouble() <= 0.60;
                System.out.print("makeItCross " + makeItCross + "\n");
                int randomX = random.nextInt(15);
                int randomY = random.nextInt(15);
                Direction randomDirection = getRandomDirection();
                Point attemptPoint = new Point(randomX, randomY);
                if (!makeItCross || idx == 0) {
                    fitFound = fitFoundRandomly(attemptPoint, tempWord, randomDirection);
                } else {
                    System.out.print("inside makeItCross else statement\n");
                    FittingObject fittingObject = fitFoundCrossing(tempWord, randomDirection);
                    fitFound = fittingObject.fitFound;
                    if (fitFound) {
                        attemptPoint = fittingObject.firstPoint; 
                    }
                }
                attempts++;
                if (attempts > 15000) {
                    throw new UnableToFitWordException("Could not fit the words!");
                }
                if (fitFound) {
                    fittedPoint = attemptPoint;
                    fittedDirection = randomDirection;
                }
            }
            Point firstLetterPoint = new Point(fittedPoint.x, fittedPoint.y);
            for (int j = 0; j < tempWord.length(); j++) {
                if (j == 0) {  // First letter
                    blocks[fittedPoint.x][fittedPoint.y].firstLetter = true;
                    blocks[fittedPoint.x][fittedPoint.y].word = tempWord.toUpperCase();
                    int randomHeightIdx = random.nextInt(heights.size());
                    blocks[fittedPoint.x][fittedPoint.y].randomlyAssignedWordRow = heights.get(randomHeightIdx);
                    heights.remove(randomHeightIdx);
                }
                if (j == tempWord.length() - 1) {  // Last letter
                    blocks[firstLetterPoint.x][firstLetterPoint.y].lastLetterLocation = new Point(fittedPoint.x, fittedPoint.y);
                }
                char c = Character.toUpperCase(tempWord.charAt(j));
                // blocks[fittedPoint.x][fittedPoint.y].direction = fittedDirection;
                blocks[fittedPoint.x][fittedPoint.y].value = c;
                blocks[fittedPoint.x][fittedPoint.y].type = BlockType.WORD;
                fittedPoint = mapNextDirectionPoint(fittedDirection, fittedPoint);
            }
            idx++;
        }
    }
    
    private boolean fitFoundRandomly(Point point, String word, Direction direction) {
        Point tempPoint = new Point(point.x, point.y);
        {
            for (int j = 0; j < word.length(); j++) {
                if (tempPoint == null || tempPoint.x < 0 || tempPoint.y < 0 ||
                tempPoint.x >= rowsColumns || tempPoint.y >= rowsColumns) {
                    return false; // Point is out of bounds
                }
                Block block = blocks[tempPoint.x][tempPoint.y];
                char c = word.charAt(j);
                if (!(block.value == '_' || block.value == c)) {
                    return false;
                }
                tempPoint = mapNextDirectionPoint(direction, tempPoint);
            }
        }
        return true;
    }

    private FittingObject fitFoundCrossing(String word, Direction direction) {
        ArrayList<Block> tempBlocks = new ArrayList<>();
        Block chosenBlock = null;
        int randomIdx = random.nextInt(word.length());
        char c = Character.toUpperCase(word.charAt(randomIdx));
        for (int i = 0; i < rowsColumns; i++) {
            for (int j = 0; j < rowsColumns; j++) {
                System.out.print("comparing " + c + " with " + blocks[i][j].value + "\n");
                if (c == blocks[i][j].value) {
                    tempBlocks.add(blocks[i][j]);
                }
            }
        }
        if (tempBlocks.isEmpty()) {
            System.out.print("isEmpty\n");
            return new FittingObject(new Point(0,0), false);
        } else if (tempBlocks.size() > 1) {
            System.out.print("several values\n");
            int randomBlockIdx = random.nextInt(tempBlocks.size());
            chosenBlock = tempBlocks.get(randomBlockIdx);    
        } else {
            System.out.print("only one\n");
            chosenBlock = tempBlocks.get(0);
        }
        System.out.print("Fitting crossed with letter " + c + " for word " + word + "\n");

        Direction opposedDirection = Direction.inverseDirection(direction);
        boolean reversed = false;
        if (direction.toString().contains("REVERSED")) {
            reversed = true;
            StringBuilder reversedWord = new StringBuilder();
            reversedWord.append(word);
            reversedWord.reverse();
            word = reversedWord.toString();
        }

        String lhs = word.substring(0, randomIdx);
        String rhs = word.substring(randomIdx);

        Point lhsPoint = mapNextDirectionPoint(opposedDirection, chosenBlock.point);
        Point rhsPoint = mapNextDirectionPoint(direction, chosenBlock.point);
        
        Point initialLetterPoint = null;
        for (int i = lhs.length() - 1; i >= 0; i--){
            if (!reversed && i == lhs.length() - 1) {
                initialLetterPoint = lhsPoint;
            }
            if (lhsPoint == null || lhsPoint.x < 0 || lhsPoint.y < 0 ||
            lhsPoint.x >= rowsColumns || lhsPoint.y >= rowsColumns) {
                return new FittingObject(new Point(0,0), false);
            }
            Block block = blocks[lhsPoint.x][lhsPoint.y];
            char lhsChar = lhs.charAt(i);
            if (!(block.value == '_' || block.value == lhsChar)) {
                return new FittingObject(new Point(0,0), false);
            }
            lhsPoint = mapNextDirectionPoint(opposedDirection, lhsPoint);
        }
        
        for (int i = 0; i < rhs.length(); i++){
            if (reversed && i == rhs.length() - 1) {
                initialLetterPoint = rhsPoint;
            }
            if (rhsPoint == null || rhsPoint.x < 0 || rhsPoint.y < 0 ||
            rhsPoint.x >= rowsColumns || rhsPoint.y >= rowsColumns) {
                return new FittingObject(new Point(0,0), false);
            }
            Block block = blocks[rhsPoint.x][rhsPoint.y];
            char lhsChar = lhs.charAt(i);
            if (!(block.value == '_' || block.value == lhsChar)) {
                return new FittingObject(new Point(0,0), false);
            }
            rhsPoint = mapNextDirectionPoint(direction, rhsPoint);
        }

        // TODO implement rhs loop

        return new FittingObject(initialLetterPoint, true);
    }

    private Point mapNextDirectionPoint(Direction direction, Point point) {
        int tempX = point.x;
        int tempY = point.y;
        switch (direction) {
            case HORIZONTAL -> tempX++;
            case VERTICAL -> tempY++;
            case UPHILL -> { tempX++; tempY--; }
            case DOWNHILL -> { tempX++; tempY++; }
            case INVERSED_HORIZONTAL -> tempX--;
            case INVERSED_VERTICAL -> tempY--;
            case INVERSED_UPHILL -> { tempX--; tempY++; }
            case INVERSED_DOWNHILL -> { tempX--; tempY--; }
        }
        // Check boundaries
        if (tempX < 0 || tempX >= rowsColumns || tempY < 0 || tempY >= rowsColumns) {
            return null;
        }
        // Return a new Point instead of modifying the original
        return new Point(tempX, tempY);
    }
    
    char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    public void fillEmptySpaces() {
        for (int i = 0; i < rowsColumns; i++) {
            for (int j = 0; j < rowsColumns; j++) {
                if (blocks[i][j].value == '_') {
                    char randomChar = chars[random.nextInt(chars.length)];
                    blocks[i][j].value = randomChar;
                }
            }
        }
    }

    private static Direction getRandomDirection() {
        Random random = new Random();
        Direction[] values = Direction.values();
        return values[random.nextInt(values.length)];
    }

    // @Override
    @SuppressWarnings("override")
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void paintFoundWords() {
        for (int i = 0; i < rowsColumns; i++) {
            for (int j = 0; j < rowsColumns; j++) {
                if (blocks[i][j].type == BlockType.WORD) {
                    blocks[i][j].found = true;
                }
            }
        }
    }

    public static Color getRandomColor() {
        Random random = new Random();
        int r = random.nextInt(256); // Generate a random value for red (0-255)
        int g = random.nextInt(256); // Generate a random value for green (0-255)
        int b = random.nextInt(256); // Generate a random value for blue (0-255)

        return new Color(r, g, b);
    }
    
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;  // Cast to Graphics2D to access advanced features
        for (int i = 0; i < rowsColumns; i++) {
            for (int j = 0; j < rowsColumns; j++) {
                // Draw each letter
                g.setColor(Color.BLACK);
                g.drawString(
                String.valueOf(blocks[i][j].value),
                (blocks[i][j].point.x * Block.WIDTH) + 12,
                (blocks[i][j].point.y * Block.HEIGHT) + 20
                );

                if (blocks[i][j].word != null) {
                    g2d.drawString(blocks[i][j].word, 16 * Block.WIDTH, blocks[i][j].randomlyAssignedWordRow * Block.HEIGHT + 20);
                }
                
                // Draw the rectangle if it's the first letter
                if (blocks[i][j].found && blocks[i][j].firstLetter) {
                    g2d.setColor(Color.RED);
            
                    // Set the stroke thickness to 3 (or adjust as needed)
                    g2d.setStroke(new BasicStroke(3));
                
                    // Calculate the coordinates for the start and end points of the line
                    Point lastPoint = blocks[i][j].lastLetterLocation;
                    int x1 = blocks[i][j].point.x * Block.WIDTH + 16;
                    int y1 = blocks[i][j].point.y * Block.HEIGHT + 16;
                    int x2 = lastPoint.x * Block.WIDTH + 16;
                    int y2 = lastPoint.y * Block.HEIGHT + 16;
                
                    // Draw the line with the thicker stroke
                    g2d.drawLine(x1, y1, x2, y2);
                
                    // Reset the stroke to the default if needed for future drawings
                    g2d.setStroke(new BasicStroke(0));
                    
                    int x = 16 * Block.WIDTH;
                    int y = blocks[i][j].randomlyAssignedWordRow * Block.HEIGHT + 20;
                    int lineY = y - (g2d.getFontMetrics().getAscent() / 2);
                    g2d.drawLine(x, lineY, x + g2d.getFontMetrics().stringWidth(blocks[i][j].word), lineY);
                }
            }
        }
    }

    public class UnableToFitWordException extends Exception {
        public UnableToFitWordException(String message) {
            super(message);
        }
    }
}

