package eu.europa.acer.runner;

import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import eu.europa.acer.fswatcher.config.WatcherConfig;
import eu.europa.acer.service.WatcherFsService;
import lombok.extern.slf4j.Slf4j;

/**
 * DispatcherHandler.
 *	- Register for filesystem events
 *	- Routes the events to the right queue 
 */
@Service
@Slf4j
public class DispatcherHandler implements CommandLineRunner {

    private static final String START_WATCHER_SERVICE = "start WatcherService";

    @Autowired
	private WatcherFsService watcherFsService;
	    
	@Override
	public void run(String... args) throws Exception {
		log.info(START_WATCHER_SERVICE);
		watcherFsService.pollAndDispatchEvents();
    }
}
