package play.modules.siena;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ddlutils.DatabaseOperationException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Database;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.data.binding.Binder;
import play.db.DB;
import play.db.Model.Property;
import play.db.jpa.JPA;
import play.exceptions.UnexpectedException;
import siena.ClassInfo;
import siena.Generator;
import siena.Id;
import siena.PersistenceManager;
import siena.PersistenceManagerFactory;
import siena.core.PersistenceManagerLifeCycleWrapper;
import siena.gae.GaePersistenceManager;
import siena.jdbc.H2PersistenceManager;
import siena.jdbc.JdbcPersistenceManager;
import siena.jdbc.PostgresqlPersistenceManager;
import siena.jdbc.ddl.DdlGenerator;
import siena.sdb.SdbPersistenceManager;

public class SienaPlugin extends PlayPlugin {
    
    private static PersistenceManager persistenceManager;
    private static DdlGenerator generator;
    
    private SienaEnhancer enhancer = new SienaEnhancer();

    public static PersistenceManager pm() {
        return persistenceManager;
    }
    
    public static String dbType(){
    	for(PlayPlugin plugin : Play.pluginCollection.getEnabledPlugins()) {
            if(plugin.getClass().getSimpleName().equals("GAEPlugin")) {
                return "nosql:gae";
            }
        }
    	
    	final String db = Play.configuration.getProperty("db");
    	
    	if(db != null && db.toLowerCase().equals("sdb")){
    		return "nosql:sdb";
    	}
    	
        final String dbUrl = Play.configuration.getProperty("db.url");
        if((db==null || db=="" ) && (dbUrl == null || dbUrl == "")){
        	throw new UnexpectedException("SienaPlugin : not using GAE requires at least a db=xxx config");
        }
        if((db!=null && db.contains("postgresql")) 
        		|| (dbUrl!=null && dbUrl.contains("postgresql"))){
        	return "sql:postgresql";
        }else if((db!=null && ("mem".equals(db) || "fs".equals(db) || db.contains("h2"))) 
        		|| (dbUrl!=null && dbUrl.contains("h2"))){
        	return "sql:h2:mysql";
        }else {
        	return "sql:mysql";
        }
    }
    
    public boolean useLifecycle(){
    	final String lc = Play.configuration.getProperty("siena.lifecycle");
    	
    	// by default doesn't use lifecycle
    	if(lc == null) {
    		return false;
    	}
    	else {
    		if("true".equals(lc) || "yes".equals(lc)){
    			return true; 
    		}
    		return false;
    	}
    }
    
