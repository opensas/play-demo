package play.modules.siena;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.scanner.ScannerException;

import play.Logger;
import play.Play;
import play.data.binding.Binder;
import play.data.binding.types.DateBinder;
import play.db.DB;
import play.db.DBPlugin;
import play.db.Model;
import play.exceptions.UnexpectedException;
import play.exceptions.YAMLException;
import play.libs.IO;
import play.templates.TemplateLoader;
import play.vfs.VirtualFile;
import siena.ClassInfo;
import siena.Filter;
import siena.Json;
import siena.embed.Embedded;


public class SienaFixtures {
	
	static Pattern keyPattern = Pattern.compile("([^(]+)\\(([^)]+)\\)");	
    static Map<String, Object> idCache = new HashMap<String, Object>();

    
    public static void executeSQL(String sqlScript) {
        for(String sql: sqlScript.split(";")) {
            if(sql.trim().length() > 0) {
                DB.execute(sql);
            }
        }
    }

    public static void executeSQL(File sqlScript) {
        executeSQL(IO.readContentAsString(sqlScript));
    }

    /**
     * Delete all Model instances for the given types using the underlying persistance mechanisms
     * @param types Types to delete
     */
    public static void delete(Class<?>... types) {
        idCache.clear();
        disableForeignKeyConstraints();
        for (Class<?> type : types) {
            try {
            	if(ClassInfo.isModel(type)){
            		SienaPlugin.pm().createQuery(type).delete();
            	}
            } catch(Exception e) {
                Logger.error(e, "While deleting " + type + " instances");
            }
            
        }
        enableForeignKeyConstraints();
        Play.pluginCollection.afterFixtureLoad();
    }

    /**
     * Delete all Model instances for the given types using the underlying persistance mechanisms
     * @param types Types to delete
     */
    @SuppressWarnings("rawtypes")
	public static void delete(List<Class> classes) {
        delete(classes.toArray(new Class[classes.size()]));
    }

    /**
     * Delete all Model instances for the all available types using the underlying persistance mechanisms
     */
    public static void deleteAllModels() {
        delete(SienaModelUtils.getSienaClasses());
    }

    
    /**
     * Use deleteDatabase() instead
     * @deprecated
     */
    @Deprecated
    public static void deleteAll() {
        deleteDatabase();
    }
    
    static String[] dontDeleteTheseTables = new String[] {"play_evolutions"};
    
    /**
     * Flush the entire JDBC database
     */
    public static void deleteDatabase() {
    	String dbType = SienaPlugin.dbType();
    	// SQL deletes tables
    	if(dbType.startsWith("sql")){    		
	        try {
	            idCache.clear();
	            List<String> names = new ArrayList<String>();
	            
	            // I prefer searching model tables only than all tables which can be technical tables
	            /*ResultSet rs = DB.getConnection().getMetaData().getTables(null, null, null, new String[]{"TABLE"});
	            while (rs.next()) {
	                String name = rs.getString("TABLE_NAME");
	                names.add(name);
	            }*/
	            for (Class<?> c : SienaModelUtils.getSienaClasses()) {
	                ClassInfo ci = ClassInfo.getClassInfo(c);
	                
	                names.add(ci.tableName);
	            }
	            
	            disableForeignKeyConstraints();
	            for (String name : names) {
	                if(Arrays.binarySearch(dontDeleteTheseTables, name) < 0) {
	                    if (Logger.isTraceEnabled()) {
	                        Logger.trace("Dropping content of table %s", name);
	                    }
	                    DB.execute(getDeleteTableStmt(name) + ";");
	                }
	            }
	            enableForeignKeyConstraints();
	            Play.pluginCollection.afterFixtureLoad();
	        } 
	        catch (Exception e) {
	            throw new RuntimeException("Cannot delete all table data : " + e.getMessage(), e);
	        }
    	}
    	// NOSQL doesn't delete tables, just models
    	else {
    		deleteAllModels();
    	}
    }
	


