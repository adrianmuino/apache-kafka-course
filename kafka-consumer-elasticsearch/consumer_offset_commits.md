Two common patterns for committing offsets in a kafka consumer app:

`enable.auto.commit = true` (default):
- Easy
- Synchronous processing of batches
- Offsets will be committed automatically at the regular commit interval every time you call `poll()`
- `auto.commit.interval.ms=5000` by default
- If you don't use synchronous processing (asynch), you will have "at-most-once" behavior because offsets will be committed before your data is processed.

```java
while(true) {
    List<Records> records = consumer.poll(Duration.ofMillis(100));
    doSomethingSynchronous(batch);
}
```

`enable.auto.commit = false`:
- Medium
- Manual commits of offsets
- Asynchronous processing of batches
- You control when to commit offsets and what's the condition to commit them
- _Ex:_ accumulating records into a buffer and then flushing the buffer to a DB + committing offsets
```java
while(true) {
    batch += consumer.poll(Duration.ofMillis(100));
    if isReady(batch) {
        doSomethingSynchronous(batch);
        consumer.commitSync();
    }
}
```