import javafx.scene.paint.Color;

public class Ship {
    private boolean vertical;
    private int size;
    private Color color;
    private String type;
    private int hitsTaken;

    // Full constructor
    public Ship(int size, Color color, String type) {
        this.size = size;
        this.color = color;
        this.type = type;
        this.hitsTaken = 0;
    }

    // Constructor for position and color only, type defaulted to "Unknown"
    public Ship(int size, Color color) {
        this(size, color, "Unknown");
    }

    // Constructors without functionality
    public Ship(int startX, int startY, Color white) {
        // Placeholder - typically startX and startY would be handled elsewhere
        this(1, white, "Unknown"); // Default size to 1, which needs proper handling if used seriously
    }

    // Getters and setters
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    // Method to check if the ship is still "alive" (not completely destroyed)
    public boolean isAlive() {
        return hitsTaken < size;
    }

    // Method to record a hit on the ship
    public void hit() {
        if (hitsTaken < size) {
            hitsTaken++;
        }
    }

    // Additional helper method to get remaining health of the ship
    public int getRemainingHealth() {
        return size - hitsTaken;
    }
}
