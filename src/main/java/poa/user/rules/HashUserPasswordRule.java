package poa.user.rules;

import poa.shared.logs.Log;
import poa.shared.rules.IProcessRule;
import poa.user.process.CreateUserProcess;

public class HashUserPasswordRule implements IProcessRule<CreateUserProcess> {

    @Override
    public void apply(CreateUserProcess createUserProcess) {

        System.out.println("-----   Applying rule: [ " + this.getClass().getSimpleName() + " ]");

        var user = createUserProcess.getInit();

        if (user.getPassword().length() < 8) {
            createUserProcess.addLog(new Log(this.getClass().getSimpleName(), "Password does not match length allowed. "));
            createUserProcess.fail();
            return;
        }

        /**
         * Services that implements the logic to hash the user's password.
         */
        createUserProcess.addLog(new Log(this.getClass().getSimpleName(), "Password successfully hashed "));
    }

}
