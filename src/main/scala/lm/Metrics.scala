package lm

import java.util.concurrent.TimeUnit

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient
import com.blacklocus.metrics.CloudWatchReporterBuilder
import com.codahale.metrics.MetricRegistry
import net.liftweb.util.Props

object Metrics extends MetricRegistry {

  /**
    * Starts metric reporting to AWS Cloudwatch.
    */
  def report(): Unit = {
    val builder = new CloudWatchReporterBuilder()
    //If AWS credentials are specified, use them.  Otherwise we'll assume
    //that the credentials are implicitly available, probably because this
    //instance was launced with a profile that allows access (this is preferable
    //when we're actually using AWS)
    Props.get("aws.id").foreach { id =>
      val credentials = new BasicAWSCredentials(id, Props.get("aws.secret").openOrThrowException("An AWS secret is required when an AWS key ID is defined"))
      builder.withClient(new AmazonCloudWatchAsyncClient(credentials))
    }
    builder.withNamespace(Props.get("cloudwatch.namespace").openOrThrowException("A cloudwatch namespace is necessary for reporting cloudwatch metrics"))
        .withRegistry(this)
        //This will report all metrics both with and without the hostname
        //allowing them to be queried by host and in aggregate
        .withDimensions(s"host=${Props.hostName}*")
        .build()
        .start(1, TimeUnit.MINUTES)
  }

}
