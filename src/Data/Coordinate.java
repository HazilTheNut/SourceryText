package Data;

import java.io.Serializable;

/**
 * Created by Jared on 3/27/2018.
 */
public class Coordinate implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

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
     * Returns a new Coordinate whose values are scaled from this Coordinate's x and y values. The non-integer results are rounded.
     * NOTE: This method does not alter the values of either Coordinate!
     * @param amount The factor by which the values are multiplied. Results are rounded back into integers before returning
     * @return A new Coordinate
     */
    public Coordinate multiply(double amount) { return new Coordinate((int)Math.round(x * amount), (int)Math.round(y * amount)); }

    /**
     * Returns a new Coordinate whose values are the x and y values of this and other Coordinate multiplied respectively, similar to a vector dot product.
     * NOTE: This method does not alter the values of either Coordinate!
     * @param other The Other Coordinate to multiply with this one
     * @return A new Coordinate
     */
    public Coordinate dotMultiply(Coordinate other) { return new Coordinate(x * other.getX(), y * other.getY()); }

    /**
     * Returns a new Coordinate whose values are scaled from this Coordinate's x and y values. The non-integer results are truncated.
     * NOTE: This method does not alter the values of either Coordinate!
     * @param amount The factor by which the values are multiplied. Results are rounded back into integers before returning
     * @return A new Coordinate
     */
    public Coordinate multiplyTruncated(double amount) { return new Coordinate((int)(x * amount), (int)(y * amount)); }

    public Coordinate normalize() {
        double x = getX();
        double y = getY();
        double dist = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        return multiply(1 / dist);
    }

    /**
     * Returns a new Coordinate whose values are the smaller values of this and the other coordinate, for each x and y respectively.
     * For example, flooring by [0,0] will set any negative x and y values to just zero.
     * NOTE: This method does not alter the values of either Coordinate!
     * @param other The other Coordinate to floor with.
     * @return A new Coordinate
     */
    public Coordinate floor(Coordinate other){
        return new Coordinate(Math.max(getX(), other.getX()), Math.max(getY(), other.getY()));
    }

    /**
     * Returns a new Coordinate whose values are the larger values of this and the other coordinate, for each x and y respectively.
     * For example, flooring by [100,100] will set any x and y values above 100 to 100
     * NOTE: This method does not alter the values of either Coordinate!
     * @param other The other Coordinate to ceil with.
     * @return A new Coordinate
     */
    public Coordinate ceil(Coordinate other){
        return new Coordinate(Math.min(getX(), other.getX()), Math.min(getY(), other.getY()));
    }

    /**
     * Returns the minimum number of 'steps' required to reach another coordinate, ignoring all terrain / walls
     * A 'step' is defined by a movement towards an adjacent space
     * @param other The target position to calculate the number of 'steps' required to reach it
     * @return The number of steps
     */
    public int stepDistance(Coordinate other) {
        return Math.abs(getX() - other.getX()) + Math.abs(getY() - other.getY());
    }

    /**
     * If a square was centered on this coordinate, how large would it have to be to include the other point into the square's interior?
     * @param other The coordinate to include into this hypothetical square
     * @return The "box distance" (1/2 the width of the square, -1)
     */
    public int boxDistance(Coordinate other){
        return Math.max(Math.abs(getX() - other.getX()), Math.abs(getY() - other.getY()));
    }

    /**
     * The length of the hypotenuse formed by the differences in the x and y positions of this and the other coordinate
     * Otherwise known as the traditionally-known form of distance
     * @param other The other Coordinate to find the distance to
     * @return The sqrt(dx^2 + dy^2) distance we all know and love
     */
    public double hypDistance(Coordinate other){
        return Math.sqrt(Math.pow(getX() - other.getX(), 2) + Math.pow(getY() - other.getY(), 2));
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

    public Coordinate copy() {
        return new Coordinate(x, y);
    }
}
