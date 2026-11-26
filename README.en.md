# Process Oriented Software Architecture

[version en español](./README.md)

## Project

Example Java project that models user registration as a process composed of rules executed sequentially. Each process maintains its state, records the results of the rules, and allows state changes to be notified.

### Requirements

- Java 17 or later.
- Apache Maven installed and available as `mvn`.
- Git to clone the repository.

Verify the installations with:

```bash
java -version
mvn -version
```

### Clone and Install

Clone the repository from GitHub and navigate to the project directory:

```bash
git clone https://github.com/AndrsBravo/process-oriented-architecture.git
cd process-oriented-architecture
```

Build the project, run the tests, and install the artifact into your local Maven repository:

```bash
mvn clean install
```

To run only the tests:

```bash
mvn test
```

## What about (Process Oriented Software Architecture)

**Process-Oriented Software Architecture** is a way of organizing code around the idea that **each use case is a process** and **each step required to complete that process is treated as a rule**. Let's imagine the process of creating ***a new user***. Now let's think about the business rules required to complete this process: validate email, validate user\_name, etc. Finally, create the user.

At its core, **Process-Oriented Software Architecture** consists of three elements: the **process**, the **rule**, and the **executor**. Due to the architecture's flexibility, these three basic elements can incorporate new secondary elements according to the criteria and the implementation’s needs.

To better understand **Process-Oriented Software Architecture**, think of its components as language features: where a `ProcessExecutor` applies a list of `IProcessRule` instances to a `Process`, one after another.

### **Process**

```java
public class Process {

    private final Long id;
    private final String processName;
    private boolean active;
    private ProcessState state;
    private Entity objInit;
    private Result<User> result;
    private Log currentLog;
    private final List<Log> logs;
    private IProcessStateChangeHandler stateChangedHandler;
    private IProcessExceptionHandler exceptionHandler;

    public Process() {
        this.logs = new ArrayList<>();
        this.id = new Random().nextLong();
        this.processName = "create_new_user_process";
    }
}
```

**Process** is a ***Data Class***, represents a solution or use case. It provides the information needed to produce the appropriate outputs,  when execution finishes.

The following **characteristics** are recommended for **implementation**: The development team may incorporate and/or adapt them according to the project's needs.

1. The **process state** should be updated internally (`Process`, `ProcessExecutor`, or an `IProcessRule`) while running, and should be read-only from the outside. The team decides the most appropriate technique to incorporate this feature. E.g.: `ReadOnlyProcessInterface`, `MutatorClass` injected through the `Process` constructor, etc.

**A process** contains a set of private properties. It is a ***Data Class*** capable of raising events to notify listeners of state changes. The following properties are recommended, but can be extended to suit the project.

1. `long id`, Identity.  
2. `String name`, Name.  
3. `boolean active`, State.  
4. `enum ProcessState state`,  descriptive process state, with the following possible values:  
   * `PENDING`  Process just created.  
   * `RUNNING` while running.  
   * `COMPLETED` finished normally.  
   * `STOPPED` the process was stopped without completing.  
   * `FAILED` an error occurred that stopped the process.  
5. `Entity objInit`  the process input data (`input`).  
6. `Entity result | ProcessResult<Entity> result | ServiceResult<Entity> result | Optional<Entity> result` The process output or return value (`output`).  
7. `record Log currentLog` last log generated at process execution.  
8. `List<Log> logs | Stream<Log> logs` logs list generated during process execution.  
9. `interface IProcessStateChangedHandler onStateChanged` for process state changes listener.  
10. `interface IProcessExceptionHandler onException` for a process execution exception (error) listener.

**The process** also integrates many other important methods that may be classified as `private`, visible only inside the process; `execution`, visible from `ProcessExecutor`; or `internal`, visible from `ProcessExecutor` and `IProcessRule` implementations.

