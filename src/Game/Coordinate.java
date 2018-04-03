package Game;

import Game.Entities.Entity;

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

    public Coordinate add(Coordinate other){
        return new Coordinate(x + other.getX(), y + other.getY());
    }

    public Coordinate subtract(Coordinate other){
        return new Coordinate(x - other.getX(), y - other.getY());
    }

    @Override
    public String toString() {
        return String.format("[%1$d,%2$d]", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Coordinate){
            Coordinate other = (Coordinate)obj;
            return x == other.getX() && y == other.getY();
        }
        return false;
    }
}
