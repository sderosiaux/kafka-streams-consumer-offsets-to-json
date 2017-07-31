package com.ctheu

import kafka.common.OffsetAndMetadata
import kafka.coordinator.group.OffsetKey
import play.api.libs.json.{Json, OWrites}

object ConsumerOffsetDetails {
  implicit val detailsJson: OWrites[ConsumerOffsetDetails] = Json.writes[ConsumerOffsetDetails]

  def apply(k: OffsetKey, value: Option[OffsetAndMetadata]): ConsumerOffsetDetails = {
    ConsumerOffsetDetails(
      k.key.topicPartition.topic(),
      k.key.topicPartition.partition(),
      k.key.group,
      k.version,
      value.map(_.offset),
      value.map(_.metadata),
      value.map(_.commitTimestamp),
      value.map(_.expireTimestamp))
  }
}

case class ConsumerOffsetDetails(
                    topic: String,
                    partition: Int,
                    group: String,
                    version: Int,
                    offset: Option[Long],
                    metadata: Option[String],
                    commitTimestamp: Option[Long],
                    expireTimestamp: Option[Long]
                  )
