package zio.gcp

import com.google.cloud.firestore.{Firestore => _, _}

import zio.{RIO, ZIO}

package object firestore {

  type Firestore = Firestore.Service

  def allCollections: RIO[Firestore, List[CollectionReference]] =
    ZIO.environmentWithZIO(_.get.allCollections)

  def allDocuments(
      collectionPath: CollectionPath
  ): RIO[Firestore, List[QueryDocumentSnapshot]] =
    ZIO.environmentWithZIO(_.get.allDocuments(collectionPath))

  def batch: RIO[Firestore, WriteBatch] =
    ZIO.environmentWithZIO(_.get.batch)

  def collection(
      collectionPath: CollectionPath
  ): RIO[Firestore, CollectionReference] =
    ZIO.environmentWithZIO(_.get.collection(collectionPath))

  def collectionGroup(collectionPath: CollectionPath): RIO[Firestore, Query] =
    ZIO.environmentWithZIO(_.get.collectionGroup(collectionPath))

  def commit(batch: WriteBatch): RIO[Firestore, List[WriteResult]] =
    ZIO.environmentWithZIO(_.get.commit(batch))

  def createDocument[A](
      collectionRef: CollectionReference,
      documentPath: DocumentPath,
      data: A
  ): RIO[Firestore, WriteResult] =
    ZIO.environmentWithZIO(
      _.get.createDocument(collectionRef, documentPath, data)
    )

  def delete(
      collectionPath: CollectionPath,
      documentPath: DocumentPath
  ): RIO[Firestore, WriteResult] =
    ZIO.environmentWithZIO(_.get.delete(collectionPath, documentPath))

  def document(
      collectionRef: CollectionReference,
      documentPath: DocumentPath
  ): RIO[Firestore, DocumentReference] =
    ZIO.environmentWithZIO(_.get.document(collectionRef, documentPath))

  def documentSnapshot(
      collectionPath: CollectionPath,
      documentPath: DocumentPath
  ): RIO[Firestore, DocumentSnapshot] =
    ZIO.environmentWithZIO(_.get.documentSnapshot(collectionPath, documentPath))

  def set[A](
      collectionPath: CollectionPath,
      documentPath: DocumentPath,
      document: A
  ): RIO[Firestore, WriteResult] =
    ZIO.environmentWithZIO(_.get.set(collectionPath, documentPath, document))

  def subCollection(
      documentRef: DocumentReference,
      collectionPath: CollectionPath
  ): RIO[Firestore, CollectionReference] =
    ZIO.environmentWithZIO(_.get.subCollection(documentRef, collectionPath))
}
