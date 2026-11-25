package poa.shared.events;

import poa.shared.logs.Log;

@FunctionalInterface
public interface IProcessLogHandler {

    public void onLogAdded(Log log);
}
