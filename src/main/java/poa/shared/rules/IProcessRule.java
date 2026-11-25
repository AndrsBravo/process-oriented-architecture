package poa.shared.rules;

import poa.user.process.CreateUserProcess;

@FunctionalInterface
public interface IProcessRule {

    void apply(CreateUserProcess createUserProcess);

}
