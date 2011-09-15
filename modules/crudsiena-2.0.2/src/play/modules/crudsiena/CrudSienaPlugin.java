package play.modules.crudsiena;

import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;

public class CrudSienaPlugin extends PlayPlugin {
	
    private CrudSienaEnhancer enhancer = new CrudSienaEnhancer();
	
    /**
     * Enhance this class
     * @param applicationClass
     * @throws java.lang.Exception
     */
    public void enhance(ApplicationClass applicationClass) throws Exception {
    	enhancer.enhanceThisClass(applicationClass);
    }
}
