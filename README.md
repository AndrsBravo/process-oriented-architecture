# Arquitectura Orientada a Procesos

Proyecto de ejemplo en Java que modela el registro de un usuario como un proceso compuesto por reglas ejecutadas en secuencia. Cada proceso mantiene su estado, registra los resultados de las reglas y permite notificar los cambios de estado.

## Requisitos

- Java 17 o posterior.
- Apache Maven instalado y disponible como `mvn`.
- Git para clonar el repositorio.

Verifica las instalaciones con:

```bash
java -version
mvn -version
```

## Clonar e instalar

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
