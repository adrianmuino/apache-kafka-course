Offset reset behavior and settings:

- Consumer reads from a log continously at a specific offset.
- Offset depends on consumer group
- Kafka log retention is 7 days (v >= 2.0) or 1 day (v < 2.0)
- If your consumer is down for more than 7 days, your offsets are "invalid"
  - Consumer can be down for many reasons, including a bug in the app
- After log retention period offset can be lost

`auto.offset.reset` (default `latest`):
- `latest` : consumer will read from the end of the log
- `earliest` : consumer will read from the start of the log
- `none` : consumer will throw exception if no offset is found

`offset.retention.minutes` (default `10080` = 7 days):
- Controls how long Kafka will remember offsets in the special topic

`log.retention.minutes` (default `10080` = 7 days):
- Controls how long Kafka will keep a log file before deleting it

To replay data for a consumer group:
- Take all consumers from a specific group and use `kafka-consumer-groups` to set the offset
- Restart the consumers

**Important**
- Set proper data retention period & offset retention period
- Ensure the auto offset reset behavior is the one you expect / want
- Use replay capability in case of unexpected behavior