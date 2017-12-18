package kcl

import java.net.InetAddress
import java.util.UUID
import com.amazonaws.auth.{AWSCredentials, AWSCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{InitialPositionInStream, KinesisClientLibConfiguration}
import utils.Global._


object KclCfg {

  import KinesisCredentials._

  object KinesisCredentials {
    val awsAccessKey = appCfg.getString("kinesis.aws.access-key")
    val awsSecretKey = appCfg.getString("kinesis.aws.secret-key")
    val appName = appCfg.getString("kinesis.app.name")

    final class BasicAWSCredentialsProvider(basic: BasicAWSCredentials) extends AWSCredentialsProvider {
      @Override def getCredentials: AWSCredentials = basic

      @Override def refresh = {}
    }

  }

  val likeStream =
    appCfg.getString("kinesis.streams.like.name")

  val idelTimeBetweenReads = ((System.currentTimeMillis % 7) + 1
  ) * appCfg.getInt("kinesis.streams.like.time.interval.factor")
  
  println(s"idletimeBetweenReads = ${idelTimeBetweenReads} ")

  val endoKclworkerId =
    s"${InetAddress.getLocalHost.getCanonicalHostName}:${likeStream}:${UUID.randomUUID.toString}"

  val streamClientMap: Map[String, KinesisClientLibConfiguration] =

    Map(likeStream -> new KinesisClientLibConfiguration(
      s"${appName}-${likeStream}",
      likeStream,
      new BasicAWSCredentialsProvider(
        new BasicAWSCredentials(awsAccessKey, awsSecretKey)),
      endoKclworkerId).withInitialPositionInStream(InitialPositionInStream.LATEST)
      .withIdleTimeBetweenReadsInMillis(idelTimeBetweenReads)
      .withInitialLeaseTableWriteCapacity(200)
      .withInitialLeaseTableReadCapacity(200))
}
