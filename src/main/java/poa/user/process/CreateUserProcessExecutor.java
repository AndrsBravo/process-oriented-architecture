package poa.user.process;

import poa.shared.process.ProcessExecutor;
import poa.user.entities.User;
import poa.user.rules.CreateUserRule;
import poa.user.rules.HashUserPasswordRule;
import poa.user.rules.ValidateUserEmailRule;
import poa.user.rules.ValidateUserNameRule;

public class CreateUserProcessExecutor extends ProcessExecutor<CreateUserProcess, User> {

    public CreateUserProcessExecutor() {

        super(CreateUserProcess::new,
                ValidateUserEmailRule::new,
                ValidateUserNameRule::new,
                HashUserPasswordRule::new,
                CreateUserRule::new);
    }

    public static CreateUserProcessExecutor build() {
        return new CreateUserProcessExecutor();
    }

}
