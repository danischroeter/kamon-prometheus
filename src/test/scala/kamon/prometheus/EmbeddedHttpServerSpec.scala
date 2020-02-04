package kamon.prometheus

import java.net.URL

import com.typesafe.config.ConfigFactory
import kamon.Kamon
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class SunHttpServerSpecSuite extends EmbeddedHttpServerSpecSuite {
  override def createTestee(): PrometheusReporter = PrometheusReporter.create()
}
class NanoHttpServerSpecSuite extends EmbeddedHttpServerSpecSuite {
  val config = ConfigFactory.parseString("kamon.prometheus.embedded-server-impl=nano").withFallback(ConfigFactory.load())
  override def createTestee(): PrometheusReporter = new PrometheusReporter(initialConfig = config)
}

abstract class EmbeddedHttpServerSpecSuite extends WordSpec with Matchers with BeforeAndAfterAll with KamonTestSnapshotSupport {
  protected def createTestee(): PrometheusReporter

  private var testee: PrometheusReporter = _

  override def beforeAll(): Unit = testee = createTestee()

  override def afterAll(): Unit = testee.stop()

  "the embedded sun http server" should {
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