package Game;

/**
 * Created by Jared on 4/1/2018.
 */
public class TagEvent {

    private int amount;
    private boolean successful;
    private boolean canceled;

    public TagEvent(int startingAmount, boolean successful){
        amount = startingAmount;
        this.successful = successful;
        canceled = false;
    }

    public void setSuccess(boolean success) { successful = success; }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel() { canceled = true; }
}
