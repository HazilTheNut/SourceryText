package Game;

import java.util.ArrayList;

/**
 * Created by Jared on 4/1/2018.
 */
public class TagEvent {

    /**
     * TagEvent:
     *
     * A structured approach to handling the effects of Tags compounding on top of each other.
     *
     * It has the following features:
     *  > amount             : An adjustable number throughout the event. Be careful with multiplicative types of operations, considering that Tags can be called upon in any order
     *  > cancel             : The entire event can be canceled; once set to true, it cannot be set back to false.
     *  > target and source  : The cause of the event and the receiving end are both known to all the Tags
     *  > tagOwner           : The TagHolder that owns the Tag. Target and Source are occasionally both not the tag owner.
     *  > cancelable actions : Some actions can be set to only run if the event is successful and is not canceled.
     *  > future actions     : Some actions can be set to only run after all Tags are parsed. For example, adding and removing Tags will cause ConcurrentModificationExceptions if they are done in the future.
     *  > gi (GameInstance)  : In most cases, the GameInstance is known.
     */

    private int amount;
    private boolean canceled;

    private TagHolder target;
    private TagHolder source;
    private TagHolder tagOwner;

    private GameInstance gi;

    private ArrayList<EventAction> cancelableActions = new ArrayList<>(); //Useful for when you want do something when the event doesn't get cancelled
    private ArrayList<EventAction> futureActions     = new ArrayList<>(); //Useful for when you wnt to do things after all the tags are processed

    public TagEvent(int startingAmount, TagHolder source, TagHolder target, GameInstance gameInstance, TagHolder tagOwner){
        amount = startingAmount;
        canceled = false;
        this.target = target;
        this.source = source;
        this.tagOwner = tagOwner;
        gi = gameInstance;
    }

    public GameInstance getGameInstance() {
        return gi;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public int getAmount(){
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void cancel() { canceled = true; }

    public TagHolder getSource() {
        return source;
    }

    public TagHolder getTarget() {
        return target;
    }

    public TagHolder getTagOwner() {
        return tagOwner;
    }

    public boolean eventPassed() { return !isCanceled(); }

    public void addCancelableAction(EventAction e){ cancelableActions.add(e); }

    public void addFutureAction(EventAction e){ futureActions.add(e); }

    public void doFutureActions(){
        for (EventAction action : futureActions) action.doAction(this);
    }

    public void doCancelableActions(){
        for (EventAction action : cancelableActions) action.doAction(this);
    }


    public interface EventAction{
        void doAction(TagEvent event);
    }
}
