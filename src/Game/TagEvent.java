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

    private ArrayList<EventAction> eventActions = new ArrayList<>();

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

    public void addCancelableAction(EventAction e){ eventActions.add(e); }

    public void enactEvent(){
        for (EventAction action : eventActions) action.doAction(this);
    }

    public interface EventAction{
        void doAction(TagEvent event);
    }
}