    /**
     * User loadModels(String name) instead
     * @param name
     * @deprecated
     */
    @Deprecated
    public static void load(String name) {
        loadModels(name);
    }

    
	@SuppressWarnings("unchecked")
	public static void loadModels(String name) {
		VirtualFile yamlFile = null;
		try  {
			for (VirtualFile vf : Play.javaPath) {
	            yamlFile = vf.child(name);
	            if (yamlFile != null && yamlFile.exists()) {
	                break;
	            }
	        }
	        if (yamlFile == null) {
	            throw new RuntimeException("Cannot load fixture " + name + ", the file was not found");
	        }
	        
	        // Render yaml file with 
	        String renderedYaml = TemplateLoader.load(yamlFile).render();
	        
			Yaml yaml = new Yaml();
			Object o = yaml.load(renderedYaml);
			if (o instanceof LinkedHashMap<?, ?>) {
				LinkedHashMap<Object, Map<?, ?>> objects = (LinkedHashMap<Object, Map<?, ?>>) o;
				for (Object key : objects.keySet()) {
					Matcher matcher = keyPattern.matcher(key.toString().trim());
					if (matcher.matches()) {
						String type = matcher.group(1);
						String id = matcher.group(2);
						if (!type.startsWith("models.")) {
							type = "models." + type;
						}
						if (idCache.containsKey(type + "-" + id)) {
							throw new RuntimeException("Cannot load fixture " + name + ", duplicate id '" + id + "' for type " + type);
						}
						Map<String, String[]> params = new HashMap<String, String[]>();
						if (objects.get(key) == null) {
							objects.put(key, new HashMap<Object, Object>());
						}
						serialize(objects.get(key), "object", params);
						Class<?> cType = (Class<?>)Play.classloader.loadClass(type);
						resolveDependencies(cType, params);
                        Object model = Binder.bind("object", cType, cType, null, params);
                        
                        // a List of objectx to save in the case of automatic query for ONE2MANY
						@SuppressWarnings("rawtypes")
						List queryObj = new ArrayList();
                                                
                        for(Field f : model.getClass().getFields()) {
							if (f.getType().isAssignableFrom(Map.class)) {
								f.set(model, objects.get(key).get(f.getName()));
							} else if (f.getType().equals(byte[].class)) {
                                f.set(model, objects.get(key).get(f.getName()));
                            } else if(Json.class.isAssignableFrom(f.getType())){
                            	Object obj = objects.get(key).get(f.getName());
                            	Json json = new Json(obj);
    							if(json!=null){
    								f.set(model, json);
    							}
                            } else if(f.isAnnotationPresent(Embedded.class) && List.class.isAssignableFrom(f.getType())){ 
                           		Object obj = objects.get(key).get(f.getName());
                           		f.set(model, obj);
                            } else if(siena.Query.class.isAssignableFrom(f.getType())){
                                final Class<?> fieldType = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
                        		final String ownerFieldName = f.getAnnotation(Filter.class).value();
                            	ArrayList<String> linkedKeys = (ArrayList<String>)objects.get(key).get(f.getName());
                            	
                            	if(linkedKeys != null){
	                            	for(String linkedKey: linkedKeys){
	                            		// be careful: the linked entity type is the fieldType.getName() and not type
	                            		Object linkedId = idCache.get(fieldType.getName() + "-" + linkedKey);
	                            		if(linkedId == null){
	                            			throw new RuntimeException("YAML AutoQuery mapping: linkedObj of type:"+fieldType.getName()+" and id:"+linkedKey+" was not found");
	                            		}
	                            		Object linkedObj = SienaPlugin.pm().getByKey(fieldType, linkedId);
	                            		if(linkedObj != null){
	                            			siena.Util.setField(linkedObj, fieldType.getField(ownerFieldName), model);
	                            			queryObj.add(linkedObj);
	                            		}else {
	                            			throw new RuntimeException("YAML AutoQuery mapping: linkedObj of type:"+fieldType.getName()+" and id:"+linkedKey+" was not found");
	                            		}	                            		
	                            	}
                            	}
                            }
						}
						
                        SienaPlugin.pm().save(model);
                        
                        // saves autoquery objects
                        if(!queryObj.isEmpty()){
                        	SienaPlugin.pm().save(queryObj);
                        }
                        
                        Class<?> tType = cType;
                        while (!tType.equals(Object.class)) {
                            idCache.put(tType.getName() + "-" + id, SienaModelUtils.keyValue(model));
                            tType = tType.getSuperclass();
                        }
					}
				}
			}
			  // Most persistence engine will need to clear their state
            Play.pluginCollection.afterFixtureLoad();
		} catch (ClassNotFoundException e) {
            throw new RuntimeException("Class " + e.getMessage() + " was not found", e);
        } catch (ScannerException e) {
            throw new YAMLException(e, yamlFile);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot load fixture " + name + ": " + e.getMessage(), e);
        }
      
	}
	
	
	   /**
     * User loadModels instead
     * @deprecated
     */
    @Deprecated
    public static void load(String... names) {
        for (String name : names) {
            loadModels(name);
        }
    }

