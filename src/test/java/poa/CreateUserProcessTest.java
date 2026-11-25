package poa;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import poa.shared.events.ProcessState;
import poa.user.entities.User;
import poa.user.process.CreateUserProcessExecutor;

public class CreateUserProcessTest {

    @Test
    public void shouldBeCompleted() {

        /* 
            Should pass the test.
            Process has to be completed
            Process should call all the rules
            Has to have all the logs.
         */
        var user = new User("Andres", "Bravo", "abravo123", "andres.bravo@example.com", "password123");

        var createUserProcess = CreateUserProcessExecutor.Init(user)
                .OnStateChange(state -> {
                    System.out.println("----   Process Change Event Received State: " + state);
                })
                .OnBeforeRule((rule, process) -> {
                    System.out.println("-----   Before apply rule: [ " + rule.getClass().getSimpleName() + " ]");
                })
                .OnAfterRule((rule, process) -> {
                    System.out.println("-----   After apply rule: [ " + rule.getClass().getSimpleName() + " ]");
                }).Execute();

        // Process logs and results 
        System.out.println("Process state:          ----------  " + createUserProcess.getProcessState() + "  ----------");
        createUserProcess.getLogs().forEach(log -> System.out.println("Log: rule: [ " + log.rule() + " ] result: [ " + log.message() + " ]"));

        assertFalse(createUserProcess.isActive());
        assertFalse(createUserProcess.isRunning());
        assertTrue(createUserProcess.getProcessState() == ProcessState.COMPLETED);
    }

    @Test
    public void shouldBeFailed() {

        /* 
        Should pass the test. 
        The process has to fail, due to email validation: missing '@'
        Process stops and do not apply more rules. 
        Generate logs until the failure.

        ---- You would see the process failing in another rule: add the '@' and change either the 'username' or 'password'.  
         */
        var user = new User("Andres", "Bravo", "abravo123", "andres.bravoexample.com", "password123");

        var createUserProcess = CreateUserProcessExecutor.Init(user)
                .OnStateChange(state -> {
                    System.out.println("----   Process Change Event Received State: " + state);
                }).Execute();

        // Process logs and results 
        System.out.println("Process state:          ----------  " + createUserProcess.getProcessState() + "  ----------");
        createUserProcess.getLogs().forEach(log -> System.out.println("Log: rule: [ " + log.rule() + " ] result: [ " + log.message() + " ]"));

        assertFalse(createUserProcess.isActive());
        assertFalse(createUserProcess.isRunning());
        assertTrue(createUserProcess.getProcessState() == ProcessState.FAILED);
    }
}
