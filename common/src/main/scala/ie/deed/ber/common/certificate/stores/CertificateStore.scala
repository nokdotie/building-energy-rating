package ie.deed.ber.common.certificate.stores

import ie.deed.ber.common.certificate.{Certificate, CertificateNumber}
import zio.ZIO
import zio.stream.{ZStream, ZPipeline}

trait CertificateStore {
  def upsertBatch(certificates: Iterable[Certificate]): ZIO[Any, Throwable, Int]
  val upsertPipeline: ZPipeline[Any, Throwable, Certificate, Int]

  def getByNumber(
      id: CertificateNumber
  ): ZIO[Any, Throwable, Option[Certificate]]
}

object CertificateStore {
  def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[CertificateStore, Throwable, Int] =
    ZIO.serviceWithZIO[CertificateStore] { _.upsertBatch(certificates) }

  val upsertPipeline: ZPipeline[CertificateStore, Throwable, Certificate, Int] =
    ZPipeline.serviceWithPipeline[CertificateStore] { _.upsertPipeline }

  def getByNumber(
      id: CertificateNumber
  ): ZIO[CertificateStore, Throwable, Option[Certificate]] =
    ZIO.serviceWithZIO[CertificateStore] { _.getByNumber(id) }
}
