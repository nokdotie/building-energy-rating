package ie.nok.ber.stores

import ie.nok.ber.{Certificate, CertificateNumber, Eircode}
import zio.ZIO
import zio.stream.{ZPipeline, ZStream}

trait CertificateStore {
  protected[ber] def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[Any, Throwable, Int]
  protected[ber] val upsertPipeline: ZPipeline[Any, Throwable, Certificate, Int]

  def getByNumber(
      id: CertificateNumber
  ): ZIO[Any, Throwable, Option[Certificate]]

  def getAllByEircode(
      eircode: Eircode
  ): ZIO[Any, Throwable, List[Certificate]]
}

object CertificateStore {
  protected[ber] def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[CertificateStore, Throwable, Int] =
    ZIO.serviceWithZIO[CertificateStore] { _.upsertBatch(certificates) }

  protected[ber] val upsertPipeline: ZPipeline[CertificateStore, Throwable, Certificate, Int] =
    ZPipeline.serviceWithPipeline[CertificateStore] { _.upsertPipeline }

  def getByNumber(
      id: CertificateNumber
  ): ZIO[CertificateStore, Throwable, Option[Certificate]] =
    ZIO.serviceWithZIO[CertificateStore] { _.getByNumber(id) }

  def getAllByEircode(
      eircode: Eircode
  ): ZIO[CertificateStore, Throwable, List[Certificate]] =
    ZIO.serviceWithZIO[CertificateStore] { _.getAllByEircode(eircode) }
}
