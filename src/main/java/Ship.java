import javafx.scene.paint.Color;

public class Ship {
    private int size;
    private Color color;
    private String type;

    public Ship(int size, Color color, String type) {
        this.size = size;
        this.color = color;
        this.type = type;
    }

    public Ship(int startX, int startY, Color white) {
    }

    public Ship(int size, Color color) {
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
}
