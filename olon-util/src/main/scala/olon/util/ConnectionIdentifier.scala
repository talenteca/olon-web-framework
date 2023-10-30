package olon
package util

trait ConnectionIdentifier {
  def jndiName: String

  override def toString() = "ConnectionIdentifier(" + jndiName + ")"

  override def hashCode() = jndiName.hashCode()

  override def equals(other: Any): Boolean = other match {
    case ci: ConnectionIdentifier => ci.jndiName == this.jndiName
    case _                        => false
  }
}

case object DefaultConnectionIdentifier extends ConnectionIdentifier {
  val jndiName = Props.get("default.jndi.name", "lift")
}
