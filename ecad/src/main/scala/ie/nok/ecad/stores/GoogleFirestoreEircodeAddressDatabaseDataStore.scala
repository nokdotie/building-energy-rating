package ie.nok.ecad.stores

import com.google.cloud.firestore._
import ie.nok.ecad._
import ie.nok.ecad.store.GoogleFirestoreEircodeAddressDatabaseDataCodec
import ie.nok.gcp.firestore.{CollectionPath, DocumentPath, Firestore}
import java.util.Base64
import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps
import zio._
import zio.stream.{ZPipeline, ZStream}

class GoogleFirestoreEircodeAddressDatabaseDataStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends EircodeAddressDatabaseDataStore {

  def upsertBatch(
      ecad: Iterable[EircodeAddressDatabaseData]
  ): ZIO[Any, Throwable, Int] =
    for {
      collectionReference <- firestore.collection(collectionPath)
      documentReferences <- ecad
        .map { ecadData =>
          firestore
            .document(
              collectionReference,
              DocumentPath(ecadData.eircode.value)
            )
            .map { (ecadData, _) }
        }
        .pipe { ZIO.collectAll }
      writeBatch <- firestore.batch
      _ = documentReferences.foreach { (ecadData, documentReference) =>
        writeBatch.set(
          documentReference,
          GoogleFirestoreEircodeAddressDatabaseDataCodec.encode(
            ecadData
          ),
          SetOptions.merge
        )
      }
      results <- firestore.commit(writeBatch)
    } yield results.size

  def getByEircode(
      eircode: Eircode
  ): ZIO[Any, Throwable, Option[EircodeAddressDatabaseData]] =
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.document(eircode.value.toString)
        ZIO.fromFutureJava { query.get() }
      }
      .map { snapshot => Option(snapshot.getData) }
      .flatMap {
        case None => ZIO.none
        case Some(data) =>
          GoogleFirestoreEircodeAddressDatabaseDataCodec
            .decode(data)
            .pipe { ZIO.fromTry }
            .map { Option(_) }
      }
}

object GoogleFirestoreEircodeAddressDatabaseDataStore {
  val layer: ZLayer[
    Firestore.Service,
    SecurityException,
    GoogleFirestoreEircodeAddressDatabaseDataStore
  ] =
    ZLayer {
      for {
        firestore <- ZIO.service[Firestore.Service]
        collectionPath <- System
          .env("ENV")
          .map {
            case Some("production") => "eircode-address-database-data"
            case _                  => "eircode-address-database-data-dev"
          }
          .map { CollectionPath.apply }
      } yield GoogleFirestoreEircodeAddressDatabaseDataStore(
        firestore,
        collectionPath
      )
    }
}
