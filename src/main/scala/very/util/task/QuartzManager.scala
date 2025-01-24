package very.util.task

import very.util.task.executor.TaskExecutor
import org.quartz.*
import org.quartz.core.jmx.JobDataMapSupport
import org.quartz.impl.DirectSchedulerFactory
import org.quartz.simpl.{ RAMJobStore, SimpleThreadPool }
import very.util.web.LogSupport

import scala.jdk.CollectionConverters.*

trait WithQuartz {
  protected object quartzManager extends QuartzManager
}
class QuartzManager extends LogSupport {

  private val threadPool = new SimpleThreadPool(2, 1)
  threadPool.setThreadNamePrefix("Quartz_")

  DirectSchedulerFactory
    .getInstance()
    .createScheduler(threadPool, new RAMJobStore())
  private val scheduler = DirectSchedulerFactory.getInstance().getScheduler
  scheduler.startDelayed(2)

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = {
      scheduler.shutdown()
    }
  })

  def schedulerTask[P, R, C, Z<:Trigger](
    job: TaskExecutor[P, R, C],
    jobName: String,
    param: () => P,
    //cronExpression: String
    schedule: ScheduleBuilder[Z]
  ): Unit = {
    val cronJob = JobBuilder
      .newJob()
      .ofType(classOf[TaskExecutorProxy[P, R, C]])
      .setJobData(
        JobDataMapSupport.newJobDataMap(
          Map(
            TaskExecutorProxy.executorInstance -> job,
            TaskExecutorProxy.executorParameter -> param,
          ).asJava
        )
      )
      .withIdentity(jobName)
      .build()
    val trigger = TriggerBuilder
      .newTrigger()
      .withIdentity(jobName)
      .withSchedule(schedule)
      //.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
      .build()

    val date = scheduler.scheduleJob(cronJob, trigger)
    logger.debug(s"task: $jobName scheduled to run at ${date.toString}")
  }
}
