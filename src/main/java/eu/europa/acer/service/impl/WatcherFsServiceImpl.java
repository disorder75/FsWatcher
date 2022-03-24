package eu.europa.acer.service.impl;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.europa.acer.fswatcher.config.WatcherConfig;
import eu.europa.acer.service.PublishService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 	Filesystem events watcher
 *	@see Oracle documentation https://docs.oracle.com/javase/tutorial/essential/io/notification.html
 */
@Service
@Slf4j
@Data
public class WatcherFsServiceImpl implements eu.europa.acer.service.WatcherFsService {

	private static final String UNSUPPORTED_EVENT = "Unsupported event {}";
	private static final String IGNORING_EVENT_ON_UNKNOWN_TYPE_FILE = "ignoring event on unknown type file {}";
	private static final String RECEIVED_EVENT = "received event {}: {}";
	private static final String FAILED_TO_MONITOR_THE_NEW_CREATED_RESOURCE = "Failed to monitor the new (created) resource {}";
	private static final String OVERFLOW_EVENTS_FROM_FILESYSTEM = "overflow events from filesystem";
	private static final String INVALID_WATCH_KEY = "Invalid WatchKey";
	private static final String FAILED_TO_MONITOR_PATH = "failed to monitor path {}";
	private static final String CONFIGURATION_DONE_ON = "Configuration done on {}";
	private static final String SCANNING = "Scanning {} ...";
	private static final String FOLDER_ALREADY_UNDER_MONITORING = "folder already under monitoring";
	private static final String UPDATE_FOLDER_MONITORING = "update folder monitoring: {} -> {}";
	private static final String REGISTER_FOLDER = "register: {}";
	
	private WatchService watcher;
	private boolean trace = false;
	private Map<WatchKey,Path> keys;
	private boolean recursive;
	
	@Autowired
	private WatcherConfig watcherConfig;
	@Autowired
	private PublishService publishService;

	/**
	 *	 Register the given directory with the WatchService
	 */
	@Override
	public void register(Path dir) throws IOException {
	     WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
	     if (trace) {
	         Path prev = keys.get(key);
	         if (prev == null)
	             log.info(REGISTER_FOLDER, dir);
	         else if (!dir.equals(prev))
	        	 log.info(UPDATE_FOLDER_MONITORING, prev, dir);
	         else
	        	 log.info(FOLDER_ALREADY_UNDER_MONITORING);

	     }
	     keys.put(key, dir);
	}
	
	/**
	 *	Register the given directory, and all its sub-directories, with the
	 *	WatchService.
	 */
	@Override
	public void registerAll(Path start) throws IOException {
	     // register directory and sub-directories
	     Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
	         @Override
	         public FileVisitResult preVisitDirectory(Path dir, 
	        		 								  BasicFileAttributes attrs) throws IOException {
	             register(dir);
	             return FileVisitResult.CONTINUE;
	         }
	     });
	}

	@Override
	public void pollAndDispatchEvents() throws IOException {

		recursive = watcherConfig.getRecursive();
		
		// init
		initWatcher();
		
		// configure folders
		watcherConfig.getFolders().forEach(f -> {
													Path path = Path.of(f);
													try {
														register(path, recursive);
													} catch (IOException e) {
														log.error(FAILED_TO_MONITOR_PATH, f, e);
													}
												});
		// poll & dispatch
		while (true) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				log.warn(INVALID_WATCH_KEY);
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				Kind<?> kind = event.kind();

				// losing events from fs!
				if (kind == OVERFLOW) {
					log.error(OVERFLOW_EVENTS_FROM_FILESYSTEM);
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				//log.debug(RECEIVED_EVENT, event.kind().name(), child);

				if (kind == ENTRY_CREATE) {
					try {
						if (recursive && Files.isDirectory(child, NOFOLLOW_LINKS)) {
							/*
							 *		Add the folders to the monitored resources 
							 */
							registerAll(child);
						} else if (Files.isRegularFile(child)) {
							/*
							 *		Deliver to Rabbit (first time)
							 */
							publishService.publish(child.toString());
						} else {
							log.warn(IGNORING_EVENT_ON_UNKNOWN_TYPE_FILE, child);
						}
					} catch (IOException x) {
						log.error(FAILED_TO_MONITOR_THE_NEW_CREATED_RESOURCE, child.toString());
					}
				} else if (kind == ENTRY_MODIFY && Files.isRegularFile(child)) {
					/*
					 *		Deliver to Rabbit (file updated or chunked)
					 */
					publishService.publish(child.toString());
				} else if (kind == ENTRY_DELETE) {
					/*
					 *		Delete pending event (if present)
					 */
					publishService.remove(child.toString());
				} else {
					log.warn(UNSUPPORTED_EVENT, kind.name());
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);
				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	@Override
	public void initWatcher() throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
	}

	@Override
	public void register(Path dir, boolean recursive) throws IOException {

		this.recursive = recursive;
		if (recursive) {
			log.info(SCANNING, dir);
			registerAll(dir);
			log.info(CONFIGURATION_DONE_ON, dir);
		} else {
			register(dir);
		}

		// enable trace after initial registration
		this.trace = true;
		
	}

    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

}