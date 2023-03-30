package ie.deed.ber.common.certificate.stores

import ie.deed.ber.common.certificate._
import zio._
import zio.stream.{ZStream, ZPipeline}

trait CertificateStore {
  def upsertBatch(certificates: Iterable[Certificate]): ZIO[Any, Throwable, Int]
  val upsertPipeline: ZPipeline[Any, Throwable, Certificate, Int]

  val streamMissingSeaiIeHtml: ZStream[Any, Throwable, CertificateNumber]
  val streamMissingSeaiIePdf: ZStream[Any, Throwable, CertificateNumber]
  val streamMissingEircodeIeEcadData: ZStream[
    CertificateStore,
    Throwable,
    Certificate
  ]

  def getById(id: CertificateNumber): ZIO[Any, Throwable, Option[Certificate]]
}

object CertificateStore {
  def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[CertificateStore, Throwable, Int] =
    ZIO.serviceWithZIO[CertificateStore] { _.upsertBatch(certificates) }

  val upsertPipeline: ZPipeline[CertificateStore, Throwable, Certificate, Int] =
    ZPipeline.serviceWithPipeline[CertificateStore] { _.upsertPipeline }

  val streamMissingSeaiIeHtml
      : ZStream[CertificateStore, Throwable, CertificateNumber] =
    ZStream.serviceWithStream[CertificateStore](_.streamMissingSeaiIeHtml)

  val streamMissingSeaiIePdf
      : ZStream[CertificateStore, Throwable, CertificateNumber] =
    ZStream.serviceWithStream[CertificateStore](_.streamMissingSeaiIePdf)

  val streamMissingEircodeIeEcadData
      : ZStream[CertificateStore, Throwable, Certificate] =
    ZStream.serviceWithStream[CertificateStore](
      _.streamMissingEircodeIeEcadData
    )

  def getById(
      id: CertificateNumber
  ): ZIO[CertificateStore, Throwable, Option[Certificate]] =
    ZIO.serviceWithZIO[CertificateStore] { _.getById(id) }
}
