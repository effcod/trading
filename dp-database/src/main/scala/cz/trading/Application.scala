package cz.micro

import io.grpc.{Server, ServerBuilder, ServerServiceDefinition}
import cz.micro.proto.dp.stock._
import io.grpc.stub.StreamObserver
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.{ExecutionContext, Future}
import scala.runtime.LazyLong
//https://pbehre.in/posts/grpc-scala-server  => https://github.com/pavitra14/grpc_hello_server
//https://github.com/xuwei-k/grpc-scala-sample
//https://www.beyondthelines.net/computing/grpc-in-scala/ => https://github.com/btlines/grpcexample

/**
 StockServer
 */
object Application extends LazyLogging  {
  private val port = 50051
  private val StockService: ServerServiceDefinition = StockStreamingGrpc.bindService(new StockStreamService, ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val server = new SimpleServer(port, StockService)
    server.start()
    server.blockUntilShutdown()
  }
}

class SimpleServer(port: Int, service: ServerServiceDefinition) { self =>
  lazy private val server: Server = init()

  private def init(): Server = {
    ServerBuilder
      .forPort(port)
      .addService(service)
      .build
  }

  def start(): Unit = {
    server.start()

    println("Server started, listening on " + port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }
  def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }
}

class StockStreamService extends StockStreamingGrpc.StockStreaming {
  val r = scala.util.Random

  override def streamStock(responseObserver: _root_.io.grpc.stub.StreamObserver[StockReply]): _root_.io.grpc.stub.StreamObserver[StockRequest] = {
    new StreamObserver[StockRequest] {
      override def onNext(value: StockRequest): Unit = {
        println("onNext:Received a Request: " + value)

        val stockData = value.symbols.map { symbol =>
          StockData(symbol = symbol, dateTime = value.from, high = r.nextDouble() * 100, low = r.nextDouble()*100, volume = r.nextInt(10000))
        }

        responseObserver.onNext(
          StockReply(data = stockData)
        )
      }

      override def onError(t: Throwable): Unit = {
        println("onError")
        println(t)
      }

      override def onCompleted(): Unit = {
        println("onCompleted")
      }
    }
  }

  override def singleStock(request: StockRequest): Future[StockReply] = {
    null
  }
}

