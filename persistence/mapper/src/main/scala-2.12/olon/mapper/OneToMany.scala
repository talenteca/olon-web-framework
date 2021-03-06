package olon
package mapper


private[mapper] object RecursiveType {
  val rec: { type R0 <: Mapper[R0] } = null
  type Rec = rec.R0
}
import olon.mapper.RecursiveType._

/**
 * Add this trait to a Mapper for managed one-to-many support
 * For example: class Contact extends LongKeyedMapper[Contact] with OneToMany[Long, Contact] { ... }
 * @tparam K the type of the primary key
 * @tparam T the mapper type
 * @author nafg
 */
trait OneToMany[K,T<:KeyedMapper[K, T]] extends KeyedMapper[K,T] { this: T =>

  private[mapper] lazy val oneToManyFields: List[MappedOneToManyBase[Rec]] = {
    new FieldFinder[MappedOneToManyBase[Rec]](
      getSingleton,
      olon.common.Logger(classOf[OneToMany[K,T]])
    ).accessorMethods map (_.invoke(this).asInstanceOf[MappedOneToManyBase[Rec]])
  }

  /**
   * An override for save to propagate the save to all children
   * of this parent.
   * Returns false as soon as the parent or a one-to-many field returns false.
   * If they are all successful returns true.
   */
  override def save: Boolean = {
    val ret = super.save &&
      oneToManyFields.forall(_.save)
    ret
  }

  /**
   * An override for delete_! to propagate the deletion
   * to all children of one-to-many fields implementing Cascade.
   * Returns false as soon as the parent or a one-to-many field returns false.
   * If they are all successful returns true.
   */
  override def delete_! : Boolean = DB.use(connectionIdentifier){ _ =>
    if(oneToManyFields.forall{(_: MappedOneToManyBase[_ <: Mapper[_]]) match {
        case f: Cascade[_] => f.delete_!
        case _ => true
      }
    })
      super.delete_!
    else {
      DB.rollback(connectionIdentifier)
      false
    }
  }


  /**
   * This implicit allows a MappedForeignKey to be used as foreignKey function.
   * Returns a function that takes a Mapper and looks up the actualField of field on the Mapper.
   */
  implicit def foreignKey[K, O<:Mapper[O], T<:KeyedMapper[K,T]](field: MappedForeignKey[K,O,T]): O=>MappedForeignKey[K,O,T] =
    field.actualField(_).asInstanceOf[MappedForeignKey[K,O,T]]

  /**
   * Simple OneToMany support for children from the same table
   */
  class MappedOneToMany[O <: Mapper[O]](meta: MetaMapper[O], foreign: MappedForeignKey[K,O,T], qp: QueryParam[O]*)
    extends MappedOneToManyBase[O](
      ()=>{
        val ret = meta.findAll(By(foreign, primaryKeyField.get) :: qp.toList : _*)
        for(child <- ret) {
          foreign.actualField(child).asInstanceOf[MappedForeignKey[K,O,T]].primeObj(olon.common.Full(OneToMany.this : T))
        }
        ret
      },
      foreign
    )

