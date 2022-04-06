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
@ConfigurationProperties(prefix = "processbuilder")
@Slf4j
@Data
public class ProcessBuilderConfig {
	
	private static final String PBUILDER_CFG = "process builder will try to execute {}";
	
	private List<String> args = new ArrayList<>();
		
	@PostConstruct
    public void showConfig() {
		
		String line = String.join(" ", args);
		log.info(PBUILDER_CFG, line);
		String[] lines = line.split(" ");
		args = List.of(lines);

    }
}