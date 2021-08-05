package olon 
package mapper 

import http.S

object DB extends db.DB1 {
  db.DB.queryCollector = {
    case (query, time) => 
      query.statementEntries.foreach{ case db.DBLogEntry(stmt, duration) => S.logQuery(stmt, duration) }
  }
}
