package org.broadinstitute.gatk.queue.engine.localq

import net.razorvine.pyro.{PyroURI, PyroProxy}
import org.broadinstitute.gatk.queue.engine.{RunnerStatus, CommandLineJobRunner}
import org.broadinstitute.gatk.queue.function.CommandLineFunction
import org.broadinstitute.gatk.queue.util.Logging
import org.broadinstitute.gatk.utils.runtime.{ProcessSettings, OutputStreamSettings, ProcessController}

/**
 * Submits jobs to a running instance of localq
 * @param function Command to run.
 */
class LocalQJobRunner(val function: CommandLineFunction) extends CommandLineJobRunner with Logging{

  /** Job Id of the currently executing job. */
  private var jobId:Int = -1
  override def jobIdString = jobId.toString

  // same as in DrmaaJobRunner
  protected val jobNameFilter = """[^A-Za-z0-9_]"""
  protected val jobNameLength = 500

  def localq_connect(): PyroProxy ={
    val uri = new PyroURI( scala.io.Source.fromFile(function.jobQueue).mkString.trim )
    val remoteObj = new PyroProxy( uri )
    return remoteObj
  }

  def start() {
    val remoteObj = localq_connect()

    // function.nCoresRequest is an Option[Int], so the underlying Int has to be extracted
    val cores_requested:String = (function.nCoresRequest match {
      case Some(x:Int) => x // this extracts the value as an Int
      case _ => 1 // if for some reason it wasn't set, use 1 core
    }).toString

    val jobName:String = function.jobRunnerJobName.take(jobNameLength).replaceAll(jobNameFilter, "_")


    updateStatus(RunnerStatus.RUNNING)
    val returnValue:Object = remoteObj.call(
      "add_script",
      jobScript.toString,
      cores_requested,
      function.commandDirectory.toString,
      function.jobOutputFile.getPath,
      function.jobOutputFile.getPath,
      jobName
    )
    remoteObj.close()

    try {
      jobId = returnValue.toString.toInt
    } catch { // if job could not be submitted, fail.
      case e: Exception =>
        logger.error("Unable to submit job " + function.commandLine.mkString(" ") + " using " + cores_requested, e)
    }
    logger.info("Submitted job with id " + jobIdString + " using " + cores_requested + " cores.")
  }

  /**
   * Possibly invoked from a shutdown thread, find and
   * stop the controller from the originating thread
   */
  def tryStop() {
    val remoteObj = localq_connect()
    try {
      remoteObj.call("stop_job_with_id",
        jobIdString)
      remoteObj.close()
    } catch {
      case e: Exception =>
        logger.error("Unable to kill shell job: " + function.description, e)
    }
    updateJobStatus()
    // require that the status is FAILED after stopping
    if( ! status.equals( RunnerStatus.FAILED )){
      logger.error("Unable to kill shell job: " + function.description)
    }
  }

  def updateJobStatus(): Boolean = {
    val remoteObj = localq_connect()
    var returnStatus:RunnerStatus.Value = null

    val returnValue:Object = remoteObj.call(
      "get_status",
      jobIdString)
    remoteObj.close()

    if( returnValue == null ){
      false
    }else{
      val jobStatus:String = returnValue.toString.trim
      if( jobStatus.equals("RUNNING") || jobStatus.equals("PENDING")) {
        returnStatus = RunnerStatus.RUNNING
      } else if (jobStatus.equals("CANCELLED") || jobStatus.equals("FAILED")) {
        returnStatus = RunnerStatus.FAILED
      } else if(jobStatus.equals("COMPLETED")){
        returnStatus = RunnerStatus.DONE
      } else if(jobStatus.equals("NOT_FOUND")){
        logger.error("Unable find job with id " + jobIdString + " in the queue when updating job status.")
      }
      updateStatus(returnStatus)
      true
    }
  }
}
