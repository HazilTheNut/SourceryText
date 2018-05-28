package Game;

import java.util.ArrayList;

/**
 * Created by Jared on 4/1/2018.
 */
public class TagEvent {

    private int amount;
    private boolean successful;
    private boolean canceled;

    private TagHolder target;
    private TagHolder source;

    private GameInstance gi;

    private ArrayList<EventAction> cancelableActions = new ArrayList<>(); //Useful for when you want do something when the event doe'nst get cancelled
    private ArrayList<EventAction> futureActions     = new ArrayList<>(); //Useful for when you wnt to do things after all the tags are processed

    public TagEvent(int startingAmount, boolean successful, TagHolder source, TagHolder target, GameInstance gameInstance){
        amount = startingAmount;
        this.successful = successful;
        canceled = false;
        this.target = target;
        this.source = source;
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
