Change these settings only if your consumer maxes out on throughtput.

`fetch.min.bytes` (default 1):
- Controls how much data you want to pull at least on each request
- Helps improving throughput and decreasing request number
- At the cost of latency

`max.poll.records` (default 500):
- Controls how many records to receive per poll request
- Increase if your messages are very small and have a lot of available RAM

`max.partitions.fetch.bytes` (default 1MB):
- Maximum data returned by the broker per partition
- If you read from 100 partitions, you'll need a lot of memory (RAM)

`fetch.max.bytes` (default 50MB):
- Maximum data returned for each fetch request (covers multiple partitions)
- The consumer performs multiple fetches in parallel