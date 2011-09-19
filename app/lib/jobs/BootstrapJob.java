package lib.jobs;

import models.Event;
import models.EventType;
import models.User;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class BootstrapJob extends Job {

	@Override
	public void doJob() {
		
		//forces orm to delete first the events
		Fixtures.delete(Event.class);
		Fixtures.delete(EventType.class);
		Fixtures.delete(User.class);

		// just to be sure, now delete all models
		Fixtures.deleteAllModels();
		Fixtures.loadModels("data.yml");
	}
	
}