package eu.europa.acer.fswatcher.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@ComponentScan("eu.europa.acer")
@Slf4j
public class FsWatcherApplication {

	@Value("${spring.application.name}")
	private String appName;
	
	
	public static void main(String[] args) {
		SpringApplication.run(FsWatcherApplication.class, args);
	}
}
