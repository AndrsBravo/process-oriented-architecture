package poa.shared.events;

import poa.shared.rules.IProcessRule;
import poa.user.process.CreateUserProcess;

@FunctionalInterface
public interface IProcessAfterRuleHandler {

    public void onAfterRule(IProcessRule rule, CreateUserProcess process);
}
