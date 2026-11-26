# Arquitectura de Software Orientada a Procesos

[english version](./README.en.md)

## Proyecto

Proyecto de ejemplo en Java que modela el registro de un usuario como un proceso compuesto por reglas ejecutadas en secuencia.

### Requisitos

- Java 17 o posterior.
- Apache Maven instalado y disponible como `mvn`.
- Git para clonar el repositorio.

Verifica las instalaciones con:

```bash
java -version
mvn -version
```

### Clonar e instalar

Clona el repositorio desde GitHub y entra en el directorio:

```bash
git clone https://github.com/AndrsBravo/process-oriented-architecture.git
cd process-oriented-architecture
```

Compila el proyecto, ejecuta las pruebas e instala el artefacto en el repositorio local de Maven:

```bash
mvn clean install
```

Para ejecutar solo las pruebas:

```bash
mvn test
```

## Qué es la Arquitectura de Software Orientada a Procesos


La **Arquitectura de Software Orientada a Procesos** es una forma de estructurar el código, considerando que **cada caso de uso, es un proceso**, y **cada etapa para completar el proceso, se considera una regla**. Imaginemos el proceso para crear ***un nuevo usuario***, ahora pensemos en las reglas de negocio necesarias para completar este proceso, validar email, validar user\_name, etc. Por último, crear usuario.

La **Arquitectura de Software Orientada a Procesos** en su forma base, está compuesta por 3 elementos, el **proceso**, la **regla** y el **ejecutor**. Debido a la flexibilidad de la arquitectura, estos tres elementos base pueden incorporar nuevos elementos secundarios según criterios y necesidades de la implementación.

Para entender mejor la **Arquitectura de Software Orientada a Procesos**, piensa en sus componentes, como si fueran característica del lenguaje de programación en la que un `ProcessExecutor` aplica en secuencia, una lista de `IProcessRule` a un `Process`.

### **Proceso**

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

***Data Class*** `class Process{}` que estructura una solución o caso de uso. Al finalizar su ejecución ofrece la información necesaria para producir los outputs adecuados.

Se recomiendan las siguientes **características de implementación**. El equipo de desarrollo podrá incorporar y/o adecuarlas, según las necesidades del proyecto.

1. El **estado del proceso** se actualiza desde el interior (desde `Process`, desde `ProcessExecutor` o desde una `IProcessRule`), mientras está en ejecución, es de solo lectura para el exterior. El equipo puede decidir la técnica más adecuada, si decide incorporar esta característica. Ej: `ReadOnlyProcessInterface`, `MutatorClass` inyectado por constructor del `Process`, etc.

**Un proceso**, contiene un conjunto de propiedades privadas, representa una ***Data Class***, con capacidad de lanzar eventos que notifican su estado. Tiene propiedades recomendadas y pueden ser ampliadas según las características del proyecto.

1. `long id`, Identidad.  
2. `String name`, Nombre  
3. `boolean active`, Estado.  
4. `enum ProcessState state`, Estado descriptivo, con los valores posibles  
   * `PENDING` Cuando se crea el proceso.  
   * `RUNNING` mientras se ejecuta el proceso.  
   * `COMPLETED` finalizado de forma normal.  
   * `STOPPED` el proceso se detuvo sin completar  
   * `FAILED` se produjo un error que detuvo el proceso.  
5. `Entity objInit` representa el dato de entrada (input) del proceso.  
6. `Entity result | ProcessResult<Entity> result | ServiceResult<Entity> result | Optional<Entity> result` representa el dato de salida o retorno (output) del proceso.  
7. `record Log currentLog` el último log generado en la ejecución del proceso.  
8. `List<Log> logs | Stream<Log> logs` listado de todos los logs producidos durante la ejecución del proceso.  
9. `interface IProcessStateChangedHandler onStateChanged` para escuchar los cambios de estado del proceso.  
10. `interface IProcessExceptionHandler onException` para notificar si una excepción (error) se produjo durante la ejecución del proceso.

Los **Procesos** también integran métodos importantes que contribuyen a la actualización del estado. Estos pueden ser `private` solo del proceso, `execution` vistos desde el `ProcessExecutor`, `internal` vistos desde el `ProcessExecutor` y desde las `IProcessRule`.

