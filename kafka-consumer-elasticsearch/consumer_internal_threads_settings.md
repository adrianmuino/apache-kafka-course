Two important consumer internal threads:

1. Poll thread
2. Heartbeat thread

`session.timeout.ms` (default 10 secs):
- Heartbeats are sent periodically from sonsumer to Consumer Coordinator broker
- If no heartbeat has been received from consumer and timeout occurs:
  - Consumer is considered dead
  - Consumer rebalancing happens
- Set lower timeouts if you want faster consumer rebalances
- Used to detect consumer application being down

`heartbeat.interval.ms` (default 3 secs):
- How often to send heartbeats
- Usually 1/3rd of session.timeout.ms
- Used to detect consumer application being down

`max.poll.interval.ms` (default 5 mins):
- Max time between two .poll() calls before declaring consumer dead
- Particularly important for Big Data frameworks like Spark in case processing takes time
- Set to higher if processing takes long
- Used to detect a data processing issue with the consumer
