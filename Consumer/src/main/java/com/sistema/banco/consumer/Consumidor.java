package com.sistema.banco.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.sistema.banco.modelos.Transaccion;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Consumidor {

	private static final String[] BANK_QUEUES = {"BANRURAL", "GYT", "BAC", "BI"};
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setUsername("David");
        factory.setPassword("TeretaKun");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.basicQos(1);

        for (String queue : BANK_QUEUES) {
            channel.queueDeclare(queue, true, false, false, null);
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);
                try {
                    Transaccion tx = mapper.readValue(payload, Transaccion.class);
                    String codigoUnico = UUID.randomUUID().toString().substring(0, 8);
                    tx.idTransaccion = "TX-" + tx.idTransaccion + "-" + codigoUnico + "-Mario";
                    tx.carnet = "0905-15-14297"; 
                    tx.nombre = "Mario David Tereta Sapalun"; 
                    tx.correo = "mteretas@miumg.edu.gt";
                    
                    String jsonModificado = mapper.writeValueAsString(tx);
                    
                    boolean enviado = postToApi(jsonModificado);

                    if (!enviado) {
                        System.out.println("Primer intento falló. Reintentando envío...");
                        enviado = postToApi(jsonModificado);
                    }

                    if (enviado) {
                    	System.out.println("Transacción procesada correctamente");
                    	System.out.println("ID: " + tx.idTransaccion);
                    	System.out.println("POST enviado a API");
                    	System.out.println("ACK enviado a RabbitMQ");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } else {
                        System.err.println("[ERROR] No se pudo enviar al POST después del reintento. El mensaje queda en RabbitMQ.");
                    }
                } catch (Exception ex) {
                    System.err.println("Error, procesando . . . : " + ex.getMessage());
                }
            };

            channel.basicConsume(queue, false, deliverCallback, consumerTag -> {});
        }
        System.out.println("Consumidor esperando mensajes...");
    }

    private static boolean postToApi(String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Respuesta del servidor: " + response.statusCode() + " -> " + response.body());
            
            return response.statusCode() == 200 || response.statusCode() == 201;
        } catch (Exception e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    } 
}
