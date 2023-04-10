package cz.micro

//https://twitter.github.io/finatra/user-guide/index.html
//https://polygon.io/docs/options/getting-started


import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.AccessLoggingFilter
import com.twitter.finatra.http.filters.ExceptionMappingFilter
import com.twitter.finatra.http.filters.HttpNackFilter
import com.twitter.finatra.http.filters.HttpResponseFilter
import com.twitter.finatra.http.filters.LoggingMDCFilter
import com.twitter.finatra.http.filters.StatsFilter
import com.twitter.finatra.http.filters.TraceIdMDCFilter
import com.twitter.finatra.http.routing.HttpRouter

object HelloWorldServerMain extends HelloWorldServer

class HelloWorldServer extends HttpServer {

  override def defaultHttpPort: String = ":8888"

  override def defaultHttpServerName: String = super.defaultHttpServerName

  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[StatsFilter[Request]]
      .filter[AccessLoggingFilter[Request]]
      .filter[HttpResponseFilter[Request]]
      .filter[ExceptionMappingFilter[Request]]
      .filter[HttpNackFilter[Request]]
      .add[HelloWorldController]

  }
}