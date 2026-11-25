package poa.user.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import poa.shared.events.IProcessExceptionHandler;
import poa.shared.events.IProcessStateChangeHandler;
import poa.shared.events.ProcessState;
import poa.shared.logs.Log;
import poa.shared.service.ServiceResult;
import poa.user.entities.User;

public class CreateUserProcess {

    private Log log;
    private User user;
    private final Long id;
    private boolean active;
    private final List<Log> logs;
    private final String processName;
    private ProcessState processState;
    private ServiceResult<User> result;
    private IProcessExceptionHandler processExceptionHandler;
    private IProcessStateChangeHandler processStateChangeHandler;

    public CreateUserProcess() {
        this.logs = new ArrayList<>();
        this.id = new Random().nextLong();
        this.processName = "create_new_user_process";
    }

    private void ProcessStateHasChanged(ProcessState state) {
        this.processState = state;
        if (this.processStateChangeHandler != null) {
            this.processStateChangeHandler.onStateChange(this.processState);
        }
    }

    public void exception(Exception exception) {
        if (this.processExceptionHandler != null) {
            this.processExceptionHandler.onException(exception);
        }
    }

    public void init(User user) {
        this.user = user;
    }

    public void setProcessStateChangeHandler(IProcessStateChangeHandler handler) {
        this.processStateChangeHandler = handler;
    }

    public void setProcessExceptionHandler(IProcessExceptionHandler handler) {
        this.processExceptionHandler = handler;
    }

    public Long getId() {
        return id;
    }

    public String getProcessName() {
        return processName;
    }

    public User getInit() {
        return this.user;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public Log currentLog() {
        return this.log;
    }

    public ProcessState getProcessState() {
        return processState;
    }

    public void addLog(Log log) {
        this.log = log;
        this.logs.add(log);
    }

    public ServiceResult<User> getResult() {
        return result;
    }

    public void setResult(ServiceResult<User> result) {
        this.result = result;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isRunning() {
        return this.processState == ProcessState.RUNNING;
    }

    public void start() {
        this.active = true;
        this.ProcessStateHasChanged(ProcessState.RUNNING);
    }

    public void complete() {
        this.active = false;
        this.ProcessStateHasChanged(ProcessState.COMPLETED);
    }

    public void stop() {
        this.active = false;
        this.ProcessStateHasChanged(ProcessState.STOPPED);
    }

    public void fail() {
        this.active = false;
        this.ProcessStateHasChanged(ProcessState.FAILED);
    }

}
