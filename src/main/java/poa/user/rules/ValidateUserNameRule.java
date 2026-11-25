package poa.user.rules;

import poa.shared.logs.Log;
import poa.shared.rules.IProcessRule;
import poa.user.process.CreateUserProcess;

public class ValidateUserNameRule implements IProcessRule {

    @Override
    public void apply(CreateUserProcess createUserProcess) {

        System.out.println("-----   Applying rule: [ " + this.getClass().getSimpleName() + " ]");

        var user = createUserProcess.getInit();

        if (user.getUserName().length() < 8) {
            createUserProcess.addLog(new Log(this.getClass().getSimpleName(), "User Name does not match length allowed. "));
            createUserProcess.fail();
            return;
        }

        /**
         * All UserName business validation services called here.
         */
        createUserProcess.addLog(new Log(this.getClass().getSimpleName(), "User name successfully validated "));

    }

}
