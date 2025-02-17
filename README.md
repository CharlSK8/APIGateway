# API Gateway - Banco

Este proyecto implementa un API Gateway para el sistema bancario, proporcionando enrutamiento, balanceo de carga y seguridad mediante autenticación JWT. Se ha desarrollado utilizando Spring Cloud Gateway y ActiveMQ para la comunicación de eventos.

## Descripción General

El API Gateway actúa como un intermediario entre los clientes y los microservicios, asegurando que todas las solicitudes estén autenticadas antes de ser procesadas. Además, gestiona eventos de autenticación mediante ActiveMQ para manejar eventos de cierre de sesión.

## Dockerizar la Aplicación desde GHCR

Este documento proporciona los pasos para obtener, ejecutar y administrar un contenedor Docker con una imagen almacenada en GitHub Container Registry (GHCR).

## Prerrequisitos

- Tener instalado [Podman](https://podman.io/)
- Acceso a GitHub Container Registry (GHCR)
- Haber iniciado sesión en GHCR con Docker:

  ```sh
  podman login ghcr.io -u <USERNAME> -p <PASSWORD> ghcr.io 
  ```

## Descargar y ejecutar el proyecto en contenedor

- **Crear network**

    ```sh
    podman network create red-bank
    ```

- **Descargar la imagen desde GHCR**

    ```sh
    podman pull ghcr.io/charlsk8/gateway:v1.0.0
    ```

- **Correr el contenedor**

    ```sh
    podman run --rm --name gateway --network=red-bank -p 9092:9092 -d ghcr.io/charlsk8/gateway:v1.0.0
    ```
    

## Generar imagen local

- **Construir imagen**

    ```sh
    podman build -t APIGateway -f Containerfile . 
    ```

- **Correr el contenedor**

    ```sh
    podman run --rm --name reactivo --network=red-bank -p 9092:9092 -d APIGateway
    ```

## Componentes Principales


### 1. AuthenticationFilterFactory

Ubicación: `src/main/java/com/banco/gateway/component/AuthenticationFilterFactory.java`

Este filtro de seguridad valida los tokens JWT en las solicitudes entrantes y los gestiona con una caché en memoria para mejorar el rendimiento.

### 2. MessageConsumer

Ubicación: `src/main/java/com/banco/gateway/component/MessageConsumer.java`

Este componente escucha eventos de autenticación, como cierre de sesión, a través de ActiveMQ y actualiza la caché de tokens en consecuencia.

### 3. TokenCacheServiceImpl

Ubicación: `src/main/java/com/banco/gateway/service/impl/TokenCacheServiceImpl.java`

Este servicio administra la caché de tokens utilizando Caffeine para almacenar tokens válidos y evitar verificaciones innecesarias con JWT.


## Configuración de Rutas

Ubicación: src/main/resources/application.yml

El API Gateway enruta las solicitudes a diferentes microservicios y aplica el filtro de autenticación donde es necesario.


## Rutas Configuradas

- Auth Service (/api/v1/auth/**): Sin autenticación requerida.
- Usuario Service (/api/v1/usuario/**): Requiere autenticación JWT.
- Transactions Service (/api/v1/transacciones/**, /api/v1/auditoria/**): Requiere autenticación JWT.
- Cuenta Bancaria Service (/api/v1/cuenta-bancaria/**, /api/v1/movimiento/**): Requiere autenticación JWT.
  

### Tecnologías Utilizadas

- **Java 17**: Lenguaje de programación principal utilizado para desarrollar la aplicación.
- **Spring Boot**: Framework utilizado para crear aplicaciones basadas en Spring de manera rápida y sencilla.
- **JWT**: Implementación de autenticación y autorización segura mediante tokens JWT.
- **ActiveMQ**: Broker de mensajería utilizado para la comunicación asincrónica entre servicios.
- **Lombok**: Herramienta que reduce el código boilerplate mediante anotaciones.
- **Spring Cloud Gateway**: Framework de Spring para construir API Gateways escalables y flexibles, permitiendo enrutamiento inteligente y filtrado de solicitudes.
- **Caffeine**: Biblioteca de caché de alto rendimiento para Java, que proporciona almacenamiento en memoria con estrategias eficientes de expiración y eliminación.
- **Podman**: Herramienta para la gestión de contenedores sin necesidad de un demonio en segundo plano, compatible con Docker y enfocada en la seguridad.


## Diagrama de Componentes

![image](https://github.com/user-attachments/assets/5b3d4176-34dc-4b47-b505-0ec0c89c3a54)


