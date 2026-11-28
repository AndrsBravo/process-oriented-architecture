package poa.user.process;

import poa.shared.process.Process;
import poa.user.entities.User;

public class CreateUserProcess extends Process<User> {

    public CreateUserProcess() {

        super("create_new_user_process");
    }

}
