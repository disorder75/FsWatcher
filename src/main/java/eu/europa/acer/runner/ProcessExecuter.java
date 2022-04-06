package eu.europa.acer.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import eu.europa.acer.fswatcher.config.ProcessBuilderConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Execute a separate process on the host machine 
 * @apiNote the assumption here is:
 * 		all the process started by this class are usefull to create and keep
 * 		alive the filesystem between a pod and the external world.
 * 		Currently this task is in charge to SSHFS, for this reason the started
 * 		process must be always alive, if java detect the exit then will put
 * 		the Kubernetes probes in a status in order to restart the pod as soon
 * 		as possible.
 * 
 *      Remember: without the filesystem correctly mounted the pod is isolated
 *      		  and no events will be notified anymore by inotify/watcher
 *
 */
@Component
@Order(1)
@Profile("!test")
@Slf4j
public class ProcessExecuter implements CommandLineRunner {
	
	private static final String PROCESS_HAS_TERMINATED_WITH_EXIT_CODE = "process {} has terminated with exit code {}";
	private static final String PROCESS_HAS_BEEN_PREMATURELY_INTERRUPTED = "process {} has been prematurely interrupted";
	private static final String ERRORS_FROM_PROCESS = "errors from process {}";
	private static final String EXECUTION_FAILED = "Execution failed";
	private static final String PROCESS_PID = "started pid process {}";
	private static final String EXE_PROCESS = "executing {} on operating system";
	
	@Autowired
	private ProcessBuilderConfig processBuilderConfig;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
	@Override
	public void run(String... args) throws Exception {
		
		log.info(EXE_PROCESS, processBuilderConfig.getArgs());
		ProcessBuilder pb = new ProcessBuilder().command(processBuilderConfig.getArgs());
		pb.redirectErrorStream(true);
		try {
			Process proc = pb.start();
			ProcessHandle h = proc.toHandle();
			log.info(PROCESS_PID, h.pid());
			AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
			AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.CORRECT);

			/*
			 *		consume on separate thread the output to avoid hang
			 *		with the Spring CommandLineRunner but also with scripts or 
			 *		all the process that requires consumed the standard output 
			 */
			 new Thread(new Runnable() {
				public void run() {
					try (InputStream processStdOutput = proc.getInputStream();
			           	 Reader r = new InputStreamReader(processStdOutput);
			           	 BufferedReader br = new BufferedReader(r)) {
			               String line;
			               while ((line = br.readLine()) != null) {
			            	   System.out.println(line);
			               }
					} catch (IOException e) {
						log.error(ERRORS_FROM_PROCESS, h.pid(), e);
					}
					
					try {
						int ret = proc.waitFor();
						log.info(PROCESS_HAS_TERMINATED_WITH_EXIT_CODE, h.pid(), ret);
					} catch (InterruptedException e) {
						log.error(PROCESS_HAS_BEEN_PREMATURELY_INTERRUPTED, h.pid());
					} finally {
						/*
						 *		Switch K8s liveness and readiness probe status 
						 *		to BROKEN and REFUSING_TRAFFIC 
						 */
						AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);
						AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.BROKEN);
					}
				}
			 }).start();

		} catch (IOException e) {
			log.error(EXECUTION_FAILED, e);
		}
		
    }
}
