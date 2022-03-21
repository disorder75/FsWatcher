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
		timer.schedule(new Publish(fpath), 15000);
		mTimers.put(fpath, timer);
	}
	
	private class Publish extends TimerTask {

		private static final String PUBLISH_FILE_ON_THE_MESSAGE_BROKER = "publish file {} on the message broker";
		private String path;
		
		public Publish(String path) {
			this.path = path;
		}
		
		@Override
		public void run() {
			log.info(PUBLISH_FILE_ON_THE_MESSAGE_BROKER, path);
		}
	}
}