package com.ctheu

import java.nio.ByteBuffer
import java.util.Properties

import kafka.coordinator.group.{BaseKey, GroupMetadataManager, OffsetKey}
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{KStreamBuilder, KeyValueMapper, Predicate}
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsConfig}
import org.rogach.scallop.ScallopConf
import play.api.libs.json.Json

object KStreamBuilder {

  val INPUT_TOPIC = "__consumer_offsets"

  val toJson: KeyValueMapper[OffsetKey, Array[Byte], KeyValue[Array[Byte], String]] =
    (k: OffsetKey, v: Array[Byte]) => {
      val value = Option(v).map(v => GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(v)))
      KeyValue.pair(null, Json.toJson(ConsumerOffsetDetails(k, value)).toString())
    }

  val baseKey: KeyValueMapper[Array[Byte], Array[Byte], KeyValue[BaseKey, Array[Byte]]] = (k: Array[Byte], v: Array[Byte]) =>
    KeyValue.pair(GroupMetadataManager.readMessageKey(ByteBuffer.wrap(k)), v)

  val offsetKey: KeyValueMapper[BaseKey, Array[Byte], KeyValue[OffsetKey, Array[Byte]]] = (key: BaseKey, value: Array[Byte]) =>
    KeyValue.pair(key.asInstanceOf[OffsetKey], value) // would love a collect() in the Java API :(

  val otherTopicsOnly: String => Predicate[BaseKey, Array[Byte]] = (outputTopic: String) => (key: BaseKey, _: Array[Byte]) => key match {
    // we ignore our own topic where we write otherwise we'll cause an infinite loop if we listen to it
    case k: OffsetKey => k.key.topicPartition.topic() != outputTopic
    case _ => false
  }

}

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val broker = opt[String](required = true, default = Some("localhost:9092"))
  val outputTopic = opt[String](required = true, default = Some("__consumer_offsets_json"))
  verify()
}

object Main extends App {
  import KStreamBuilder._
  val conf = new Conf(args)

  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "consumer-offsets-consumer-app")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, conf.broker())
  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass)
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray.getClass)
  props.put("exclude.internal.topics", "false") // necessary to consume __consumer_offsets

  val builder = new KStreamBuilder
  builder.stream[Array[Byte], Array[Byte]](INPUT_TOPIC)
    .map[BaseKey, Array[Byte]](baseKey)
    .filter(otherTopicsOnly(conf.outputTopic()))
    .map[OffsetKey, Array[Byte]](offsetKey)
    .map[Array[Byte], String](toJson)
    .to(Serdes.ByteArray, Serdes.String, conf.outputTopic())

  val streams = new KafkaStreams(builder, props)
  streams.start()

  sys.addShutdownHook(streams.close())
}
