package com.ebiznext.kafka.consumer

import java.util

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerRebalanceListener, KafkaConsumer, OffsetAndMetadata, OffsetCommitCallback}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException

import scala.collection.JavaConversions._
import scala.util.{Failure, Try}

/**
  * Created by smanciot on 21/07/17.
  */
trait ConsumerService[K,V] extends LazyLogging{
  def consumer: KafkaConsumer[K, V]
  def topics: List[String]

  def handler: ConsumerHandler[V]

  def timeout = 100L

  def every = 100L

  private var count = 0

  private var currentOffsets = Map[TopicPartition, OffsetAndMetadata]()

  private class ConsumerRebalanceHandler extends ConsumerRebalanceListener {
    override def onPartitionsAssigned(partitions: util.Collection[TopicPartition]): Unit = {
      for(partition <- currentOffsets.keys){
        if(!partitions.contains(partition)){
          logger.info(s"removing partition ${partition.topic()}-${partition.partition()} from current offsets")
          currentOffsets -= partition
        }
      }
    }

    override def onPartitionsRevoked(partitions: util.Collection[TopicPartition]): Unit = {
      logger.warn("Lost partitions in rebalance. Committing current offsets:" + currentOffsets)
      Try(consumer.commitSync(currentOffsets)) match {
        case Failure(exception) => logger.error(s"Commit failed for offsets $currentOffsets", exception)
        case _ => // nothing to do
      }
    }
  }

  val offsetCommitCallback = new OffsetCommitCallback {
    override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit = {
      Option(exception) match {
        case Some(_) => logger.error(s"Commit failed for offsets $offsets", exception)
        case None =>
      }
    }
  }

  def run(): Unit = {
    sys.ShutdownHookThread{
      consumer.wakeup()
    }
    try{
      consumer.subscribe(topics, new ConsumerRebalanceHandler)
      while(true){
        val records = consumer.poll(timeout)
        for(record <- records){
          handler.processMessage(record.value())
          currentOffsets += new TopicPartition(record.topic(), record.partition()) ->
            new OffsetAndMetadata(record.offset()+1, "no metadata")
          count += 1
          if(count % every == 0){
            consumer.commitAsync(currentOffsets, offsetCommitCallback)
          }
        }
        consumer.commitAsync(currentOffsets, offsetCommitCallback)
      }
    }
    catch {
      case w: WakeupException => //nothing to do
      case e: Exception => logger.error("Unexpected error", e)
    }
    finally {
      try{
        logger.info("About to close consumer. Committing current offsets:" + currentOffsets)
        consumer.commitSync(currentOffsets)
      }
      finally {
        consumer.close()
      }
    }
  }
}

trait ConsumerHandler[T] {
  def processMessage(message: T): Boolean
}