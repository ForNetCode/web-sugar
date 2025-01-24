package very.util.task

import very.util.task.TaskExecutorProxy.executorInstance
import very.util.task.executor.TaskExecutor
import org.quartz.{Job, JobExecutionContext}
import very.util.web.LogSupport

import java.time.LocalDateTime

type TaskKeyFunc[P] = (P, LocalDateTime) => String


class TaskExecutorProxy[P, R, C] extends Job with LogSupport {
  override def execute(context: JobExecutionContext): Unit = {

    val dataMap = context.getJobDetail.getJobDataMap
    val taskExecutor = dataMap
      .get(TaskExecutorProxy.executorInstance)
      .asInstanceOf[TaskExecutor[P, R, C]]

    val param =
      dataMap.get(TaskExecutorProxy.executorParameter).asInstanceOf[() => P]
    logger.debug(s"cron job: ${context.getJobDetail.getKey.getName} begin to run")
    val time = System.currentTimeMillis()
    taskExecutor(param())
    logger.debug(
      s"cron job: ${context.getJobDetail.getKey.getName} cost: ${(System.currentTimeMillis() - time) / 1000} s"
    )
  }
}

object TaskExecutorProxy {
  val executorInstance: String = "taskInstance"
  val executorParameter: String = "taskParameter"
}
