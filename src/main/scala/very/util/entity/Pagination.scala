package very.util.entity

trait Page {
  def page:Int
  def pageSize:Int

  def offset: Int = (page - 1) * pageSize

  def limit: Int = pageSize
}

case class Pagination(page: Int, pageSize: Int) extends Page {
  assert(pageSize <= 50)
  assert(page > 0)
}
case class Pagination2(page:Int, pageSize:Int) extends Page
