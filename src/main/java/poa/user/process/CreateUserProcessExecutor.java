package poa.user.process;

import java.util.Iterator;
import java.util.List;

import poa.shared.events.IProcessAfterRuleHandler;
import poa.shared.events.IProcessBeforeRuleHandler;
import poa.shared.events.IProcessExceptionHandler;
import poa.shared.events.IProcessStateChangeHandler;
import poa.shared.rules.IProcessRule;
import poa.user.entities.User;
import poa.user.rules.CreateUserRule;
import poa.user.rules.HashUserPasswordRule;
import poa.user.rules.ValidateUserEmailRule;
import poa.user.rules.ValidateUserNameRule;

public class CreateUserProcessExecutor {

    private final CreateUserProcess createUserProcess;
    private final Iterator<IProcessRule> processRules;
    private IProcessBeforeRuleHandler beforeRuleHandler;
    private IProcessAfterRuleHandler afterRuleHandler;

    public CreateUserProcessExecutor() {
        this.createUserProcess = new CreateUserProcess();
        this.processRules = List.of(
                new ValidateUserEmailRule(),
                new ValidateUserNameRule(),
                new HashUserPasswordRule(),
                new CreateUserRule()
        ).iterator();
    }

    private void onBeforeRule(IProcessRule rule) {
        if (this.beforeRuleHandler != null) {
            this.beforeRuleHandler.onBeforeRule(rule, this.createUserProcess);
        }
    }

    private void onAfterRule(IProcessRule rule) {
        if (this.afterRuleHandler != null) {
            this.afterRuleHandler.onAfterRule(rule, this.createUserProcess);
        }
    }

    public static CreateUserProcessExecutor Init(User user) {
        CreateUserProcessExecutor executor = new CreateUserProcessExecutor();
        executor.createUserProcess.init(user);
        return executor;
    }

    public CreateUserProcessExecutor OnStateChange(IProcessStateChangeHandler handler) {
        this.createUserProcess.setProcessStateChangeHandler(handler);
        return this;
    }

    public CreateUserProcessExecutor OnException(IProcessExceptionHandler handler) {
        this.createUserProcess.setProcessExceptionHandler(handler);
        return this;
    }

    public CreateUserProcessExecutor OnBeforeRule(IProcessBeforeRuleHandler handler) {
        this.beforeRuleHandler = handler;
        return this;
    }

    public CreateUserProcessExecutor OnAfterRule(IProcessAfterRuleHandler handler) {
        this.afterRuleHandler = handler;
        return this;
    }

    public CreateUserProcess Execute() {

        System.out.println("--   Executing [ " + this.createUserProcess.getProcessName() + " ] ID: [ " + this.createUserProcess.getId() + " ]");
        this.createUserProcess.start();
        while (this.createUserProcess.isActive() && this.processRules.hasNext()) {
            IProcessRule businessRule = this.processRules.next();
            this.onBeforeRule(businessRule);
            businessRule.apply(this.createUserProcess);
            this.onAfterRule(businessRule);
        }
        if (this.createUserProcess.isRunning()) {
            this.createUserProcess.complete();
        }

        System.out.println("--   Finishing [ " + this.createUserProcess.getProcessName() + " ] ID: [ " + this.createUserProcess.getId() + " ]");

        return this.createUserProcess;
    }
}
