package be.objectify.deadbolt

package object scala {
  type RoleGroup = Array[String]
  type RoleGroups = List[RoleGroup]

  def allOf(roleNames: String*): RoleGroup = roleNames.toArray

  def allOfGroup(roleNames: String*): RoleGroups = List(roleNames.toArray)

  def anyOf(roleGroups: RoleGroup*): RoleGroups = roleGroups.toList
}
