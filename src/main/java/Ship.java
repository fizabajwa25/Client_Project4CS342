import javafx.scene.paint.Color;

public class Ship {
    public boolean vertical; // might be better as private with a getter/setter
    private int size;
    private Color color;
    private String type;
    private int hitCount; // to track the hits the ship has taken

    // Fully parameterized constructor
    public Ship(int size, Color color, String type) {
        this.size = size;
        this.color = color;
        this.type = type;
        this.vertical = false; // default orientation
        this.hitCount = 0;
    }

    // Constructor with default color and type, meant for quick setup
    public Ship(int size, Color color) {
        this(size, color, "Unknown"); // delegate to the main constructor
    }

    // Constructor for starting position (if needed for specific logic, which is not detailed here)
    public Ship(int startX, int startY, Color color) {
        this(1, color); // Assuming default size 1 for lack of better information
        // startX and startY could be part of Ship's state if needed
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

    public boolean isAlive() {
        return hitCount < size; // Ship is alive if it hasn't been hit enough times
    }

    public void hit() {
        if (hitCount < size) {
            hitCount++; // Increment the hit count only if it hasn't reached the size of the ship
        }
    }

    public boolean isVertical() {
        return vertical; // Return the orientation of the ship
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical; // Allow changing the orientation
    }
}
