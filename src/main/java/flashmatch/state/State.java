package flashmatch.state;

public interface State {

    State getNext();

    void log();

    int getCurrentStateID();
}
