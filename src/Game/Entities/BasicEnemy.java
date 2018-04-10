package Game.Entities;

import Engine.SpecialText;
import Game.Coordinate;
import Game.Player;
import Game.Registries.ItemRegistry;

import java.util.ArrayList;

/**
 * Created by Jared on 4/3/2018.
 */
public class BasicEnemy extends CombatEntity {

    public BasicEnemy(){
        setMaxHealth(10);
        ItemRegistry registry = new ItemRegistry();
        setWeapon(registry.generateItem(4).setQty(50));
    }

    protected int detectRange = 15;

    @Override
    public void onTurn() {
        Player player = getGameInstance().getPlayer();
        int distance = player.getLocation().stepDistance(getLocation());
        if (distance < 2){
            doWeaponAttack(player.getLocation());
        } else if (distance < detectRange){
            pathToPosition(player.getLocation());
        }
    }

    protected void pathToPosition(Coordinate target){
        currentPoints.clear();
        futurePoints.clear();
        futurePoints.add(new SpreadPoint(target.getX(), target.getY(), 0));
        doPathing(1);
    }

    private ArrayList<SpreadPoint> currentPoints = new ArrayList<>();
    private ArrayList<SpreadPoint> futurePoints = new ArrayList<>();

    private void doPathing(int n){
        if (n > detectRange) return;
        moveFutureToPresentPoints();
        for (SpreadPoint point : currentPoints){
            attemptFuturePoint(point.x+1, point.y, n);
            attemptFuturePoint(point.x-1, point.y, n);
            attemptFuturePoint(point.x, point.y+1, n);
            attemptFuturePoint(point.x, point.y-1, n);
        }
        for (SpreadPoint pt : futurePoints){
            if (pt.x == getLocation().getX() && pt.y == getLocation().getY()){
                for (SpreadPoint cp : currentPoints){
                    if (getLocation().stepDistance(new Coordinate(cp.x, cp.y)) <= 1){
                        teleport(new Coordinate(cp.x, cp.y));
                        return;
                    }
                }
            }
        }
        doPathing(n+1);
    }

    private void moveFutureToPresentPoints(){
        for (SpreadPoint point : futurePoints) if (!currentPoints.contains(point)) {
            currentPoints.add(point);
        }
        futurePoints.clear();
    }

    private void attemptFuturePoint(int col, int row, int generation){
        if (getGameInstance().isSpaceAvailable(new Coordinate(col, row)) || getLocation().equals(new Coordinate(col, row))) futurePoints.add(new SpreadPoint(col, row, generation));
    }

    private class SpreadPoint{
        int x;
        int y;
        int g; //Shorthand for 'generation'
        private SpreadPoint(int x, int y, int g){
            this.x = x;
            this.y = y;
            this.g = g;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SpreadPoint){
                SpreadPoint other = (SpreadPoint)obj;
                return x == other.x && y == other.y;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("PathPoint:[%1$d,%2$d,%3$d]", x, y, g);
        }
    }
}
