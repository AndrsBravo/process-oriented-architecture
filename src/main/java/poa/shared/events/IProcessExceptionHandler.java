package poa.shared.events;

@FunctionalInterface
public interface IProcessExceptionHandler {

    public void onException(Exception exception);
}
