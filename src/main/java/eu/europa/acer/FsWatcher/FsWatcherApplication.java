package eu.europa.acer.FsWatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class FsWatcherApplication {

	@Value("${spring.application.name}")
	private String appName;
	
	Logger logger = LoggerFactory.getLogger(FsWatcherApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(FsWatcherApplication.class, args);
	}
	
	/**
	 * Empty method just to keep alive the application on the initial build.
	 * Will be removed asap.
	 * 
	 * @throws Exception
	 */
	@Scheduled(fixedRate = 10000)
	public void logMe() throws Exception {
		logger.info("[th {}] {}: i'm alive", Thread.currentThread().getId(), appName);
	}

}
