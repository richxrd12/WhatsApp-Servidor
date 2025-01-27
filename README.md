# WhatsApp Clone

Este proyecto es una copia de WhatsApp desarrollada en **JavaFX**. Proporciona tanto un cliente como un servidor, con funcionalidades diseñadas para simular una experiencia de mensajería similar a la aplicación original.

## Tecnologías utilizadas

- **JavaFX**: Framework para el desarrollo de interfaces gráficas.
- **Docker Desktop**: Para gestionar el servidor y la base de datos.
- **Gson**: Librería para trabajar con JSON.
- **MySQL**: Base de datos utilizada en el servidor.

## Requisitos previos

Antes de comenzar, asegúrate de tener instalado:

1. **Java**: JDK 22 o superior.
2. **IntelliJ IDEA**: IDE recomendado para ejecutar el proyecto.
3. **Docker Desktop**: https://www.docker.com/products/docker-desktop/
4. **JAR de Gson**: https://mvnrepository.com/artifact/com.google.code.gson/gson/2.11.0
5. **JAR de MySQL**: https://www.mysql.com/products/connector/ (Coger el JDBC)
6. **Asegurarse de poner los JAR en la librería del proyecto.**

## Instrucciones para la instalación y uso

### 1. Configuración del servidor

1. Localiza el `build.bat`, está en la raíz del proyecto.
2. Inicia el **Docker Desktop**.
3. Ejecuta los siguientes comandos según lo que necesites:
    - **Crear el contenedor por primera vez**: `.\build.bat -f`
    - **Levantar un contenedor ya existente**: `.\build.bat`
    - **Eliminar un contenedor existente**: `.\build.bat -d`

### 2. Ejecución del servidor

1. Asegúrate de que el contenedor de la base de datos esté levantado.
2. Ejecuta la aplicación del servidor desde IntelliJ IDEA.

### 3. Ejecución de los clientes

1. Ejecuta la aplicación del cliente desde IntelliJ IDEA.
2. Conecta múltiples clientes al servidor para probar las funcionalidades de mensajería.

## Estructura del proyecto

El proyecto sigue el patrón **Modelo-Vista-Controlador (MVC)**:

- **Modelo**: Gestión de datos y lógica de negocio. (En este proyecto el servidor es el Modelo)
- **Vista**: Interfaz gráfica desarrollada con JavaFX.
- **Controlador**: Comunicación entre el modelo y la vista.

## Estado del proyecto

Actualmente, el proyecto se encuentra en **desarrollo**.
