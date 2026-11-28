package poa.user.rules;

import poa.shared.logs.Log;
import poa.shared.rules.IProcessRule;
import poa.user.process.CreateUserProcess;

public class CreateUserRule implements IProcessRule<CreateUserProcess> {

    @Override
    public void apply(CreateUserProcess createUserProcess) {

        System.out.println("-----   Applying rule: [ " + this.getClass().getSimpleName() + " ]");

        /* Create User service called here.

         var user = createUserProcess.getInit();
   
         var createUser = MyUserServiceFactory.CreateUser();
         var serviceResult = createUser.create(user);

         createUserProcess.setResult(serviceResult);
           
         if(serviceResult.error()){
             
             createUserProcess.addLog(new Log(this.getClass().getSimpleName(), "Error creating user: " + serviceResult.getErrorMessage()));
           
             createUserProcess.exception(serviceResult.getException());           
             createUserProcess.fail();
             return;
         }*/
        createUserProcess.addLog(new Log(this.getClass().getSimpleName(), "User name successfully created "));

    }

}
