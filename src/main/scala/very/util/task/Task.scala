package very.util.task

// 任务描述
trait Task[Param, Response, Context] {

  def name: String

  // 用作唯一性校验
  def run(
    param: Param,
  )(using context: Context): Response
}

type UnitTask = Task[Unit, Unit, Unit]

//trait UniqueTask[P, R, C] extends Task[P, R, C] {
//  // uniqueKey for this Task
//  def key: String
//}
