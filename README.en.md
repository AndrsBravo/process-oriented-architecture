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

## Implementation with Generics

There is little benefit to the **Process-Oriented Software Architecture** proposal when using strongly typed processes like the example in the first version of this code (see branch `poa.v1.0`).

This version, we provide the recommended implementation to take full advantage of all its benefits.

### Generic Process Structure

This implementation consists in a single **generic process**, `Process<T>`, with parameterized input and return type, which all other use cases has to extend from. Processes with different implementations can exist depending on the project's needs, for example, processes with different input and return types, such as `Process<T, R>`.

The changes are described below:

1. Generic bases classes `Process<T>`, `ProcessExecutor<P extends Process<T>, T>`, and `IProcessRule<Process<T>>`, the same applies to their secondary classes for triggering events.

#### Process

New structure of the `Process` class will be:

```java

public class Process<T> {

    private Log log;
    private T objInit;
    private final Long id;
    private boolean active;
    private final List<Log> logs;
    private final String processName;
    private ProcessState processState;
    private ServiceResult<T> result;
    private IProcessExceptionHandler processExceptionHandler;
    private IProcessStateChangeHandler processStateChangeHandler;

    public Process(String processName) {
        this.logs = new ArrayList<>();
        this.id = new Random().nextLong();
        this.processName = processName;
    }
}
```

- The private initialization and return fields, along with their getters and setters, become generic: `private T objInit;` and `private ServiceResult<T> result;`.
- It receives the process name via the constructor from the classes that extend it.

#### Rules

New structure of the `IProcessRule` interface will be:

```java

@FunctionalInterface
public interface IProcessRule<P> {

    void apply(P process);

}

```

- Rules implementing this interface must specify the process which the rule applies to.

#### Executor

New structure of the `ProcessExecutor` class will be:

```java

public class ProcessExecutor<P extends Process<T>, T> {

    private final P process;
    private final Iterator<Supplier<IProcessRule<P>>> processRules;
    private IProcessBeforeRuleHandler<P, T> beforeRuleHandler;
    private IProcessAfterRuleHandler<P, T> afterRuleHandler;

    public ProcessExecutor(Supplier<P> processSupplier, Supplier<IProcessRule<P>>... rules) {
        this.process = processSupplier.get();
        this.processRules = Arrays.asList(rules).iterator();
    }
}
```

- It must be extended by providing two type parameters: the process, which extends `Process` (`P extends Process<T>`), and the input and return data type (`<T>`).
- Its constructor must receive a process instance supplier and the list of rules.

### Implementation in the Sample Use Case (CreateUserProcess)

For concrete implementations, such as the example we are building here, we extend the generic classes `Process<T>` and `ProcessExecutor<P extends Process<T>, T>`, also implement the `IProcessRule<P>` interface. This yields the following result:

**Process** `CreateUserProcess.java`

```java
public class CreateUserProcess extends Process<User> {

    public CreateUserProcess() {

        super("create_new_user_process");
    }

}

```

**Executor** `CreateUserProcessExecutor.java`

```java 
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

```

- It is recommended to implement a static `build()` method as a Factory Method in every subclass of `ProcessExecutor`, making it easy to use.

**Rule** `CreateUserProcessExecutor.java`

```java

public class CreateUserRule implements IProcessRule<CreateUserProcess> {

    @Override
    public void apply(CreateUserProcess createUserProcess) {
    }
}
```

**Use** will be like `CreateUserProcessTest.java`

```java

 @Test
public void shouldBeCompleted() {
    var user = new User("Andres", "Bravo", "abravo123", "andres.bravo@example.com", "password123");

    var createUserProcess = CreateUserProcessExecutor.build().Init(user).Execute();
}
```
