package eu.europa.acer.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.springframework.stereotype.Service;

import eu.europa.acer.service.PublishService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PublishServiceImpl implements PublishService {
	
	private static final String EVENT_ALREADY_DELIVERED_ON_DELETED_FILE = "client has removed the file {}, not found into the events table (already published?)";
	private static final String CLIENT_HAS_DELETED_THE_FILE_DELETE_THE_PENDING_EVENT = "client has deleted the file {}, removing also from the events table";
	private static final String PUBLISH_FILE_ON_THE_MESSAGE_BROKER = "publishing the file {} on the message broker";
	private static final int EVT_WAIT_COMPLETED_MS = 30000;
	
	Map<String, Timer> mTimers = new HashMap<>();
	
	@Override
	public void publish(String fpath) {

		Timer timer = null;

		if (mTimers.containsKey(fpath)) {
			/*
			 *		Re-arm the timer 
			 */
			//log.debug("re-arm timer on file {}", fpath);
			timer = mTimers.get(fpath);
			timer.cancel();
		}
		/*
		 *		Arm the timer 
		 */
		timer = new Timer();
		timer.schedule(new Publish(fpath), EVT_WAIT_COMPLETED_MS);
		//log.debug("scheduled file {} - scheduler id {}", fpath, timer.hashCode());
		mTimers.put(fpath, timer);
	}
	
	private class Publish extends TimerTask {
		
		private String fpath;
		
		public Publish(String fpath) {
			this.fpath = fpath;
		}
		
		@Override
		public void run() {
			log.info(PUBLISH_FILE_ON_THE_MESSAGE_BROKER, fpath);
			mTimers.remove(fpath);
		}
	}

	@Override
	public void remove(String fpath) {
		if (mTimers.containsKey(fpath)) {
			log.warn(CLIENT_HAS_DELETED_THE_FILE_DELETE_THE_PENDING_EVENT, fpath);
			Timer timer = mTimers.get(fpath);
			timer.cancel();
			mTimers.remove(fpath);
		} else
			log.warn(EVENT_ALREADY_DELIVERED_ON_DELETED_FILE, fpath);
	}
}