  /**
   * This is the base class to use for fields that represent one-to-many or parent-child relationships.
   * Maintains a list of children, tracking pending additions and deletions, and
   * keeping their foreign key pointed to this mapper.
   * Implements Buffer, so the children can be managed as one.
   * Most users will use MappedOneToMany, however to support children from multiple tables
   * it is necessary to use MappedOneToManyBase.
   * @param reloadFunc A function that returns a sequence of children from storage.
   * @param foreign A function that gets the MappedForeignKey on the child that refers to this parent
   */
  class MappedOneToManyBase[O <: Mapper[_]](val reloadFunc: () => Seq[O],
                                            val foreign: O => MappedForeignKey[K,_,T]) extends scala.collection.mutable.Buffer[O] {
    private var inited = false
    private var _delegate: List[O] = _
    /**
     * children that were added before the parent was ever saved
    */
    private var unlinked: List[O] = Nil
    protected def delegate: List[O] = {
      if(!inited) {
        refresh()
        inited = true
      }
      _delegate
    }
    protected def delegate_=(d: List[O]): Unit = _delegate = d

    /**
     * Takes ownership of e. Sets e's foreign key to our primary key
     */
    protected def own(e: O): O = {
      val f0 = foreign(e).asInstanceOf[Any]
      f0 match {
        case f: MappedLongForeignKey[O,T] with MappedForeignKey[K,_,T] =>
          f.apply(OneToMany.this)
        case f: MappedForeignKey[K,_,T] =>
          f.set(OneToMany.this.primaryKeyField.get)
      }
      if(!OneToMany.this.saved_?)
         unlinked ::= e
      e
    }
    /**
     * Relinquishes ownership of e. Resets e's foreign key to its default value.
     */
    protected def unown(e: O): O = {
      val f = foreign(e)
      f.set(f.defaultValue)
      unlinked = unlinked filter {e.ne}
      e
    }
    /**
     * Returns the backing List
     */
    def all: List[O] = delegate

    // 2.8: return this
    def +=(elem: O): MappedOneToManyBase.this.type = {
      delegate = delegate ++ List(own(elem))
      this
    }
    // 2.7
    //def readOnly = all
    def length: Int = delegate.length
    // 2.7
    //def elements = delegate.elements
    // 2.8
    def iterator: Iterator[O] = delegate.iterator

    def apply(n: Int): O = delegate(n)

    // 2.7
    /* def +:(elem: O) = {
      delegate ::= own(elem)
      this
    } */
    // 2.8
    def +=:(elem: O): MappedOneToManyBase.this.type = {
      delegate ::= own(elem)
      this
    }

    override def indexOf[B >: O](e: B): Int = delegate.indexWhere(e.asInstanceOf[AnyRef].eq)

    // 2.7
    // def insertAll(n: Int, iter: Iterable[O]) {
    // 2.8
    def insertAll(n: Int, iter: Traversable[O]) {
      val (before, after) = delegate.splitAt(n)
      iter foreach own
      delegate = before ++ iter ++ after
    }

    def update(n: Int, newelem: O): Unit = {
      unown(delegate(n))
      delegate = delegate.take(n) ++ List(own(newelem)) ++ delegate.drop(n+1)
    }

    def remove(n: Int): O = {
      val e = unown(delegate(n))
      delegate = delegate.filterNot(e.eq)
      e
    }

    def clear(): Unit = {
      while(delegate.nonEmpty)
        remove(0)
    }

    /**
     * Reloads the children from storage.
     * NOTE: This may leave children in an inconsistent state.
     * It is recommended to call save or clear() before calling refresh.
     */
    def refresh(): Unit = {
      delegate = reloadFunc().toList
      if(saved_?)
        unlinked = Nil
      else
        unlinked = _delegate
    }

    /**
     * Saves this "field," i.e., all the children it represents.
     * Returns false as soon as save on a child returns false.
     * Returns true if all children were saved successfully.
     */
    def save: Boolean = {
      unlinked foreach {u =>
        val f = foreign(u)
        if(f.obj.map(_ eq OneToMany.this) openOr true) // obj is Empty or this
          f.apply(OneToMany.this)
      }
      unlinked = Nil
      delegate = delegate.filter {e =>
          foreign(e).get == OneToMany.this.primaryKeyField.get ||
            foreign(e).obj.map(_ eq OneToMany.this).openOr(false) // obj is this but not Empty
      }
      delegate.forall(_.save)
    }

    override def toString: String = {
      val c = getClass.getSimpleName
      val l = c.lastIndexOf("$")
      c.substring(c.lastIndexOf("$",l-1)+1, l) + delegate.mkString("[",", ","]")
    }
  }

  /**
   * Adds behavior to delete orphaned fields before save.
   */
  trait Owned[O<:Mapper[_]] extends MappedOneToManyBase[O] {
    var removed: List[O] = Nil
    override def unown(e: O) = {
      removed = e :: removed
      super.unown(e)
    }
    override def own(e: O) = {
      removed = removed filter {e.ne}
      super.own(e)
    }
    override def save: Boolean = {
      val unowned = removed.filter{ e =>
        val f = foreign(e)
        f.get == f.defaultValue
      }
      unowned foreach {_.delete_!}
      super.save
    }
  }

  /**
   * Trait that indicates that the children represented
   * by this field should be deleted when the parent is deleted.
   */
  trait Cascade[O<:Mapper[_]] extends MappedOneToManyBase[O] {
    def delete_! : Boolean = {
      delegate.forall { e =>
          if(foreign(e).get ==
            OneToMany.this.primaryKeyField.get) {
              e.delete_!
            }
          else
            true // doesn't constitute a failure
      }
    }
  }
}