    /**
     * @see loadModels(String name)
     */
    public static void loadModels(String... names) {
        for (String name : names) {
            loadModels(name);
        }
    }

    /**
     * User loadModels instead
     * @deprecated
     */
    public static void load(List<String> names) {
        loadModels(names);
    }

    /**
     * @see loadModels(String name)
     */
    public static void loadModels(List<String> names) {
        String[] tNames = new String[names.size()];
        for (int i = 0; i < tNames.length; i++) {
            tNames[i] = names.get(i);
        }
        load(tNames);
    }

    /**
     * Load and parse a plain YAML file and returns the corresponding Java objects.
     * The YAML parser used is SnakeYAML (http://code.google.com/p/snakeyaml/)
     * @param name Name of a yaml file somewhere in the classpath (or conf/)me
     * @return Java objects
     */
    public static Object loadYaml(String name) {
        return loadYaml(name, Object.class);
    }

    public static List<?> loadYamlAsList(String name) {
        return (List<?>)loadYaml(name);
    }

    public static Map<?,?> loadYamlAsMap(String name) {
        return (Map<?,?>)loadYaml(name);
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadYaml(String name, Class<T> clazz) {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(clazz, Play.classloader));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return (T)loadYaml(name, yaml);
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadYaml(String name, Yaml yaml) {
        VirtualFile yamlFile = null;
        try {
            for (VirtualFile vf : Play.javaPath) {
                yamlFile = vf.child(name);
                if (yamlFile != null && yamlFile.exists()) {
                    break;
                }
            }
            InputStream is = Play.classloader.getResourceAsStream(name);
            if (is == null) {
                throw new RuntimeException("Cannot load fixture " + name + ", the file was not found");
            }
            Object o = yaml.load(is);
            return (T)o;
        } catch (ScannerException e) {
            throw new YAMLException(e, yamlFile);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot load fixture " + name + ": " + e.getMessage(), e);
        }
    }
    

    /**
     * Delete a directory recursively
     * @param path relative path of the directory to delete
     */
    public static void deleteDirectory(String path) {
        try {
            FileUtils.deleteDirectory(Play.getFile(path));
        } catch (IOException ex) {
            throw new UnexpectedException(ex);
        }
    }

    // Private
    
	static void serialize(Map<?, ?> values, String prefix, Map<String, String[]> serialized) {
        for (Object key : values.keySet()) {
            Object value = values.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof Map<?, ?>) {
                serialize((Map<?, ?>) value, prefix + "." + key, serialized);
            } else if (value instanceof Date) {
                serialized.put(prefix + "." + key.toString(), new String[]{new SimpleDateFormat(DateBinder.ISO8601).format(((Date) value))});
            } else if (value instanceof List<?>) {
                List<?> l = (List<?>) value;
                String[] r = new String[l.size()];
                int i = 0;
                for (Object el : l) {
                    r[i++] = el.toString();
                }
                serialized.put(prefix + "." + key.toString(), r);
            } else if (value instanceof String && value.toString().matches("<<<\\s*\\{[^}]+}\\s*")) {
                Matcher m = Pattern.compile("<<<\\s*\\{([^}]+)}\\s*").matcher(value.toString());
                m.find();
                String file = m.group(1);
                VirtualFile f = Play.getVirtualFile(file);
                if (f != null && f.exists()) {
                    serialized.put(prefix + "." + key.toString(), new String[]{f.contentAsString()});
                }
            } else {
                serialized.put(prefix + "." + key.toString(), new String[]{value.toString()});
            }
        }
    }
	
