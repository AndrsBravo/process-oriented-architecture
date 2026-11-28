package poa.shared.events;

import poa.shared.rules.IProcessRule;

@FunctionalInterface
public interface IProcessBeforeRuleHandler<P extends poa.shared.process.Process<T>, T> {

    public void onBeforeRule(IProcessRule rule, P process);
}
