package cz.micro

import com.typesafe.scalalogging.LazyLogging
import cz.micro.proto.dp.stock._
import cz.micro.proto.timeframe.{TimeFrame, TimeFrameUnit}
import io.grpc.stub.StreamObserver
import io.grpc.{CallOptions, ManagedChannel, ManagedChannelBuilder}
/*


// simple strategy history/normal runner
object Application {
  def main() = {
    val stocks = new HistoryStock(start, stop, symbols)
    val options = new HistoryOption(start, stop, symbols, ?call/put, ..? )
    val dividends = new Dividends(start, stop, symbols)
    val openTrades = new OpenTradeProvider()
    val trader = new Trader()


    Using(new SimpleHistoryRunner(trader)) { shr =>
      shr.registerOptionProvider(options)
        .registerStockProvider(stocks)
        .registerDividendProvider(stocks)
        .registerOpenTrade(openTrades)

      shr.run(new SimpleStrategy(conf)) // AND? bezet vice strategii najednou?
    } //.close()
  }
}

class SimpleStrategy extends StrategyBase with LazyLogging {
  def getSignal() = {

  }
}

class SimpleHistoryRunner extends StrategyRunner{


}

class SimpleRealTimeRunner extends StrategyRunner {
}


 */
object Application extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val port = 50051
    val host = "localhost"

    val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build()

    val stub = StockStreamingGrpc.StockStreamingStub.newStub(channel, CallOptions.DEFAULT)
    val so = new StreamObserver[StockReply]() {
      override def onNext(value: StockReply): Unit = {
        println(s"Received reply ${value.data.mkString(",")} " + value)
      }

      override def onError(t: Throwable): Unit = {
        println(t)
      }

      override def onCompleted(): Unit = {
        println("client completed")
      }
    }
    val observer = stub.streamStock(so)

    Thread.sleep(3000)
    val listRequests = List(
      StockRequest(symbols = List("MCD", "KO", "IBM"), timeFrame = Some(TimeFrame(value = 1, unit = TimeFrameUnit.minute))),
      StockRequest(symbols = List("KO"), timeFrame = Some(TimeFrame(value = 2, unit = TimeFrameUnit.minute))),
      StockRequest(symbols = List("IBM"), timeFrame = Some(TimeFrame(value = 3, unit = TimeFrameUnit.minute))),
      StockRequest(symbols = List("AAPL"), timeFrame = Some(TimeFrame(value = 4, unit = TimeFrameUnit.minute))),
    )

    try {
      listRequests.foreach { req =>
        println("send request " + req + " and sleep")
        observer.onNext(req)
        Thread.sleep(5000)
      }
    } finally {
      observer.onCompleted()
      channel.shutdown()
    }
  }
}