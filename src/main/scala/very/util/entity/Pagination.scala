package very.util.entity

trait Page {
  //def page:Int
  //def pageSize:Int

  def offset: Int //= (page - 1) * pageSize

  def limit: Int// = pageSize
}

case class Pagination(page: Int, pageSize: Int) extends Page {
  assert(pageSize <= 50)
  assert(page > 0)

  def offset: Int = (page - 1) * pageSize

  def limit: Int = pageSize
}
case class Pagination2(page:Int, pageSize:Int) extends Page {
  def offset: Int = (page - 1) * pageSize
  def limit: Int = pageSize
}


case class Offset(start:Int, end:Int) extends Page {
  assert(start >= 0 && end >= 1 && end - start <=100)

  def offset: Int = start

  def limit: Int = end -start
}