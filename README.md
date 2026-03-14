# Programacion\_III\_Transacciones\_RabbitMQ-\_Java



\# Procesamiento de Transacciones Bancarias con RabbitMQ y Java



\## Descripción del Proyecto



Este proyecto implementa un sistema distribuido para el procesamiento de transacciones bancarias utilizando \*\*Java, Maven y RabbitMQ\*\*, aplicando el patrón \*\*Producer–Consumer\*\*.



El sistema consume un lote de transacciones desde una API externa, distribuye cada transacción a una cola de RabbitMQ según el banco destino y posteriormente un consumidor procesa dichas transacciones enviándolas a una API de almacenamiento.



El objetivo es demostrar una arquitectura desacoplada, resiliente y capaz de procesar grandes volúmenes de transacciones de forma eficiente.



\---



\# Arquitectura del Sistema



El sistema sigue el siguiente flujo:



API (GET /transacciones)

↓

Producer (Java)

↓

RabbitMQ (colas por banco)

↓

Consumer (Java)

↓

API (POST /transacciones)



\### Flujo de procesamiento



1\. El \*\*Producer\*\* realiza una solicitud GET para obtener un lote de transacciones.

2\. Cada transacción contiene el campo \*\*bancoDestino\*\*.

3\. El Producer publica cada transacción como mensaje JSON en una cola cuyo nombre corresponde al banco destino.

4\. RabbitMQ actúa como intermediario para desacoplar la generación y el procesamiento de transacciones.

5\. El \*\*Consumer\*\* escucha múltiples colas.

6\. Cuando recibe una transacción:



&#x20;  \* La convierte a objeto Java.

&#x20;  \* Agrega datos del estudiante (nombre, carnet y correo).

&#x20;  \* Envía la transacción a la API mediante POST.

7\. Si el POST es exitoso se envía un \*\*ACK manual\*\* a RabbitMQ confirmando el procesamiento del mensaje.



\---



\# Tecnologías Utilizadas



\* Java 17

\* Maven

\* RabbitMQ

\* Jackson (procesamiento de JSON)

\* Java HttpClient (consumo de APIs REST)



\---



\# Estructura del Proyecto



El sistema está dividido en dos aplicaciones independientes.



\## Producer



Encargado de:



\* Consumir la API GET

\* Procesar el lote de transacciones

\* Publicar cada transacción en RabbitMQ



Paquete principal:



com.sistema.banco.producer



Clases principales:



Productor.java

LoteTransacciones.java

Transaccion.java



\---



\## Consumer



Encargado de:



\* Escuchar múltiples colas

\* Procesar las transacciones

\* Enviar los datos a la API mediante POST



Paquete principal:



com.sistema.banco.consumer



Clases principales:



Consumidor.java

Transaccion.java



\---



\# Funcionamiento del Producer



El Producer realiza los siguientes pasos:



1\. Se conecta a RabbitMQ.

2\. Realiza una solicitud GET a la API de transacciones.

3\. Valida la respuesta del servidor.

4\. Convierte el JSON recibido a objetos Java usando Jackson.

5\. Recorre todas las transacciones del lote.

6\. Obtiene el banco destino de cada transacción.

7\. Crea dinámicamente una cola con el nombre del banco si no existe.

8\. Publica la transacción como mensaje persistente en RabbitMQ.



Cada mensaje enviado contiene la transacción completa en formato JSON.



\---



\# Funcionamiento del Consumer



El Consumer realiza las siguientes acciones:



1\. Se conecta a RabbitMQ.

2\. Escucha múltiples colas correspondientes a distintos bancos.

3\. Recibe cada mensaje en formato JSON.

4\. Convierte el JSON a objeto Java.

5\. Agrega información del estudiante:



&#x20;  \* nombre

&#x20;  \* carnet

&#x20;  \* correo

6\. Genera un identificador único para la transacción.

7\. Envía la transacción a la API mediante POST.



\---



\# ACK Manual



El sistema utiliza \*\*confirmación manual de mensajes (ACK)\*\*.



El ACK se envía únicamente cuando la API responde exitosamente.



Esto garantiza que \*\*no se pierdan transacciones\*\* en caso de error.



\---



\# Sistema de Reintento



Si el envío al POST falla, el consumidor realiza \*\*un reintento automático\*\* antes de confirmar el mensaje.



Si ambos intentos fallan:



\* El mensaje \*\*no se confirma\*\*

\* Permanece en RabbitMQ para ser procesado posteriormente.



Esto aumenta la confiabilidad del sistema.



\---



\# Manejo de Errores



El sistema incluye manejo básico de errores para:



\* Fallos de conexión con RabbitMQ

\* Fallos en la API externa

\* Errores en la deserialización de JSON

\* Fallos en el envío de POST



Los errores se registran mediante logs en consola.



\---



\# Ejecución del Sistema



\## 1. Iniciar RabbitMQ



RabbitMQ debe estar ejecutándose localmente.



Host:

127.0.0.1



\---



\## 2. Ejecutar Producer



El Producer realiza:



\* Consumo del GET

\* Creación de colas

\* Publicación de mensajes



\---



\## 3. Ejecutar Consumer



El Consumer:



\* Escucha las colas

\* Procesa cada mensaje

\* Envía la transacción al POST

\* Confirma el mensaje mediante ACK



\---



\# Ejemplo de Logs



Producer:



Producer iniciado.

GET exitoso.

Transacción enviada a cola BANRURAL.

Transacción enviada a cola BAC.



Consumer:



Mensaje recibido desde cola BANRURAL

Transacción enviada a API

Respuesta del servidor: 200

ACK enviado a RabbitMQ



\---



\# Características del Sistema



\* Arquitectura desacoplada

\* Colas dinámicas por banco

\* Mensajes persistentes

\* Confirmación manual de mensajes

\* Manejo básico de errores

\* Reintento automático de envío

\* Procesamiento distribuido



\---



\# Autor



Nombre: Mario David Tereta Sapalun

Carnet: 0905-15-14297



Curso: Programación III

Proyecto: Procesamiento de Transacciones con RabbitMQ

