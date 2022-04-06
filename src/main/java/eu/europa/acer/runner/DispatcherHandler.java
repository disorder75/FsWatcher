package eu.europa.acer.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import eu.europa.acer.service.WatcherFsService;
import lombok.extern.slf4j.Slf4j;

/**
 * DispatcherHandler.
 *	- Register for filesystem events
 *	- Routes the events to the right queue 
 */
@Component
@Order(2)
@Profile("!test")
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
