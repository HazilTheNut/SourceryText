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
     *  > success            : An adjustable boolean throughout the event. Events that are not successful will prevent that action. (Although there are special cases)
     *  > cancel             : The entire event can be canceled, overriding the 'success' feature, and it cannot be undone.
     *  > target and source  : The cause of the event and the receiving end are both known to all the Tags
     *  > cancelable actions : Some actions can be set to only run if the event is successful and is not canceled.
     *  > future actions     : Some actions can be set to only run after all Tags are parsed. For example, adding and removing Tags will cause ConcurrentModificationExceptions if they are done in the future.
     *  > gi (GameInstance)  : In most cases, the GameInstance is known.
     */

    private int amount;
    private boolean successful;
    private boolean canceled;

    private TagHolder target;
    private TagHolder source;
    private TagHolder tagOwner;

    private GameInstance gi;

    private ArrayList<EventAction> cancelableActions = new ArrayList<>(); //Useful for when you want do something when the event doesn't get cancelled
    private ArrayList<EventAction> futureActions     = new ArrayList<>(); //Useful for when you wnt to do things after all the tags are processed

    public TagEvent(int startingAmount, boolean successful, TagHolder source, TagHolder target, GameInstance gameInstance, TagHolder tagOwner){
        amount = startingAmount;
        this.successful = successful;
        canceled = false;
        this.target = target;
        this.source = source;
        this.tagOwner = tagOwner;
        gi = gameInstance;
    }

    public GameInstance getGameInstance() {
        return gi;
    }

    public void setSuccess(boolean success) { successful = success; }

    public boolean isSuccessful() {
        return successful;
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

    public boolean eventPassed() { return isSuccessful() && !isCanceled(); }

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
