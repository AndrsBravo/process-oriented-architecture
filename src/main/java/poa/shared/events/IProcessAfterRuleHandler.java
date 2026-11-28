package poa.shared.events;

import poa.shared.rules.IProcessRule;

@FunctionalInterface
public interface IProcessAfterRuleHandler<P extends poa.shared.process.Process<T>, T> {

    public void onAfterRule(IProcessRule rule, P process);
}
