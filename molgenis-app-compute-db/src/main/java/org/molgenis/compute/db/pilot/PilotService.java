package org.molgenis.compute.db.pilot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.util.WebAppUtil;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date:
 * /usr/local/mysql/bin/mysql -u molgenis -pmolgenis compute < $HOME/compute.sql
 * 20/07/2012 Time: 16:53 To change this template use File | Settings | File
 * Templates.
 */
public class PilotService implements MolgenisService
{
	private static final Logger LOG = Logger.getLogger(PilotService.class);

	public static final String TASK_GENERATED = "generated";
	public static final String TASK_READY = "ready";
	public static final String TASK_RUNNING = "running";
	public static final String TASK_FAILED = "failed";
	public static final String TASK_DONE = "done";

	public PilotService(MolgenisContext mc)
	{
	}

	@Override
	public synchronized void handleRequest(MolgenisRequest request, MolgenisResponse response) throws ParseException,
			DatabaseException, IOException
	{
		LOG.debug(">> In handleRequest!");
		LOG.debug(request);

		if ("started".equals(request.getString("status")))
		{
			String backend = request.getString("backend");

			LOG.info("Looking for task to execute for host [" + backend + "]");

			List<ComputeTask> tasks = findRunTasksReady(backend);

			if (tasks.isEmpty())
			{
				LOG.info("No tasks to start for host [" + backend + "]");
				return;
			}

			ComputeTask task = tasks.get(0);

			// we add task id to the run listing to identify task when
			// it is done
			String pilotServiceUrl = request.getAppLocation() + request.getServicePath();
			String computeScript = task.getComputeScript().replaceAll("\r", "");
			String runName = task.getComputeRun().getName();
			String userEnvironment = task.getComputeRun().getUserEnvironment() == null ? "" : task.getComputeRun()
					.getUserEnvironment();

			// TODO escape quotes ??
			StringBuilder sb = new StringBuilder();
			sb.append("echo TASKNAME:").append(task.getName()).append("\n");
			sb.append("echo RUNNAME:").append(runName).append("\n");
			sb.append("echo \"").append(userEnvironment).append("\" > user.env\n");

			for (ComputeTask prev : task.getPrevSteps())
			{
				sb.append("echo \"").append(prev.getOutputEnvironment()).append("\" > ").append(prev.getName())
						.append(".env\n");
			}

			sb.append(computeScript).append("\n");
			sb.append("cp log.log done.log\n");

			// Upload log_file and if present the output env file
			sb.append("if [ -f ").append(task.getName()).append(".env ]; then\n");
			sb.append("curl -F status=done -F log_file=@done.log ");
			sb.append("-F output_file=@").append(task.getName()).append(".env ");
			sb.append(pilotServiceUrl);
			sb.append("\nelse\n");
			sb.append("curl -F status=done -F log_file=@done.log ");
			sb.append(pilotServiceUrl);
			sb.append("\nfi\n");

			String taskScript = sb.toString();

			LOG.info("Script for task [" + task.getName() + "] of run [ " + runName + "]:\n" + taskScript);

			// change status to running
			task.setStatusCode(PilotService.TASK_RUNNING);
			WebAppUtil.getDatabase().update(task);

			// send response
			PrintWriter pw = response.getResponse().getWriter();
			try
			{
				pw.write(taskScript);
				pw.flush();
			}
			finally
			{
				IOUtils.closeQuietly(pw);
			}
		}
		else
		{
			String logFileContent = FileUtils.readFileToString(request.getFile("log_file"));
			LogFileParser logfile = new LogFileParser(logFileContent);
			String taskName = logfile.getTaskName();
			String runName = logfile.getRunName();
			List<String> logBlocks = logfile.getLogBlocks();
			String runInfo = StringUtils.join(logBlocks, "\n");

			List<ComputeTask> tasks = WebAppUtil.getDatabase().query(ComputeTask.class).eq(ComputeTask.NAME, taskName)
					.and().eq(ComputeTask.COMPUTERUN_NAME, runName).find();

			if (tasks.isEmpty())
			{
				LOG.warn("No task found for TASKNAME [" + taskName + "] of RUN [" + runName + "]");
				return;
			}

			ComputeTask task = tasks.get(0);

			if ("done".equals(request.getString("status")))
			{
				LOG.info(">>> task [" + taskName + "] of run [" + runName + "] is finished");
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					task.setStatusCode(TASK_DONE);
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);

					File output = request.getFile("output_file");
					if (output != null)
					{
						task.setOutputEnvironment(FileUtils.readFileToString(output));
					}
				}
				else
				{
					LOG.warn("from done: something is wrong with task [" + taskName + "] of run [" + runName
							+ "] status should be [running] but is [" + task.getStatusCode() + "]");
				}
			}
			else if ("pulse".equals(request.getString("status")))
			{
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					LOG.info(">>> pulse from task [" + taskName + "] of run [" + runName + "]");
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
				}
			}
			else if ("nopulse".equals(request.getString("status")))
			{
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					LOG.info(">>> no pulse from task [" + taskName + "] of run [" + runName + "]");
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
					task.setStatusCode("failed");
				}
				else if (task != null && task.getStatusCode().equalsIgnoreCase(TASK_DONE))
				{
					LOG.info("double check: job is finished & no pulse from it for task [" + taskName + "] of run ["
							+ runName + "]");
				}
			}

			WebAppUtil.getDatabase().update(task);
		}
	}

	private List<ComputeTask> findRunTasksReady(String backendName) throws DatabaseException
	{

		List<ComputeRun> runs = WebAppUtil.getDatabase().query(ComputeRun.class)
				.equals(ComputeRun.COMPUTEBACKEND_NAME, backendName).find();

		return WebAppUtil.getDatabase().query(ComputeTask.class)
				.equals(ComputeTask.STATUSCODE, PilotService.TASK_READY).in(ComputeTask.COMPUTERUN, runs).find();
	}
}
