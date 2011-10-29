package lib.jobs;

import play.i18n.Lang;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.siena.SienaFixtures;

@OnApplicationStart
public class BootstrapJob extends Job {

	@Override
	public void doJob() {
		SienaFixtures.deleteAllModels();
		SienaFixtures.loadModels("data-" + Lang.get() + ".yml");
	}
	
}