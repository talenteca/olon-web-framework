package olon
package db

import java.sql.Connection
import olon.common._
import olon.util.ConnectionIdentifier

/**
 * Vend JDBC connections
 */
trait ConnectionManager {
  def newConnection(name: ConnectionIdentifier): Box[Connection]
  def releaseConnection(conn: Connection)
  def newSuperConnection(name: ConnectionIdentifier): Box[SuperConnection] = Empty
}

