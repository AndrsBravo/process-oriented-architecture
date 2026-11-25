package poa.shared.service;

public class ServiceResult<T> {

    private T result;
    private Exception exception;
    private String message;

    public void setResult(T result) {
        this.result = result;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }

    public String getMessage() {
        return message;
    }

}
