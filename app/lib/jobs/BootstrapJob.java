package lib.jobs;

import models.Event;
import models.EventType;
import models.User;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

//@OnApplicationStart
public class BootstrapJob extends Job {

	@Override
	public void doJob() {
		Fixtures.deleteAllModels();
		Fixtures.loadModels("data.yml");
	}
	
}