    @Override
    public void onApplicationStart() {
    	// DISABLES JPA
    	if(JPA.isEnabled()){
    		Play.pluginCollection.disablePlugin(play.db.jpa.JPAPlugin.class);
    	}
        // GAE ?
        /*boolean gae = false;       
        for(PlayPlugin plugin : Play.pluginCollection.getEnabledPlugins()) {
            if(plugin.getClass().getSimpleName().equals("GAEPlugin")) {
                gae = true;
                break;
            }
        }*/

		@SuppressWarnings("rawtypes")
		List<Class> classes = SienaModelUtils.getSienaClasses();

		// determines DB type
		final String dbType = dbType();
        
        // DDL is for SQL and not in prod mode
        if(dbType.startsWith("sql")) {        	
        	// JDBC
        	String ddlType = "mysql";
        	// initializes DDL Generator
			Connection connection = new PlayConnectionManager().getConnection();

			Logger.debug("Siena DB Type: %s", dbType);
			final String db = Play.configuration.getProperty("db");
            final String dbUrl = Play.configuration.getProperty("db.url");
            if((db==null || db=="" ) && (dbUrl == null || dbUrl == "")){
            	throw new UnexpectedException("SienaPlugin : not using GAE requires at least a db config");
            }
            if(dbType.contains("postgresql")){
            	persistenceManager = new PostgresqlPersistenceManager(new PlayConnectionManager(), null);
            	ddlType = "postgresql";
            	generator = new DdlGenerator("postgresql");
            }else if(dbType.contains("h2")){
            	// the H2 dbMode in Play is "mysql" 
            	persistenceManager = new H2PersistenceManager(new PlayConnectionManager(), null, "mysql");
            	// the DDL type is mysql because in play the DB is H2 in Mysql mode. 
            	// But the DDLGenerator is wired to h2 
            	// because longvarchar and CLOB is not managed the same way in H2/MYSQL and real MYSQL
            	ddlType = "mysql";
            	generator = new DdlGenerator("h2");
            }
            else {
            	persistenceManager = new JdbcPersistenceManager(new PlayConnectionManager(), null);
            	generator = new DdlGenerator("mysql");
            }
			Logger.debug("Siena DDL Type: %s", ddlType);
			
			// Alter tables before installing
            for(Class<?> c : classes) {
            	// adds classes to the DDL generator
            	generator.addTable(c);
            }
            // get the Database model
			Database database = generator.getDatabase();
	
			Platform platform = PlatformFactory.createNewPlatformInstance(ddlType);
			platform.setDataSource(DB.datasource);
			// siena.ddl can have create/update/ddl
			// if siena.ddl is defined, uses it
			// if not: 
			// in dev mode, will be update by default
			// in prod mode, will be none by default
			if(Play.mode.isDev()){
				String ddl = Play.configuration.getProperty("siena.ddl", "update");
				Logger.debug("Siena DDL dev mode: %s", ddl);
				if ("create".equals(ddl)) {
					Logger.debug("Siena DDL Generator SQL: %s", platform.getCreateModelSql(database, false, false));
					// creates tables and do not drop tables and do not continues on error 
					try {
						platform.createModel(connection, database, false, false);
					}catch(DatabaseOperationException ex){
						Logger.warn("Siena DDL createTables generated exception:%s", ex.getCause()!=null?ex.getCause():ex.getMessage());
					}
				}else if("update".equals(ddl)){
					Database currentDatabase = platform.readModelFromDatabase(connection, ddlType);
					Logger.debug("Siena DDL Generator SQL: %s", platform.getAlterModelSql(currentDatabase, database));
					// alters tables and continues on error 
					platform.alterModel(currentDatabase, database, true);
				}
			}
			else if(Play.mode.isProd()){
				String ddl = Play.configuration.getProperty("siena.ddl", "none");
				Logger.debug("Siena DDL prod mode: %s", ddl);
				if ("create".equals(ddl)) {
					Logger.debug("Siena DDL Generator SQL: %s", platform.getCreateModelSql(database, false, false));
					// creates tables and do not drop tables and do not continues on error 
					try {
						platform.createModel(connection, database, false, false);
					}catch(DatabaseOperationException ex){
						Logger.warn("Siena DDL createTables generated exception:%s", ex.getCause()!=null?ex.getCause():ex.getMessage());
					}
				}else if("update".equals(ddl)){
					Database currentDatabase = platform.readModelFromDatabase(connection, null);
					Logger.debug("Siena DDL Generator SQL: %s", platform.getAlterModelSql(currentDatabase, database));
					// alters tables and continues on error 
					platform.alterModel(currentDatabase, database, true);
				}
			}
			
			// activate lifecycle or not
			if(useLifecycle()){
				Logger.debug("Siena activating lifecycle management");
				persistenceManager = new PersistenceManagerLifeCycleWrapper(persistenceManager);
			}

			// is it required ?
			// connection.close();
            persistenceManager.init(null);
			                    
        } else if(dbType.equals("nosql:gae")) {
			Logger.debug("Siena DB Type: GAE");
            persistenceManager = new GaePersistenceManager();
			
            // activate lifecycle or not
			if(useLifecycle()){
				Logger.debug("Siena activating lifecycle management");
				persistenceManager = new PersistenceManagerLifeCycleWrapper(persistenceManager);
			}

			persistenceManager.init(null);
        }
        else if(dbType.equals("nosql:sdb")) {
			Logger.debug("Siena DB Type: SDB");
            persistenceManager = new SdbPersistenceManager();

            String awsAccessKeyId = Play.configuration.getProperty("siena.aws.accesskeyid");            
            String awsSecretAccessKey = Play.configuration.getProperty("siena.aws.secretaccesskey");
            String prefix = Play.configuration.getProperty("siena.aws.prefix", "siena_devel_");
            String consistentread = Play.configuration.getProperty("siena.aws.consistentread", "true");

            if(awsAccessKeyId == null || awsSecretAccessKey == null){
            	throw new UnexpectedException("siena.aws.accesskeyid & siena.aws.secretaccesskey required in conf");
            }
            
            Properties p = new Properties();
            p.setProperty("implementation", "siena.sdb.SdbPersistenceManager");
            p.setProperty("awsAccessKeyId", awsAccessKeyId);
            p.setProperty("awsSecretAccessKey", awsSecretAccessKey);
            p.setProperty("prefix", prefix);

            
            // activate lifecycle or not
			if(useLifecycle()){
				Logger.debug("Siena activating lifecycle management");
				persistenceManager = new PersistenceManagerLifeCycleWrapper(persistenceManager);
			}

            persistenceManager.init(p);
            
            if(consistentread.toLowerCase().equals("true")){
            	persistenceManager.option(SdbPersistenceManager.CONSISTENT_READ);
            }
        }

        // Install all classes in PersistenceManager
        for(Class<?> c : classes) {
        	// installs it into the PM
            PersistenceManagerFactory.install(persistenceManager, c);
        }

    }
    
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object bind(String name, Class clazz, java.lang.reflect.Type type, Annotation[] annotations, Map<String, String[]> params) 
    {
        if (siena.ClassInfo.isModel(clazz)) {
            String keyName = SienaModelUtils.keyName(clazz);
            String idKey = name + "." + keyName;
            if (params.containsKey(idKey) && params.get(idKey).length > 0 && params.get(idKey)[0] != null && params.get(idKey)[0].trim().length() > 0) {
            	Field idField = SienaModelUtils.keyField(clazz);
            	Id idAnn = idField.getAnnotation(Id.class);
    			if(idAnn != null && idAnn.value() == Generator.AUTO_INCREMENT) {
    				// ONLY long ID can be auto_incremented
	                String id = params.get(idKey)[0];
	                try {
	                    siena.Query<?> query = pm().createQuery(clazz).filter(keyName, 
	                    		play.data.binding.Binder.directBind(name, annotations, id + "", SienaModelUtils.keyType(clazz)));
	                    Object o = query.get();
	                    return SienaModelUtils.edit(o, name, params, annotations);
	                } catch (Exception e) {
	                    throw new UnexpectedException(e);
	                }
    			}
            }
            return SienaModelUtils.create(clazz, name, params, annotations);
        }
        return super.bind(name, clazz, type, annotations, params);
    }

