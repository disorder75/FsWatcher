package eu.europa.acer.fswatcher.config;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "watcher")
@Slf4j
@Data
public class WatcherConfig {
	
	private static final String MONITORED_FOLDER = "WatcherService will monitor: {}";
	
	private List<String> folders = new ArrayList<>();
	private Boolean recursive;
		
	@PostConstruct
    public void showConfig() {
		folders.forEach(f -> log.info(MONITORED_FOLDER, f));
    }
}