package olon
package mapper
package view

import olon.http.{ Paginator, PaginatorSnippet, SortedPaginator, SortedPaginatorSnippet }

/**
 * Helper for when using paginators with a ModelSnippet.
 * Adds a dispatch that delegates the "paginate" snippet to the paginator member.
 * @author nafg and Timothy Perrett
 */
trait PaginatedModelSnippet[T <: Mapper[T]] extends ModelSnippet[T] {
  abstract override def dispatch: DispatchIt = super.dispatch orElse Map("paginate" -> paginator.paginate)
  /**
   * The paginator to delegate to
   */
  val paginator: PaginatorSnippet[T]
}

/**
 * Paginate mapper instances by supplying the model you
 * wish to paginate and Paginator will run your query for you etc.
 *
 * @param meta The singleton of the Mapper class you're paginating
 * @author nafg and Timothy Perrett
 */
class MapperPaginator[T <: Mapper[T]](val meta: MetaMapper[T]) extends Paginator[T] {
  /**
   * QueryParams to use always
   */
  var constantParams: Seq[QueryParam[T]] = Nil

  def count: Long = meta.count(constantParams: _*)
  def page: Seq[T] = meta.findAll(constantParams ++ Seq[QueryParam[T]](MaxRows(itemsPerPage), StartAt(first)): _*)
}

/**
 * Convenience class that combines MapperPaginator with PaginatorSnippet
 * @param meta The singleton of the Mapper class you're paginating
 */
class MapperPaginatorSnippet[T <: Mapper[T]](meta: MetaMapper[T])
  extends MapperPaginator[T](meta) with PaginatorSnippet[T]

/**
 * Implements MapperPaginator and SortedPaginator.
 * @param meta The singleton of the Mapper class you're paginating
 * @param initialSort The field to sort by initially
 * @param _headers Pairs of column labels and MappedFields.
 */
class SortedMapperPaginator[T <: Mapper[T]](meta: MetaMapper[T],
                                initialSort: olon.mapper.MappedField[_, T],
                                _headers: (String, MappedField[_, T])*)
    extends MapperPaginator[T](meta) with SortedPaginator[T, MappedField[_, T]] {

    val headers = _headers.toList
    sort = (headers.indexWhere{case (_,`initialSort`)=>true; case _ => false}, true)

    override def page: Seq[T] = meta.findAll(constantParams ++ Seq[QueryParam[T]](mapperSort, MaxRows(itemsPerPage), StartAt(first)): _*)
    private def mapperSort = sort match {
      case (fieldIndex, ascending) =>
        OrderBy(
          headers(fieldIndex) match {case (_,f)=>f},
          if(ascending) Ascending else Descending
        )
    }
}

/**
 * Convenience class that combines SortedMapperPaginator and SortedPaginatorSnippet.
 * @param meta The singleton of the Mapper class you're paginating
 * @param initialSort The field to sort by initially
 * @param headers Pairs of column labels and MappedFields.
 */
class SortedMapperPaginatorSnippet[T <: Mapper[T]](
  meta: MetaMapper[T],
  initialSort: olon.mapper.MappedField[_, T],
  headers: (String, MappedField[_, T])*
) extends SortedMapperPaginator[T](meta, initialSort, headers: _*)
  with SortedPaginatorSnippet[T, MappedField[_, T]]

