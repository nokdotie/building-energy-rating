import zio.{Console, ZIO}
import zio.stream.{ZPipeline, ZSink}
import java.io.{File, IOException}
import org.mongodb.scala._
import org.mongodb.scala.model._
import scala.util.chaining.scalaUtilChainingOps

trait Store {
  val upsert: ZPipeline[Any, Throwable, Int, Unit]
}

object Store {
  val upsert: ZIO[Store, Nothing, ZPipeline[Any, Throwable, Int, Unit]] =
    ZIO.serviceWith[Store] { _.upsert }
}

object FileStore extends Store {
  val upsert: ZPipeline[Any, Throwable, Int, Unit] = {
    val file = File.createTempFile("certificate-numbers-", ".txt")

    ZPipeline.fromSink {
      ZSink
        .fromFile(file)
        .contramapChunks { certificateNumbers =>
          certificateNumbers.flatMap { certificateNumber =>
            s"$certificateNumber\n".getBytes
          }
        }
        .summarized {
          Console.printLine(s"Upsert in ${file.getAbsolutePath}")
        } { (_, _) => () }
        .map { _ => () }
        .ignoreLeftover
    }
  }
}

object MongoStore extends Store {
  val upsert: ZPipeline[Any, Throwable, Int, Unit] = {
    val client = MongoClient("mongodb://localhost:27017")
    val database = client.getDatabase("building-energy-rating")
    val collection = database.getCollection("certificates")

    collection.createIndex(
      Indexes.hashed("certificate_number"),
      IndexOptions().unique(true)
    )

    ZPipeline[Int].chunks
      .mapZIO { chunk =>
        val inserts = chunk
          .map { a => Document("certificate_number" -> a) }
          .map { InsertOneModel(_) }

        ZIO.fromFuture { implicit ec =>
          collection
            .bulkWrite(inserts, BulkWriteOptions().ordered(false))
            .toFuture()
        }
      }
      .map { _ => () }
  }
}
