package com.sistema.banco.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.sistema.banco.modelos.LoteTransacciones;
import com.sistema.banco.modelos.Transaccion;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Productor {
	public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("127.0.0.1");
        factory.setUsername("David");
        factory.setPassword("TeretaKun");

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            System.out.println("Producer iniciado. Conectado a RabbitMQ como Mario.");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Error al obtener transacciones. Código HTTP: " + response.statusCode());
                return;
            }

            System.out.println("GET exitoso. Procesando lote de transacciones...");

            LoteTransacciones lote = mapper.readValue(response.body(), LoteTransacciones.class);

            for (Transaccion tx : lote.transacciones) {
                String bank = tx.bancoDestino.toUpperCase().trim();
                
                channel.queueDeclare(bank, true, false, false, null);

                String payload = mapper.writeValueAsString(tx);
                
                channel.basicPublish("", bank, MessageProperties.PERSISTENT_TEXT_PLAIN, payload.getBytes());

                System.out.println("--------------------------------");
                System.out.println("Transacción enviada");
                System.out.println("ID: " + tx.idTransaccion);
                System.out.println("Banco destino: " + bank);
                System.out.println("Cola RabbitMQ: " + bank);
                System.out.println("--------------------------------");
            }

            System.out.println("Proceso terminado. Lote enviado.");

        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

}
