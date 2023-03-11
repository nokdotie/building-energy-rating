package zio.gcp.firestore

import com.google.api.core.ApiFutureToListenableFuture
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.cloud.firestore
import com.google.cloud.firestore.{Firestore => _, _}
import scala.jdk.CollectionConverters._
import zio._
import zio.gcp.auth.GoogleCredentials

object Firestore {

  trait Service {
    def allCollections: Task[List[CollectionReference]]
    def allDocuments(
        collectionPath: CollectionPath
    ): Task[List[QueryDocumentSnapshot]]
    def batch: Task[WriteBatch]
    def collection(collectionPath: CollectionPath): Task[CollectionReference]
    def collectionGroup(collectionPath: CollectionPath): Task[Query]
    def commit(batch: WriteBatch): Task[List[WriteResult]]
    def createDocument[A](
        collectionRef: CollectionReference,
        collectionPath: DocumentPath,
        data: A
    ): Task[WriteResult]
    def delete(
        collectionPath: CollectionPath,
        documentPath: DocumentPath
    ): Task[WriteResult]
    def document(
        collectionRef: CollectionReference,
        documentPath: DocumentPath
    ): Task[DocumentReference]
    def documentSnapshot(
        collectionPath: CollectionPath,
        documentPath: DocumentPath
    ): Task[DocumentSnapshot]
    def set[A](
        collectionPath: CollectionPath,
        documentPath: DocumentPath,
        document: A
    ): Task[WriteResult]
    def subCollection(
        documentRef: DocumentReference,
        collectionPath: CollectionPath
    ): Task[CollectionReference]
  }

  def live: ZLayer[Scope, Throwable, Firestore] =
    ZLayer.fromZIO {
      val acquire: Task[firestore.Firestore] =
        GoogleCredentials.applicationDefault
          .flatMap { credentials =>
            ZIO.attempt {
              FirestoreOptions
                .getDefaultInstance()
                .toBuilder()
                .setCredentialsProvider(
                  FixedCredentialsProvider.create(credentials)
                )
                .build()
                .getService()
            }
          }

      val release = (firestore: com.google.cloud.firestore.Firestore) =>
        ZIO.attempt(firestore.close()).orDie

      ZIO.acquireRelease(acquire)(release).map { firestore =>
        new Service {
          def allCollections: Task[List[CollectionReference]] =
            ZIO.attempt(firestore.listCollections.asScala.toList)

          def allDocuments(
              collectionPath: CollectionPath
          ): Task[List[QueryDocumentSnapshot]] =
            ZIO
              .fromFutureJava(
                new ApiFutureToListenableFuture[QuerySnapshot](
                  firestore
                    .collection(collectionPath.value)
                    .get
                )
              )
              .map(querySnapshot => querySnapshot.getDocuments.asScala.toList)

          def batch: Task[WriteBatch] = ZIO.attempt(firestore.batch)

          def collection(
              collectionPath: CollectionPath
          ): Task[CollectionReference] =
            ZIO.attempt(firestore.collection(collectionPath.value))

          def collectionGroup(collectionPath: CollectionPath): Task[Query] =
            ZIO.attempt(firestore.collectionGroup(collectionPath.value))

          def commit(batch: WriteBatch): Task[List[WriteResult]] =
            ZIO
              .fromFutureJava(
                new ApiFutureToListenableFuture[java.util.List[WriteResult]](
                  batch.commit
                )
              )
              .map(writeResults => writeResults.asScala.toList)

          def createDocument[A](
              collectionRef: CollectionReference,
              documentPath: DocumentPath,
              data: A
          ): Task[WriteResult] =
            ZIO.fromFutureJava(
              new ApiFutureToListenableFuture[WriteResult](
                firestore
                  .collection(collectionRef.getPath)
                  .document(documentPath.value)
                  .create(data)
              )
            )

          def delete(
              collectionPath: CollectionPath,
              documentPath: DocumentPath
          ): Task[WriteResult] =
            ZIO.fromFutureJava(
              new ApiFutureToListenableFuture[WriteResult](
                firestore
                  .collection(collectionPath.value)
                  .document(documentPath.value)
                  .delete
              )
            )

          def document(
              collectionRef: CollectionReference,
              documentPath: DocumentPath
          ): Task[DocumentReference] =
            ZIO.attempt(collectionRef.document(documentPath.value))

          def documentSnapshot(
              collectionPath: CollectionPath,
              documentPath: DocumentPath
          ): Task[DocumentSnapshot] =
            ZIO.fromFutureJava(
              new ApiFutureToListenableFuture[DocumentSnapshot](
                firestore
                  .collection(collectionPath.value)
                  .document(documentPath.value)
                  .get
              )
            )

          def set[A](
              collectionPath: CollectionPath,
              documentPath: DocumentPath,
              document: A
          ): Task[WriteResult] =
            ZIO.fromFutureJava(
              new ApiFutureToListenableFuture[WriteResult](
                firestore
                  .collection(collectionPath.value)
                  .document(documentPath.value)
                  .set(document)
              )
            )

          def subCollection(
              documentReference: DocumentReference,
              collectionPath: CollectionPath
          ): Task[CollectionReference] =
            ZIO.attempt(documentReference.collection(collectionPath.value))
        }
      }
    }
}
