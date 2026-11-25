package poa.shared.events;

import poa.shared.rules.IProcessRule;
import poa.user.process.CreateUserProcess;

@FunctionalInterface
public interface IProcessBeforeRuleHandler {

    public void onBeforeRule(IProcessRule rule, CreateUserProcess process);
}
