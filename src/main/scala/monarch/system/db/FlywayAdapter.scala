package monarch.system.db

import zio.*
import org.flywaydb.core.api.FlywayException
import org.flywaydb.core.Flyway
import monarch.system.config.Config
import monarch.system.config.PostgresConfig
import org.flywaydb.core.api.configuration.Configuration
import org.flywaydb.core.api.output.MigrateResult
import org.flywaydb.core.api.configuration.FluentConfiguration

trait FlywayAdapter:

  def migrate(): IO[FlywayException, MigrateResult]

object FlywayAdapter:

  def migrate: ZIO[FlywayAdapter, FlywayException, MigrateResult] =
    ZIO.serviceWithZIO(_.migrate())

case class FlywayAdapterLive(config: PostgresConfig) extends FlywayAdapter:
  private def flyway: UIO[Flyway] =
    UIO
      .effectTotal(Flyway.configure().dataSource(config.url, config.user, config.password))
      .map(Flyway(_))
      

  override def migrate(): IO[FlywayException, MigrateResult] = 
      flyway.map(_.migrate())

      

object FlywayAdapterLive:
  def layer: URLayer[Config, FlywayAdapterLive] = ZLayer.fromZIO(
    Config.dbConfig.map(FlywayAdapterLive(_))
  )
