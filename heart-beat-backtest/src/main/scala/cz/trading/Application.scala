package cz.micro

import com.google.protobuf.empty.Empty
import com.typesafe.scalalogging.LazyLogging
import cz.micro.proto.dp.heart_beat.{HeartBeatMessage, HeartBeatServiceGrpc, HeartBeatSubscribe, HeartBeatSubscribeReply, HeartBeatSubscription, HeartBeatUnsubscription}
import cz.micro.proto.dp.heart_beat.HeartBeatServiceGrpc.HeartBeatService
import io.grpc.stub.StreamObserver
import io.grpc.{Server, ServerBuilder, ServerServiceDefinition}

import scala.concurrent.{ExecutionContext, Future}

object Application extends LazyLogging  {
  private val port = 50051
  private val heartBeatService: ServerServiceDefinition = HeartBeatServiceGrpc.bindService(new HeartBeatServiceImpl, ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val server = new SimpleServer(port, heartBeatService)
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

class HeartBeatServiceImpl extends HeartBeatService {
  override def subscribe(request: HeartBeatSubscription, responseObserver: StreamObserver[HeartBeatMessage]): Unit = {
    responseObserver.onNext()
  }

  override def unsubscribe(request: HeartBeatUnsubscription): Future[HeartBeatUnsubscription] = ???
}
