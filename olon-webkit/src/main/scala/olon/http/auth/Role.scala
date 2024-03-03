package olon
package http
package auth

import olon.common.Box
import olon.common.Empty
import olon.common.Full

object AuthRole {
  def apply(roleName: String): Role = new Role {
    def name = roleName
  }

  def apply(roleNames: String*): List[Role] = roleNames.toList.map(n =>
    new Role {
      def name = n
    }
  )

  // SCALA3 using `x*` instead of `x: _*`
  def apply(roleName: String, roles: Role*): Role = new Role {
    def name = roleName
  }.addRoles(roles*)
}

/** A Role may be assigned to a resource denominated by a path. A subject that
  * is assigned to the same role or to a role higher into the roles hierarchy
  * will have access to requested resource.
  */
trait Role {
  private var parent: Box[Role] = Empty
  private var childs: List[Role] = Nil

  /** The name of the role
    */
  def name: String

  /** Add child Role(s) to this role. Node name is ensured to be unique (by
    * name) in the tree.
    */
  def addRoles(roles: Role*) = {
    for (role <- roles) {
      getRoleByName(role.name) match {
        case Empty =>
          childs = role :: childs
          role.parent = Full(this)
        case _ =>
      }
    }
    this
  }

  /** Returns the child nodes
    */
  def getChildRoles = childs

  /** Returns the parent node
    */
  def getParent = parent

  /** Search for a child Role with this name
    */
  def getRoleByName(roleName: String): Box[Role] =
    (this.name == roleName) match {
      case false =>
        childs.find(role =>
          role.getRoleByName(roleName) match {
            case Empty       => false
            case theRole @ _ => return theRole
          }
        )
        Empty
      case _ => Full(this)
    }

  /** Removes the child Role
    */
  def removeRoleByName(roleName: String): Box[Role] = {
    getRoleByName(roleName).map(_.detach).openOr(Empty)
  }

  /** Removes this Role from its parent
    */
  def detach: Box[Role] = {
    this.parent.map { case p =>
      p.childs = p.childs.filter(role => role.name != this.name)
      this.parent = Empty
      this
    }
  }

  /** Verifies if this Role is a child of a role having the name <i>roleName</i>
    */
  def isChildOf(roleName: String): Boolean = (this.name == roleName) match {
    case true => return true
    case _    => this.parent.map(_.isChildOf(roleName)).openOr(false)
  }

  /** Verifies if this Role is the parent of the given Role
    */
  def isParentOf(roleName: String): Boolean =
    !this.getRoleByName(roleName).isEmpty

  override def toString: String = {
    var str = "Role(" + name;
    for (role <- childs) {
      str = str + ", " + role.toString
    }
    str + ")"
  }
}
