package org.broadinstitute.gatk.queue.engine.localq

import org.broadinstitute.gatk.queue.engine.CommandLineJobManager
import org.broadinstitute.gatk.queue.function.CommandLineFunction

/**
 * Runs jobs using LocalQ
 */
class LocalQJobManager extends CommandLineJobManager[LocalQJobRunner]{
  def runnerType = classOf[LocalQJobRunner]
  def create(function: CommandLineFunction) = new LocalQJobRunner(function)

  override def updateStatus(runners: Set[LocalQJobRunner]) = {
    var updatedRunners = Set.empty[LocalQJobRunner]
    runners.foreach(runner => if (runner.updateJobStatus()) {updatedRunners += runner})
    updatedRunners
  }
  override def tryStop(runners: Set[LocalQJobRunner]) {
    runners.foreach(_.tryStop())
  }

}
