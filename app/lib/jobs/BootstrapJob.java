package lib.jobs;

import play.i18n.Lang;
import play.jobs.Every;
import play.jobs.Job;
import play.test.Fixtures;

@Every("30min")
public class BootstrapJob extends Job {

	@Override
	public void doJob() {
		Fixtures.deleteAllModels();
		Fixtures.loadModels("data-" + Lang.get() + ".yml");
	}
	
}