1. Execution methods (`internal`): `start()`, `stop()`, `complete()`, `fail()` these update process execution state and call the private event method `void stateHasChanged(ProcessState state)`.  
2. Action notification methods (`internal`): `void addLog(Log log)` action notification rise from a rule.  
3. Event-raising methods (`private`): `void stateHasChanged(ProcessState state)` a process state changed event will be raised.  
4. Event-raising methods (`internal`): `void exception(Exception exception)` an exception (error) caught event will be raised.  
5. Event-listening methods (`execution`): `void setOnProcessStateChanged(IProcessStateChangedHandler handler)`, `void setOnException(IProcessExceptionHandler handler)`.  
   

### **Rule**

```java
@FunctionalInterface
interface IProcessRule{
  void apply(Process process)
}
```

It is a Functional Interface (Java): `interface IProcessRule{}`. It defines the method `void apply(Process process)`. Rules receive the `Process` or a `ProcessMutatorClass` as a parameter of the `apply` method, execute its own actions, and update the process. These characteristics are recommended for rules:

1. **Single responsibility**: a rule executes only one action.  
2. **No logic implementation**: it is limited to use the software component that implements the business logic and then update the process.  
3. **One process per rule**: not apply the same rule to different processes.

### **Executor**

```java

public class ProcessExecutor {

    private final Process process;
    private final Iterator<IProcessRule> processRules;
    private IProcessBeforeRuleHandler beforeRuleHandler;
    private IProcessAfterRuleHandler afterRuleHandler;

    public ProcessExecutor() {
        this.process = new Process();
        this.processRules = List.of(
                new FirstRule(),
                new SecondRule(),
                new ThirdRule(),
                new LastRule()
        ).iterator();
    }
    
    public static ProcessExecutor Init(Entity init) {
        ProcessExecutor executor = new ProcessExecutor();
        executor.process.init(init);
        return executor;
    }
    
    public Process Execute() {

        this.process.start();
        while (this.process.isActive() && this.processRules.hasNext()) {
            IProcessRule businessRule = this.processRules.next();
            this.onBeforeRule(businessRule);
            businessRule.apply(this.process);
            this.onAfterRule(businessRule);
        }
        if (this.process.isRunning()) {
            this.process.complete();
        }

       return this.process;
    }
}
```

Implements a variation of the Pipeline or Chain of Responsibility pattern. It loops through the rules, applying each one to the process while the process remains active. Contains at its most basic form, the following properties.

1. `final Process process;` the process internally created and returned when execution finished.  
2. `final Iterator<IProcessRule> rules` the list of rules to apply a process during execution.  
3. `IProcessBeforeRuleHandler beforeRuleHandler` stores the event handler to notify that a rule is about to be applied. The class should contain a private method `void onBeforeRule(IProcessRule rule)` to raise this event.  
4. `IProcessAfterRuleHandler afterRuleHandler` stores the event handler to notify that a rule has been applied. The class should contain a private method `void onAfterRule(IProcessRule rule)` to raise this event.

It also owns **Factory**  **and**, as well as **the process** **Builder**. As a builder, it receives the values needed to construct the process. As a factory, it creates and returns instances of both itself and the process. Has those main methods:

1. `static ProcessExecutor Init(Entity entity)` class method that creates and returns the `ProcessExecutor` instance; the process is created, and it receives the `Entity` parameter through a method of the same name.  
2. `public ProcessExecutor OnStateChange(IProcessStateChangedHandler handler)` class method that receives the `IProcessStateChangedHandler` event handler, to assign it to the process through the method `void setOnProcessStateChanged(IProcessStateChangedHandler handler)`. Returns selfs instance (this).  
3. `public ProcessExecutor OnException(IProcessExceptionHandler handler)` class method that receives the `IProcessExceptionHandler` event handler, to assign it to the process through the method `void setOnException(IProcessExceptionHandler handler)`. Returns selfs instance (this).  
4. `public ProcessExecutor OnBeforeRule(IProcessBeforeRuleHandler handler)` class method that receives the `IProcessBeforeRuleHandler` event handler, so that the executor can notify that a rule is about to be applied. Returns selfs instance (this).  
5. `public ProcessExecutor OnAfterRule(IProcessAfterRuleHandler handler)` class method that receives the `IProcessAfterRuleHandler` event handler, so that the executor can notify that a rule has been applied. Returns selfs instance (this).  
6. `public Process Execute()` class method that starts process execution and returns it.
