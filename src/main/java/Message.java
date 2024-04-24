import java.awt.*;
import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 42L;
    private static final int GRID_SIZE = 10; // Assuming a grid size of 10x10

    private int[][] boardState;
    private int[][] opponentBoardState = new int[GRID_SIZE][GRID_SIZE];
    private Rectangle[][] gridRectangles = new Rectangle[GRID_SIZE][GRID_SIZE]; // GUI rectangles for the grid


    private MessageType type;

    public enum MessageType {
        SET_BOARD,
        GET_BOARD,
        SET_OPPONENT_BOARD,
        GET_OPPONENT_BOARD,
        TRY_MOVE,
        HIT,
        SET_BOARD_PLAYER_VS_PLAYER, GET_BOARD_PLAYER_VS_PLAYER, MISS
    }

    public Message(MessageType type) {
        this.type = type;
    }

    public Message(MessageType type, int[][] boardState) {
        this.type = type;
        this.boardState = boardState;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int[][] getBoardState() {
        return boardState;
    }

    public void setBoardState(int[][] boardState) {
        this.boardState = boardState;
    }
}
