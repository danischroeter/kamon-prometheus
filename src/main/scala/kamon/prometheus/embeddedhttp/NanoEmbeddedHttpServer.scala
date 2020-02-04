package kamon.prometheus.embeddedhttp

import com.typesafe.config.Config
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.{Response, newFixedLengthResponse}
import kamon.prometheus.{EmbeddedHttpServer, ScrapeSource}

class NanoEmbeddedHttpServer(hostname: String, port: Int, scrapeSource: ScrapeSource, config: Config) extends EmbeddedHttpServer(hostname, port, scrapeSource, config) {
  private val server = {
    val s = new NanoHTTPD(hostname, port) {
      override def serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response = {
        newFixedLengthResponse(Response.Status.OK, "text/plain; version=0.0.4; charset=utf-8", scrapeSource.scrapeData())
      }
    }
    s.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
    s
  }

  override def stop(): Unit = server.stop()
}
