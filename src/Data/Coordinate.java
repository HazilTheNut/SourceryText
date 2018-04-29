package Data;

/**
 * Created by Jared on 3/27/2018.
 */
public class Coordinate {
    /**
     * Coordinate:
     *
     * The basic of location in SourceryText.
     * It is a pair of integers with convenience functions for adding, subtracting, etc. two different coordinates.
     */

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

    /**
     * Re-assigns the x and y values of the Coordinate
     * @param x new x pos
     * @param y new y pos
     */
    public void setPos(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void movePos(int xDiff, int yDiff){
        x += xDiff;
        y += yDiff;
    }

    /**
     * Returns a new Coordinate that is the sum of the Coordinate accessing the method and the parameter of the method
     * NOTE: This method does not alter the values of either Coordinate!
     * @param other The other Coordinate to add onto this one
     * @return A new Coordinate
     */
    public Coordinate add(Coordinate other){
        return new Coordinate(x + other.getX(), y + other.getY());
    }

    /**
     * Returns a new Coordinate that is the difference of the Coordinate accessing the method and the parameter of the method
     * NOTE: This method does not alter the values of either Coordinate!
     * @param other The other Coordinate to subtract from this one
     * @return A new Coordinate
     */
    public Coordinate subtract(Coordinate other){
        return new Coordinate(x - other.getX(), y - other.getY());
    }

    /**
     * Returns a new Coordinate whose values are scaled from this Coordinate's x and y values
     * NOTE: This method does not alter the values of either Coordinate!
     * @param amount The factor by which the values are multiplied. Results are truncated back into integers before returning
     * @return A new Coordinate
     */
    public Coordinate multiply(double amount) { return new Coordinate((int)(x * amount), (int)(y * amount)); }

    /**
     * Returns the minimum number of 'steps' required to reach another coordinate, ignoring all terrain / walls
     * A 'step' is defined by a movement towards an adjacent space
     * @param other The target position to calculate the number of 'steps' required to reach it
     * @return The number of steps
     */
    public int stepDistance(Coordinate other) {
        return Math.abs(getX() - other.getX()) + Math.abs(getY() - other.getY());
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
