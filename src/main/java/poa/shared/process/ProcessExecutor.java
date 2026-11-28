package poa.shared.process;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import poa.shared.events.IProcessAfterRuleHandler;
import poa.shared.events.IProcessBeforeRuleHandler;
import poa.shared.events.IProcessExceptionHandler;
import poa.shared.events.IProcessStateChangeHandler;
import poa.shared.rules.IProcessRule;

public class ProcessExecutor<P extends Process<T>, T> {

    private final P process;
    private final Iterator<Supplier<IProcessRule<P>>> processRules;
    private IProcessBeforeRuleHandler<P, T> beforeRuleHandler;
    private IProcessAfterRuleHandler<P, T> afterRuleHandler;

    public ProcessExecutor(Supplier<P> processSupplier, Supplier<IProcessRule<P>>... rules) {
        this.process = processSupplier.get();
        this.processRules = Arrays.asList(rules).iterator();
    }

    private void onBeforeRule(IProcessRule rule) {
        if (this.beforeRuleHandler != null) {
            this.beforeRuleHandler.onBeforeRule(rule, this.process);
        }
    }

    private void onAfterRule(IProcessRule rule) {
        if (this.afterRuleHandler != null) {
            this.afterRuleHandler.onAfterRule(rule, this.process);
        }
    }

    public ProcessExecutor<P, T> Init(T init) {
        this.process.init(init);
        return this;
    }

    public ProcessExecutor<P, T> OnStateChange(IProcessStateChangeHandler handler) {
        this.process.setProcessStateChangeHandler(handler);
        return this;
    }

    public ProcessExecutor<P, T> OnException(IProcessExceptionHandler handler) {
        this.process.setProcessExceptionHandler(handler);
        return this;
    }

    public ProcessExecutor<P, T> OnBeforeRule(IProcessBeforeRuleHandler handler) {
        this.beforeRuleHandler = handler;
        return this;
    }

    public ProcessExecutor<P, T> OnAfterRule(IProcessAfterRuleHandler handler) {
        this.afterRuleHandler = handler;
        return this;
    }

    public P Execute() {

        System.out.println("--   Executing [ " + this.process.getProcessName() + " ] ID: [ " + this.process.getId() + " ]");
        this.process.start();
        while (this.process.isActive() && this.processRules.hasNext()) {
            Supplier<IProcessRule<P>> ruleSupplier = this.processRules.next();
            IProcessRule<P> businessRule = ruleSupplier.get();
            this.onBeforeRule(businessRule);
            businessRule.apply(this.process);
            this.onAfterRule(businessRule);
        }
        if (this.process.isRunning()) {
            this.process.complete();
        }

        System.out.println("--   Finishing [ " + this.process.getProcessName() + " ] ID: [ " + this.process.getId() + " ]");

        return this.process;
    }
}
