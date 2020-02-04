package kamon.prometheus

import java.net.URL

import kamon.Kamon
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class EmbeddedHttpServerSpec extends WordSpec with Matchers with BeforeAndAfterAll with KamonTestSnapshotSupport {

  private val testee = PrometheusReporter.create()

  override def afterAll(): Unit = {
    testee.stop()
  }

  "the embedded http server" should {
    "provide no data comment on GET to /metrics when no data loaded yet" in {
      //act
      val metrics = httpGetMetrics()
      //assert
      metrics shouldBe "# The kamon-prometheus module didn't receive any data just yet.\n"
    }

    "provide the metrics on GET to /metrics with empty data" in {
      //arrange
      testee.reportPeriodSnapshot(emptyPeriodSnapshot)
      //act
      val metrics = httpGetMetrics()
      //assert
      metrics shouldBe ""
    }

    "provide the metrics on GET to /metrics with data" in {
      //arrange
      testee.reportPeriodSnapshot(counter("jvm.mem"))
      //act
      val metrics = httpGetMetrics()
      //assert
      metrics shouldBe "# TYPE jvm_mem_total counter\njvm_mem_total 1.0\n"
    }

    "provide the metrics on GET to /metrics with data after reconfigure" in {
      //arrange
      testee.reconfigure(Kamon.config)
      testee.reportPeriodSnapshot(counter("jvm.mem"))
      //act
      val metrics = httpGetMetrics()
      //assert
      metrics shouldBe "# TYPE jvm_mem_total counter\njvm_mem_total 2.0\n"
    }
  }


  private def httpGetMetrics(): String = {
    val src = scala.io.Source.fromURL(new URL("http://127.0.0.1:9095/metrics"))
    try
      src.mkString
    finally
      src.close()
  }

}
