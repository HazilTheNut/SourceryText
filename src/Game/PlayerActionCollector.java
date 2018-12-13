package Game;

import Data.Coordinate;
import Game.Spells.Spell;

public interface PlayerActionCollector {

    /**
     * Runs when player performs an attack
     * @param loc The position aimed at.
     * @param weapon The weapon used to attack
     */
    void onPlayerAttack(Coordinate loc, Item weapon);

    /**
     * Runs when the player throws an item.
     *
     * @param target The position aimed at
     * @param item The Item being thrown
     */
    void onPlayerThrowItem(Coordinate target, Item item);

    /**
     * Runs when the player moves
     * @param loc The new position of the player
     */
    void onPlayerMove(Coordinate loc);

    /**
     * Runs when the player readies a spell
     * @param loc The position aimed at
     * @param spell The spell being readied
     */
    void onPlayerReadySpell(Coordinate loc, Spell spell);

    /**
     * Runs when the player drags the mouse around while readying a spell
     * @param loc The position aimed at
     * @param spell The spell being readied
     */
    void onPlayerDragSpell(Coordinate loc, Spell spell);

    /**
     * Runs when the player casts a spell
     * @param loc The position aimed at
     * @param spell The spell being casted
     */
    void onPlayerCastSpell(Coordinate loc, Spell spell);

    /**
     * Runs when the player interacts with an object
     * @param loc The position of the object being interacted with.
     */
    void onPlayerInteract(Coordinate loc);
}