    static void resolveDependencies(Class<?> type, Map<String, String[]> serialized) {
        for (Model.Property field :SienaModelUtils.listProperties(SienaPlugin.pm(), type)) {
            if (field.isRelation) {
            	if(field.field.isAnnotationPresent(Embedded.class) 
            			&& !ClassInfo.isModel(field.field.getType())){
            		continue;
            	}
            	// ONE2ONE & ONE2MANY
                String[] ids = serialized.get("object." + field.name);
                if (ids != null) {
                    for (int i = 0; i < ids.length; i++) {
                        String id = ids[i];
                        id = field.relationType.getName() + "-" + id;
                        if (!idCache.containsKey(id)) {
                            throw new RuntimeException("No previous reference found for object of type " + field.name + " with key " + ids[i]);
                        }
                        ids[i] = idCache.get(id).toString();
                    }
                }
                serialized.remove("object." + field.name);
                serialized.put("object." + field.name + "." + SienaModelUtils.keyName(field.relationType), ids);

            }
        }
    }
    
    private static void disableForeignKeyConstraints() {
        if (DBPlugin.url.startsWith("jdbc:oracle:")) {
            DB.execute("begin\n" +
                    "for i in (select constraint_name, table_name from user_constraints where constraint_type ='R'\n" +
                    "and status = 'ENABLED') LOOP\n" +
                    "execute immediate 'alter table '||i.table_name||' disable constraint '||i.constraint_name||'';\n" +
                    "end loop;\n" +
                    "end;");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:hsqldb:")) {
            DB.execute("SET REFERENTIAL_INTEGRITY FALSE");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:h2:")) {
            DB.execute("SET REFERENTIAL_INTEGRITY FALSE");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:mysql:")) {
            DB.execute("SET foreign_key_checks = 0;");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:postgresql:")) {
            DB.execute("SET CONSTRAINTS ALL DEFERRED");
            return;
        }

        // Maybe Log a WARN for unsupported DB ?
        Logger.warn("Fixtures : unable to disable constraints, unsupported database : " + DBPlugin.url);
    }

    private static void enableForeignKeyConstraints() {
        if (DBPlugin.url.startsWith("jdbc:oracle:")) {
             DB.execute("begin\n" +
                     "for i in (select constraint_name, table_name from user_constraints where constraint_type ='R'\n" +
                     "and status = 'DISABLED') LOOP\n" +
                     "execute immediate 'alter table '||i.table_name||' enable constraint '||i.constraint_name||'';\n" +
                     "end loop;\n" +
                     "end;");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:hsqldb:")) {
            DB.execute("SET REFERENTIAL_INTEGRITY TRUE");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:h2:")) {
            DB.execute("SET REFERENTIAL_INTEGRITY TRUE");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:mysql:")) {
            DB.execute("SET foreign_key_checks = 1;");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:postgresql:")) {
            return;
        }

        // Maybe Log a WARN for unsupported DB ?
        Logger.warn("Fixtures : unable to enable constraints, unsupported database : " + DBPlugin.url);
    }


    static String getDeleteTableStmt(String name) {
        if (DBPlugin.url.startsWith("jdbc:mysql:") ) {
            return "TRUNCATE TABLE " + name;
        } else if (DBPlugin.url.startsWith("jdbc:postgresql:")) {
            return "TRUNCATE TABLE " + name + " cascade";
        } else if (DBPlugin.url.startsWith("jdbc:oracle:")) {
            return "TRUNCATE TABLE " + name;
        }
        return "DELETE FROM " + name;
    }
}
