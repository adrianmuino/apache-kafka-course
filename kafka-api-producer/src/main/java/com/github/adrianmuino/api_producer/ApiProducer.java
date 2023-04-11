package com.github.adrianmuino.api_producer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

public class ApiProducer
{
    Logger logger = LoggerFactory.getLogger(ApiProducer.class.getName());

    public ApiProducer(){}
    public static void main( String[] args )
    {
        try {
          new ApiProducer().run();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    public void run() throws IOException{

        logger.info("Setup");

        // /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        // BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);
        
        // create an API connection
        HttpURLConnection conn;
        conn = createApiConnection();

        // create producer properties
        Properties properties = createKafkaProperties();

        // safe producer properties
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // same as acks=-1
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); // kafka 2.0 >= 1.1 so we can keep this as 5. Use 1 otherwise.        

        // create a kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          logger.info("stopping application...");
          logger.info("shutting down client from twitter...");
          conn.disconnect();
          logger.info("closing producer...");
          producer.close();
          logger.info("done!");
        }));

        // loop to send msgs to kafka
        // on a different thread, or multiple different threads....

        InputStream inputStream = conn.getInputStream();
    
        String msg = convertStreamToString(inputStream);
        logger.info(msg);

        // send data to kafka - asynchronously
        producer.send(new ProducerRecord<String,String>("api_msgs", null, msg), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e != null) {
                    logger.error("Error while producing.\n", e);
                }
            }
        });

        logger.info("End of application");
    }
    
    private static String convertStreamToString (InputStream inputStream) {
      try (Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
        return scanner.hasNext() ? scanner.next() : "";
      }
    }

    public HttpURLConnection createApiConnection() throws IOException {
        URL url = new URL("https://jsonplaceholder.typicode.com/posts/1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Attempts to establish a connection.
        conn.connect();

        return conn;
    }
    
      public Properties createKafkaProperties() {
        String bootStrapServers = "ubuntu-vm:9092";

        // create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
      }
}