    @Override
    public Object bind(String name, Object o, Map<String, String[]> params) {
        if (siena.ClassInfo.isModel(o.getClass())) {
            return SienaModelUtils.edit(o, name, params, null);
        }
        return null;
    }
    
    @Override
    public void enhance(ApplicationClass applicationClass) throws Exception {
        enhancer.enhanceThisClass(applicationClass);
    }
    
	@Override
    public play.db.Model.Factory modelFactory(Class<? extends play.db.Model> modelClass) {
    	if(ClassInfo.isModel(modelClass)){
    		return new SienaModelLoader(modelClass);
    	}
    	return null;
    }
	
	public static SienaModelLoader sienaModelFactory(Class<?> modelClass){
		if(ClassInfo.isModel(modelClass)){
    		return new SienaModelLoader(modelClass);
    	}
    	return null;
	}
    
    public static class SienaModelLoader implements play.db.Model.Factory {
    	private Class<?> clazz;
    	private ClassInfo sienaInfo;
    	
    	public SienaModelLoader(Class<?> clazz) {
            this.clazz = clazz;
            this.sienaInfo = ClassInfo.getClassInfo(clazz);
        }
    	
    	@Override
		public play.db.Model findById(Object id) {
    		if (id == null) {
                return null;
            }
    		try {
                return new ModelWrapper(
                		pm().getByKey(clazz, Binder.directBind(id.toString(), keyType())));
            } catch (Exception e) {
                // Key is invalid, thus nothing was found
                return null;
            }
		}
    	
        @Override
        public String keyName() {
            Field f = keyField();
            return (f == null) ? null : f.getName();
        }

        @Override
        public Class<?> keyType() {
            return keyField().getType();
        }

        @Override
        public Object keyValue(play.db.Model m) {
            return SienaModelUtils.keyValue(m);
        }

        //
        Field keyField() {
            return sienaInfo.getIdField();
        }


		@Override
		public List<play.db.Model> fetch(int offset, int size, 
				String orderBy,	String order, 
				List<String> searchFields, String keywords, String where) {		
			// maps the siena models to play models
			// it's a bit brutal but it allows using simple Siena models everywhere
			List<play.db.Model> playModels = new ArrayList<play.db.Model>();
			for(Object obj:
					SienaModelUtils.fetch(pm(), clazz, offset, size, 
							orderBy, order, searchFields, keywords, where))
			{
				playModels.add(new ModelWrapper(obj));
			}
			
			return playModels;
		}

		@Override
		public Long count(List<String> searchFields, String keywords, String where) {
			return SienaModelUtils.count(pm(), clazz, searchFields, keywords, where);
		}

		@Override
		public void deleteAll() {
			SienaModelUtils.deleteAll(pm(), clazz);
		}

		@Override
		public List<Property> listProperties() {
			return SienaModelUtils.listProperties(pm(), clazz);
		}
    }
}
