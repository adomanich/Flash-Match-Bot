package flashmatch.state;

public class StateController {

    private State state;

    public StateController(State state) {
        this.state = state;
    }

    public State getNext() {
        return state.getNext();
    }

    public void log() {
        state.log();
    }

    public int getCurrentStateId() {
        return state.getCurrentStateID();
    }
}
