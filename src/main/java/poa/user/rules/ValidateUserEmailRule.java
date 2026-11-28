package poa.user.rules;

import poa.shared.logs.Log;
import poa.shared.rules.IProcessRule;
import poa.user.process.CreateUserProcess;

public class ValidateUserEmailRule implements IProcessRule<CreateUserProcess> {

    @Override
    public void apply(CreateUserProcess createUserProcess) {

        System.out.println("-----   Applying rule: [ " + this.getClass().getSimpleName() + " ]");

        var user = createUserProcess.getInit();

        if (!user.getEmail().contains("@")) {
            createUserProcess.addLog(new Log(this.getClass().getSimpleName(), "Not valid email. "));
            createUserProcess.fail();
            return;
        }

        /**
         * All email business validation services called here.
         */
        createUserProcess.addLog(new Log(this.getClass().getSimpleName(), "Email successfully validated "));
    }

}
