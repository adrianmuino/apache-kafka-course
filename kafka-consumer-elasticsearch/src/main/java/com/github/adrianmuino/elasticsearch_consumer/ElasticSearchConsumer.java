package com.github.adrianmuino.elasticsearch_consumer;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchConsumer
{
    public static RestHighLevelClient createClient(){
      // credentials
      String hostname = System.getenv("ELASTIC_HOST");
      String username = System.getenv("ELASTIC_USER");
      String password = System.getenv("ELASTIC_PASS");

      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

      RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 443, "https")).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
        @Override
          public HttpAsyncClientBuilder customizeHttpClient (HttpAsyncClientBuilder httpClientBuilder){
            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
          }
      });

      RestHighLevelClient client = new RestHighLevelClient(builder);
      return client;
    }

    public static KafkaConsumer<String, String> createConsumer(String topic){
      String bootStrapServers = "ubuntu-vm:9092";
      String groupId = "elasticsearch-application";

      // create consumer properties
      Properties properties = new Properties();
      properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
      properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
      properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // "latest" - unread msgs only

      // create the consumer
      KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

      // subscribe consumer to our topic(s)
      consumer.subscribe(Arrays.asList(topic));

      return consumer;
    }
    public static void main( String[] args ) throws IOException
    {
        Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
        RestHighLevelClient client = createClient();

        KafkaConsumer<String, String> consumer = createConsumer("api_msgs");

        // poll for new data
        while(true) {
          ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
          for (ConsumerRecord<String, String> record : records) {
            String jsonString = record.value();

            if(jsonString == null || jsonString.trim().isEmpty()){
              continue;
            }

            IndexRequest indexRequest = new IndexRequest(
              "producer",
              "messages"
            ).source(jsonString, XContentType.JSON);
    
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            String id = indexResponse.getId();
            logger.info(id);
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
      }

        // // close the client gracefully
        // client.close();
    }
}