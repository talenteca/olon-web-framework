package olon

package object mapper {
  type SuperConnection = db.SuperConnection
  type ConnectionIdentifier = util.ConnectionIdentifier
  type DriverType = db.DriverType
  type ConnectionManager = db.ConnectionManager
  type DBLogEntry = db.DBLogEntry
  type StandardDBVendor = db.StandardDBVendor

  def DBLogEntry: db.DBLogEntry.type = db.DBLogEntry
  def DefaultConnectionIdentifier: util.DefaultConnectionIdentifier.type = util.DefaultConnectionIdentifier
  def DriverType: db.DriverType.type = db.DriverType
}
