# Use case

Read the famous internal topic `__consumer_offsets` with any projects that can only read JSON topics, such as timeseries databases, to enable some monitoring and alerting.

Details on the blog post: https://www.ctheu.com/2017/08/07/looking-at-kafka-s-consumers-offsets/

# How to run it

Clone the project, and `sbt run`, or via Intellij IDEA, run the `Main` class.

You can also build it using `sbt universal:packageBin` and unzip `target\universal\kafka-consumer-offsets-to-json-topic-1.0.zip`.

# Dependencies

It only depends on Kafka Streams 0.11 (it can be setup to 0.10 if needed).

It is using some classes from Kafka server to know how to parse the content of `__consumer_offsets` which is binary-based.

# Notes

Kafka is regularly commiting messages to `__consumer_offsets`, even if no-one is consuming.

By running this Kafka Streams project, and listening to the output topic, you'll get something like :

```
$ kafka-console-consumer.bat --topic __consumer_offsets_json --new-consumer --bootstrap-server localhost:9092
{"topic":"WordsWithCountsTopic","partition":0,"group":"console-consumer-26549","version":1,"offset":95,"metadata":"","commitTimestamp":1501542796444,"expireTimestamp":1501629196444}
{"topic":"WordsWithCountsTopic","partition":0,"group":"console-consumer-26549","version":1,"offset":95,"metadata":"","commitTimestamp":1501542801444,"expireTimestamp":1501629201444}
{"topic":"WordsWithCountsTopic","partition":0,"group":"console-consumer-26549","version":1,"offset":95,"metadata":"","commitTimestamp":1501542806444,"expireTimestamp":1501629206444}
{"topic":"WordsWithCountsTopic","partition":0,"group":"console-consumer-26549","version":1,"offset":95,"metadata":"","commitTimestamp":1501542811445,"expireTimestamp":1501629211445}
{"topic":"WordsWithCountsTopic","partition":0,"group":"console-consumer-26549","version":1,"offset":95,"metadata":"","commitTimestamp":1501542816447,"expireTimestamp":1501629216447}
{"topic":"__consumer_offsets","partition":5,"group":"consumer-offsets-consumer-app-2","version":1,"offset":106,"metadata":"","commitTimestamp":1501542868586,"expireTimestamp":1501629268586}
{"topic":"__consumer_offsets","partition":17,"group":"consumer-offsets-consumer-app-2","version":1,"offset":1025,"metadata":"","commitTimestamp":1501542868736,"expireTimestamp":1501629268736}
```

The output topic offsets are not published to not cause an infinite loop (listening to it would cause messages published to this same topic, that would move on the offsets, that would cause new messages to pop etc.).

# Warning

It's probably not a good idea to rely on this in production, because it's an implementation detail that can changed across versions.

# Options

The broker address and the output topic can be customized.

Defaults are:
- `localhost:9092`
- `__consumer_offsets_json`


```
  -b, --broker  <arg>
  -o, --output-topic  <arg>
      --help                  Show help message
```

