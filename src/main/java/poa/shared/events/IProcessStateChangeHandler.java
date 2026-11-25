package poa.shared.events;

@FunctionalInterface
public interface IProcessStateChangeHandler {

    public void onStateChange(ProcessState process);
}
