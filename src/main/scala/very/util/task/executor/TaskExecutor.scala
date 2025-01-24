package very.util.task.executor

import very.util.task.Task

trait TaskExecutor[P, R, C] extends Function[P, Unit] {
  protected val graph: TaskGraph[P, R, C]

}

trait TaskGraph[P, R, C] extends Iterable[Task[P, R, C]]

object TaskGraph {
  // TODO: add dsl to combine Task to TaskGraph
}

case class LineTaskGraph[P, R, C](list: List[Task[P, R, C]])
  extends TaskGraph[P, R, C] {
  override def iterator: Iterator[Task[P, R, C]] = list.iterator
}

case class SingleTaskGraph[P, R, C](task: Task[P, R, C])
  extends TaskGraph[P, R, C] {
  @transient private lazy val list = List(task)
  override def iterator: Iterator[Task[P, R, C]] = list.iterator
}

class SimpleTaskExecutor[P, R, C](val graph: TaskGraph[P, R, C])(using
  context: C
) extends TaskExecutor[P, R, C] {
  override def apply(param: P): Unit = {
    for (task <- graph) {
      task.run(param)(using context)
    }
  }
}
def singleTaskExecutor[P, R, C](task: Task[P, R, C])(using context: C) =
  SimpleTaskExecutor(
    SingleTaskGraph(task)
  )(using context)
