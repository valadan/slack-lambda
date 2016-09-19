package io.valadan.slacklambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.lambda.runtime.events.S3Event
import java.nio.ByteBuffer
import java.util.Base64
import com.amazonaws.services.lambda.runtime.{Context, LambdaLogger}
import slack.api.BlockingSlackApiClient 
import java.io.{OutputStream, InputStream}
import java.time._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.JavaConverters._

/**
 * @author valadan
 */
trait AWSLambdaHandler {
  import AWSLambdaHandler._
  private lazy val kms = new AWSKMSClient()
 
  protected lazy val token = decryptToken(kms, encryptedToken)
  
  def handle(event: S3Event, context: Context): Unit = {
    internalHandler(event, context)
  }
  
  protected def internalHandler(event: S3Event, context: Context): Unit

}

object AWSLambdaHandler {
 // encrypted Slack api token
  private val encryptedToken =
    "AQECAHgSMj86r7T9RX8aM/WclNofw1cHLQTJfIBztHpR8PuYLQAAAJQwgZEGCSqGSIb3DQEHBqCBgzCBgAIBADB7BgkqhkiG9w0BBwEwHgYJYIZIAWUDBAEuMBEEDPvkn5CQEyMx7SVnZAIBEIBOLFka1SFVXmcADBkbAMGEkafxvnYKAoH4SJOrZEpHMjlET9thcseS8V2dwIMGxAWCAQbOVC4G3FH1pbDeLGaGGXJxJcYGOqvxZxvdJwcu"
  private def decryptToken(kms: AWSKMSClient, encryptedToken: String): String = {
    val encryptedBytes = ByteBuffer.wrap(Base64.getDecoder.decode(encryptedToken))
    val decryptedKeyBytes = kms.decrypt(new DecryptRequest().withCiphertextBlob(encryptedBytes)).getPlaintext
    new String(decryptedKeyBytes.array())
  }
}

object SlackLambdaHandler extends AWSLambdaHandler {

  def internalHandler(event: S3Event, context: Context): Unit = {
    val client = BlockingSlackApiClient(token)
    val record = event.getRecords.asScala.headOption
    record.map { notification =>
      val s3Entity = notification.getS3
      val bucket = s3Entity.getBucket.getName
      val size = Option(s3Entity.getObject.getSizeAsLong).map(sz => s"size:"+sz).getOrElse("")
      val key = s3Entity.getObject.getKey
      val event = notification.getEventName
      val result = client.postChatMessage("#ecw", s"$event completed for $bucket/$key $size!", Some("narendra"), Some(false), None, None, None, None, None, None, None)
    }

  }

  
}