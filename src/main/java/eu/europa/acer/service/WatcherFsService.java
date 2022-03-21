package eu.europa.acer.service;

import java.io.IOException;
import java.nio.file.Path;

public interface WatcherFsService {
	
    /**
     * Register the given directory with the WatchService
     */
    void register(Path dir) throws IOException;
 
    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    void registerAll(final Path start) throws IOException;
    
    /**
     * Creates a WatchService and registers the given directory
     */
    void initWatcher() throws IOException;

    /**
     * Add the given to the monitor WatchService
     */
    void register(Path dir, boolean recursive) throws IOException;

    /**
     * Listen for i/o events from monitored folders 
     */
    void pollAndDispatchEvents() throws IOException;

}