package Game;

/**
 * Created by Jared on 3/27/2018.
 */
public class Coordinate {

    private int x;
    private int y;

    public Coordinate(int x, int y){
        setPos(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPos(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void movePos(int xDiff, int yDiff){
        x += xDiff;
        y += yDiff;
    }

    @Override
    public String toString() {
        return String.format("[%1$d,%2$d]", x, y);
    }
}
