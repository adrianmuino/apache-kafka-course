# Kafka Sample Project

Start zookeeper
```bash
zookeeper-server-start.sh kafka_2.13-3.4.0/config/zookeeper.properties
```

Start kafka server
```bash
kafka-server-start.sh kafka_2.13-3.4.0/config/server.properties
```

Start console consumer
```bash
kafka-console-consumer.sh --bootstrap-server ubuntu-vm:9092 --topic first_topic --group my-first-application
```

Start console producer
```bash
kafka-console-producer.sh --bootstrap-server ubuntu-vm:9092 --topic first_topic --producer.config kafka_2.13-3.4.0/config/producer.properties
```