1. Métodos de ejecución (`internal`), `start()`, `stop()`, `complete()`, `fail()` actualizan el estado de ejecución del proceso y llaman al método de evento privado `void stateHasChanged(ProcessState state)`.  
2. Métodos de notificación de acción (`internal`), `void addLog(Log log)` la regla notifica al proceso que una acción se ha ejecutado.  
3. Métodos para lanzar eventos (`private`), `void stateHasChanged(ProcessState state)` se lanzará el evento, el estado del proceso ha cambiado.  
4. Métodos para lanzar eventos (`internal`), `void exception(Exception exception)` se lanzará un evento que notifica una excepción (error).  
5. Métodos para atrapar eventos (`execution`), `void setOnProcessStateChanged(IProcessStateChangedHandler handler)`, `void setOnException(IProcessExceptionHandler handler)`.

### **Regla**

```java
@FunctionalInterface
interface IProcessRule{
  void apply(Process process)
}

```

Es una Functional Interface (java) `interface IProcessRule{}`. Define el método `void apply(Process process)`. Las reglas reciben el `Process` o una `ProcessMutatorClass` como parámetro del método `apply`, ejecutan la acción que corresponda y actualizan el proceso. Se recomienda que las reglas cumplan las siguientes características:

1. **Responsabilidad única** cada regla ejecute solo una acción (Simple Responsibility).  
2. **No implementación lógica**, se limita a llamar a los componentes del software que contienen las implementaciones de la lógica de negocio y actualiza el proceso.  
3. **Una regla para un proceso**, no se aplica la misma regla a distintos procesos.

### **Ejecutor**

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
                new SecundRule(),
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

Es el coordinador de la ejecución del proceso. La clase `class ProcessExecutor{}`, implementa una variante de pipe line o de cadena de responsabilidad, en la que aplica en bucle todas las reglas al proceso, mientras este está activo. Contiene en su forma más básica las siguientes propiedades.

1. `final Process process;` proceso que internamente crea y retorna al ejecutarse.  
2. `final Iterator<IProcessRule> rules` lista de reglas que se aplicaran al proceso en ejecución.  
3. `IProcessBeforeRuleHandler beforeRuleHandler` guarda el manejador de eventos para notificar que se aplicará una regla. La clase contendrá un método privado `void onBeforeRule(IProcessRule rule)` para lanzar este evento.  
4. `IProcessAfterRuleHandler afterRuleHandler` guarda el manejador de eventos para notificar que una regla ya se aplico. La clase contendrá un método privado `void onAfterRule(IProcessRule rule)` para lanzar este evento.

También **builder y factory de sí mismo y de proceso**. Cómo builder recibe los valores para la construcción del proceso. Como factory retorna una instancia de sí mismo y del proceso. Sus principales métodos:

1. `static ProcessExecutor Init(Entity entity)` método de clase, crea y retorna la instancia de `ProcessExecutor`, se crea el proceso, el parámetro `Entity` lo recibirá el proceso en un método del mismo nombre.  
2. `public ProcessExecutor OnStateChange(IProcessStateChangedHandler handler)` método de clase, recibe el manejador de evento `IProcessStateChangedHandler`, para asignarlo al proceso mediante el método `void setOnProcessStateChanged(IProcessStateChangedHandler handler)`  
3. `public ProcessExecutor OnException(IProcessExceptionHandler handler)` método de clase, recibe el manejador de evento `IProcessExceptionHandler`, para asignarlo al proceso mediante el método `void setOnException(IProcessExceptionHandler handler)`.  
4. `public ProcessExecutor OnBeforeRule(IProcessBeforeRuleHandler handler)` método de clase, recibe el manejador de evento `IProcessBeforeRuleHandler`, para que el ejecutor notifique se aplicara una regla.  
5. `public ProcessExecutor OnAfterRule(IProcessAfterRuleHandler handler)` método de clase, recibe el manejador de evento `IProcessAfterRuleHandler`, para que el ejecutor notifique que una regla ya se aplico.  
6. `public Process Execute()` método de clase, inicia la ejecución del proceso y lo retorna.
