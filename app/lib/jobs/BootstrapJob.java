package lib.jobs;

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