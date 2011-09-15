/**
 * 
 */
package play.modules.siena;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import play.Logger;
import play.Play;
import play.data.binding.BeanWrapper;
import play.data.binding.Binder;
import play.data.validation.Validation;
import play.db.Model;
import play.db.Model.Property;
import play.exceptions.UnexpectedException;
import siena.ClassInfo;
import siena.Filter;
import siena.Json;
import siena.PersistenceManager;
import siena.Query;
import siena.SienaException;
import siena.embed.Embedded;

import com.google.gson.JsonParseException;
import com.mysql.jdbc.Util;

/**
 * Static functions to create/edit a Siena model
 * 
 * @author Pascal Voitot <pascal.voitot@mandubian.org>
 *
 */
public class SienaModelUtils {
	private static final long serialVersionUID = 949918995355310821L;

	public static <T> T create(Class<T> type, String name,
			Map<String, String[]> params) {
		return (T) create(type, name, params, new Annotation[0]);
	}
	
	public static <T> T create(Class<T> type, String name,
			Map<String, String[]> params, Annotation[] annotations) {
		T model = siena.Util.createObjectInstance(type);
		return (T) edit(model, name, params, annotations);
	}
	
	
	public static Field keyField(Class<?> clazz) {
        return ClassInfo.getClassInfo(clazz).getIdField();
    }
	
	public static Class<?> keyType(Class<?> clazz) {
		Field f = keyField(clazz);
        return (f == null) ? null : f.getType();
    }
	
	public static String keyName(Class<?> clazz) {
        Field f = keyField(clazz);
        return (f == null) ? null : ClassInfo.getSimplestColumnName(f);
    }
	
	public static Object keyValue(Object obj) {
		if(obj == null) return null;
        Field k = keyField(obj.getClass());
        return null != k ? siena.Util.readField(obj, k) : null;
    }
	
	public static <T> T edit(T o, String name, Map<String, String[]> params, Annotation[] annotations) {
		try {
			BeanWrapper bw = new BeanWrapper(o.getClass());
			// Start with relations
			Class<?> spClazz = o.getClass();
			/*Set<Field> fields = new HashSet<Field>();
			while (spClazz!=null) {
				Collections.addAll(fields, spClazz.getDeclaredFields());
				spClazz = spClazz.getSuperclass();
			}*/
			for (Field field : ClassInfo.getClassInfo(spClazz).allExtendedFields) {
				boolean isEntity = false;
				boolean isJson = false;
				String relation = null;
				boolean multiple = false;
				String owner = null;
				Class<?> clazz = field.getType();

				// ONE TO MANY or ONE TO ONE association
				if(ClassInfo.isModel(clazz)) {
					isEntity = true;
					relation = clazz.getName();
				}

				// MANY TO ONE association
				// type QUERY<T> + annotation @Filter 
				else if(siena.Query.class.isAssignableFrom(clazz)){
					isEntity = true;
					multiple = true;
					Class<?> fieldType = 
						(Class<?>) ((ParameterizedType) 
								field.getGenericType()).getActualTypeArguments()[0];
					relation = fieldType.getName();
					owner = field.getAnnotation(Filter.class).value();
					// by default, takes the type of the parent entity in lower case
					if(owner == null || "".equals(owner)){
						owner = o.getClass().getName().toLowerCase();
					}
				}
				else if(Json.class.isAssignableFrom(clazz)){
					isJson = true;
				}
				else if(field.isAnnotationPresent(Embedded.class)){
					if(List.class.isAssignableFrom(clazz)){
						multiple = true;
	            	}
					else if(Map.class.isAssignableFrom(clazz)){
						multiple = true;
	            	}
	            	else {
	            		multiple = false;
	            	}
				}
				else if(byte[].class.isAssignableFrom(clazz)
						/*|| Blob.class.isAssignableFrom(field.getType())*/)
				{
					// if params is present but empty, resets the older value
					@SuppressWarnings("unused")
					String[] posted = params.get(name + "." + field.getName());
					// TODO
					@SuppressWarnings("unused")
					Object val = field.get(o);	
					//params.put(name + "." + field.getName(), val);
				}
				
				if (isEntity) {
					// builds entity list for many to one
					if (multiple) {
						// retrieves list to synchronize new and removed objects
						Query<?> q = (Query<?>)siena.Util.readField(o, field);
						// no limitation for the time being
						List<?> relObjs = q.fetch();

						@SuppressWarnings("unchecked")
						Class<? extends siena.Model> relClass = (Class<? extends siena.Model>)Play.classloader.loadClass(relation);
						String idName = keyName(relClass);
						String[] ids = params.get(name + "." + field.getName() + "@"+idName);
						if(ids == null) {
							ids = params.get(name + "." + field.getName() + "."+idName);
						}

						if (ids != null) {							
							params.remove(name + "." + field.getName() + "."+idName);
							params.remove(name + "." + field.getName() + "@"+idName);
							
							Field ownerField = siena.Util.getField(relClass, owner);
							for (String _id : ids) {
								if (_id.equals("")) {
									continue;
								}
								Object idVal = Binder.directBind(_id, keyType(relClass));
								
								// verifies the field is not already owned by the object
								// if yes, no need to resave it with this owner
								boolean b = false;
								for(Object relObj:relObjs){
									Object keyRelObj = keyValue(relObj);
									if(keyRelObj != null && keyRelObj.equals(idVal)){
										relObjs.remove(relObj);
										b = true;
										break;
									}
								}
								if(!b){
									siena.Model res = 
										siena.Model.all(relClass)
											.filter(idName, idVal)
											.get();
									if(res!=null){
										// sets the object to the owner field into the relation entity
										
										if(ownerField == null) {
											throw new UnexpectedException("In related Model "+relClass.getName()+" owner field '"+owner+"' not found");
										}
										siena.Util.setField(res, ownerField, o);
										res.save();
									}
										
									else Validation.addError(name+"."+field.getName(), "validation.notFound", _id);
								}
								
							}
							// now remaining objects have to be unowned
							for(Object relObj:relObjs){
								siena.Util.setField(relObj, ownerField, null);
								SienaPlugin.pm().save(relObj);
							}

							// can't set arraylist to Query<T>
							// bw.set(field.getName(), o, l);
						}
					}
					// builds simple entity for simple association
					else {
						@SuppressWarnings("unchecked")
						Class<? extends siena.Model> relClass = (Class<? extends siena.Model>)Play.classloader.loadClass(relation);
						String idName = keyName(relClass);
						String[] ids = params.get(name + "." + field.getName() + "@"+idName);
						if(ids == null) {
							ids = params.get(name + "." + field.getName() + "."+idName);
						}
						if (ids != null && ids.length > 0 && !ids[0].equals("")) {
							params.remove(name + "." + field.getName() + "."+idName);
							params.remove(name + "." + field.getName() + "@"+idName);

							siena.Model res = 
								siena.Model.all(relClass)
									.filter(idName, Binder.directBind(ids[0], keyType(relClass)))
									.get();
							if(res!=null)
								bw.set(field.getName(), o, res);
							else Validation.addError(name+"."+field.getName(), "validation.notFound", ids[0]);

						} else if(ids != null && ids.length > 0 && ids[0].equals("")) {
							bw.set(field.getName(), o , null);
							params.remove(name + "." + field.getName() + "."+idName);
							params.remove(name + "." + field.getName() + "@"+idName);
						}
					}	                	
				}
				else if(isJson){
					String[] jsonStr = params.get(name + "." + field.getName());
					if (jsonStr != null && jsonStr.length > 0 && !jsonStr[0].equals("")) {
						try {
							//com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
							//parser.parse(jsonStr[0]);
							
							Json json = Json.loads(jsonStr[0]);
							if(json!=null){
								bw.set(field.getName(), o, json);
								params.remove(name + "." + field.getName());
							}
							else Validation.addError(name+"."+field.getName(), "validation.notParsable");
						}catch(JsonParseException ex){
							ex.printStackTrace();
							Logger.error("json parserdelete exception:%s", 
									ex.getCause()!=null?ex.getCause().getMessage(): ex.getMessage());
							Validation.addError(
									name+"."+field.getName(), 
									"validation.notParsable", 
									ex.getCause()!=null?ex.getCause().getMessage(): ex.getMessage());
						}catch(SienaException ex){
							ex.printStackTrace();
							Logger.error("json parserdelete exception:%s", 
									ex.getCause()!=null?ex.getCause().getMessage(): ex.getMessage());
							Validation.addError(
									name+"."+field.getName(), 
									"validation.notParsable", 
									ex.getCause()!=null?ex.getCause().getMessage(): ex.getMessage());
						}
						catch(IllegalArgumentException ex){
							ex.printStackTrace();
							Logger.error("json parser exception:%s", 
									ex.getCause()!=null?ex.getCause().getMessage(): ex.getMessage());
							Validation.addError(
									name+"."+field.getName(), 
									"validation.notParsable", 
									ex.getCause()!=null?ex.getCause().getMessage(): ex.getMessage());
						}
					}
				}	
			}
			// Then bind
			// all composites objects (simple entity, list and maps) are managed
			// by this function
			// v1.0.x code
			// bw.bind(name, o.getClass(), params, "", o);

			// v1.1 compliant
			bw.bind(name, (Type)o.getClass(), params, "", o, o.getClass().getAnnotations());
			
			return (T) o;
		} catch (Exception e) {
			throw new UnexpectedException(e);
		}
	}

	
	@SuppressWarnings({ "rawtypes" })
	public static List<Class> getSienaClasses(){
		// we must at list try to find the siena classes...
        // the classes extending siena.Model 
		List<Class> classes = Play.classloader.getAssignableClasses(siena.Model.class);
        // the classes having @Entity        
        List<Class> entityClasses = Play.classloader.getAnnotatedClasses(siena.Entity.class);
        
        // uses a set to prevent duplicate classes easily
        // adds only the classes not yet in the list (removes those who have both conditions)
        
        for(Class<?> cl: entityClasses){
        	if(!classes.contains(cl)){
        		classes.add(cl);
        	}
        }
        
        return classes;
	}
	
	public static List<Property> listProperties(final PersistenceManager pm, final Class<?> clazz) {
		List<Model.Property> properties = new ArrayList<Model.Property>();
        Set<Field> fields = new LinkedHashSet<Field>();
        // can't use classInfo.allFields as we need also Query fields
        // TODO superclass fields?
        /*for(Field f:clazz.getDeclaredFields()){
        	if(f.getType() == Class.class ||
        			(f.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT ||
        			(f.getModifiers() & Modifier.STATIC) == Modifier.STATIC ||
        			f.isSynthetic()) 
        	{         
        		continue;
        	}
        	fields.add(f);
        }*/

        for (Field f : ClassInfo.getClassInfo(clazz).allExtendedFields) {
            Model.Property mp = buildProperty(f, pm);
            if (mp != null) {
                properties.add(mp);
            }
        }
        return properties;
	}
	
	public static Model.Property buildProperty(final Field field, final PersistenceManager pm) {
        Model.Property modelProperty = new Model.Property();
        final Class<?> clazz = field.getType();
        
        modelProperty.type = clazz;
        modelProperty.field = field;
        // ONE-TO-ONE / MANY-TO-ONE
        if (ClassInfo.isModel(clazz)) {
        	modelProperty.isRelation = true;
            modelProperty.relationType = clazz;
            modelProperty.choices = new Model.Choices() {

                @SuppressWarnings("unchecked")
                public List<Object> list() {
                	return (List<Object>)pm.createQuery(clazz).fetch();
                }
            };
        }
        // AUTOMATIC QUERY
        // ONE-TO-MANY
        if (Query.class.isAssignableFrom(clazz)) {
            final Class<?> fieldType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            
            modelProperty.isRelation = true;
            modelProperty.isMultiple = true;
            modelProperty.relationType = fieldType;
            modelProperty.choices = new Model.Choices() {
            	@SuppressWarnings("unchecked")
            	public List<Object> list() {
            		return (List<Object>)pm.createQuery(fieldType).fetch();
            	}
            };
        }
        
        // ENUM
        if (clazz.isEnum()) {
            modelProperty.choices = new Model.Choices() {
                @SuppressWarnings("unchecked")
                public List<Object> list() {
                    return (List<Object>) Arrays.asList(clazz.getEnumConstants());
                }
            };
        }
        
        // JSON
        if (Json.class.isAssignableFrom(clazz)) {
            modelProperty.type = String.class;
        }

        if (field.isAnnotationPresent(Embedded.class)) {
        	if(List.class.isAssignableFrom(clazz)){
        		final Class<?> fieldType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        		
        		modelProperty.isRelation = true;
                modelProperty.isMultiple = true;
                modelProperty.relationType = fieldType;
        	}
        	else if(Map.class.isAssignableFrom(clazz)){
        		// gets T2 for map<T1,T2>
        		final Class<?> fieldType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
        		modelProperty.isRelation = true;
                modelProperty.isMultiple = true;
                modelProperty.relationType = fieldType;
        	}
        	else {
        		modelProperty.isRelation = true;
        		modelProperty.isMultiple = false;
        		modelProperty.relationType = clazz;
        	}
        }
        
        modelProperty.name = field.getName();
        if (clazz.equals(String.class)) {
            modelProperty.isSearchable = true;
        }
        if(ClassInfo.isGenerated(field)){
        	modelProperty.isGenerated = true;
        }
        return modelProperty;
    }
	
	public static <T> T findById(Class<T> clazz, Object id) {
		if (id == null) {
            return null;
        }
		try {
			// needs to manage UUID because directBind doesn't do it
			Class<?> kt = keyType(clazz);
			if(kt == UUID.class){
				return SienaPlugin.pm().getByKey(clazz, UUID.fromString(id.toString()));
			}else {
				return SienaPlugin.pm().getByKey(clazz, 
            		Binder.directBind(id.toString(), keyType(clazz)));
			}
        } catch (Exception e) {
            // Key is invalid, thus nothing was found
            return null;
        }
	}
	
	/*
     * code directly inspired from Morphia Play Plugin 
     * https://github.com/greenlaw110/play-morphia
     * 
     * Support the following syntax at the moment: property = 'val' property
     * in ('val1', 'val2' ...) prop1 ... and prop2 ...
     */
	public static <T> void processWhere(Query<T> q, String where) {
        if (null != where) {
            where = where.trim();
        } else {
            where = "";
        }
        if ("".equals(where) || "null".equalsIgnoreCase(where))
            return;
        
        String[] propValPairs = where.split("(and|&&)");
        for (String propVal : propValPairs) {
            if (propVal.contains("=")) {
                String[] sa = propVal.split("=");
                if (sa.length != 2) {
                    throw new IllegalArgumentException(
                            "invalid where clause: " + where);
                }
                String prop = sa[0];
                String val = sa[1];
                Logger.trace("where prop val pair found: %1$s = %2$s",
                        prop, val);
                prop = prop.replaceAll("[\"' ]", "");
                if (val.matches("[\"'].*[\"']")) {
                    // string value
                    val = val.replaceAll("[\"' ]", "");
                    q.filter(prop, val);
                } else {
                    // possible string, number or boolean value
                    if (val.matches("[-+]?\\d+\\.\\d+")) {
                        q.filter(prop, Float.parseFloat(val));
                    } else if (val.matches("[-+]?\\d+")) {
                        q.filter(prop, Integer.parseInt(val));
                    } else if (val
                            .matches("(false|true|FALSE|TRUE|False|True)")) {
                        q.filter(prop, Boolean.parseBoolean(val));
                    } else {
                        q.filter(prop, val);
                    }
                }
            } else if (propVal.contains(" in ")) {
                String[] sa = propVal.split(" in ");
                if (sa.length != 2) {
                    throw new IllegalArgumentException(
                            "invalid where clause: " + where);
                }
                String prop = sa[0].trim();
                String val0 = sa[1].trim();
                if (!val0.matches("\\(.*\\)")) {
                    throw new IllegalArgumentException(
                            "invalid where clause: " + where);
                }
                val0 = val0.replaceAll("[\\(\\)]", "");
                String[] vals = val0.split(",");
                List<Object> l = new ArrayList<Object>();
                for (String val : vals) {
                    // possible string, number or boolean value
                    if (val.matches("[-+]?\\d+\\.\\d+")) {
                        l.add(Float.parseFloat(val));
                    } else if (val.matches("[-+]?\\d+")) {
                        l.add(Integer.parseInt(val));
                    } else if (val
                            .matches("(false|true|FALSE|TRUE|False|True)")) {
                        l.add(Boolean.parseBoolean(val));
                    } else {
                        l.add(val);
                    }
                }
                q.filter(prop + " IN ", l);
            } else {
                throw new IllegalArgumentException("invalid where clause: "
                        + where);
            }
        }
    }
	
	public static <T> List<T> fetch(PersistenceManager pm, Class<T> clazz, 
			int offset, int size, 
			String orderBy,	String order, 
			List<String> searchFields, String keywords, String where) {
		Query<T> q = pm.createQuery(clazz);		
		Field keyField = keyField(clazz);
		// ORDER
		if(orderBy == null) {
			if (order == null) {
				q.order(keyField.getName());
			}
			else {
				if(order.equals("+")){
					q.order(keyField.getName());
				}
				else if(order.equals("-")){
					q.order(order+keyField.getName());
				}
				else if(order.equals("ASC")){
					q.order(keyField.getName());
				}	
				else if(order.equals("DESC")){
					q.order("-"+keyField.getName());
				}
				else {
					q.order(keyField.getName());
				} 
			}
		}
		else {
			if (order == null) {
				q.order(orderBy);
			}
			else {
				if(order.equals("+")){
					q.order(orderBy);
				}else if(order.equals("-")){
					q.order(order+orderBy);
				}
				else if(order.equals("ASC")){
					q.order(orderBy);
				}
				else if(order.equals("DESC")){
					q.order("-"+orderBy);
				}
				else {
					q.order(orderBy);
				} 
			}
		}
		
		// SEARCH
		// TODO define the search strings
		if(keywords != null){
			if(searchFields != null && searchFields.size() != 0){
				q.search(keywords, (String[])searchFields.toArray());
			}else{
				ClassInfo ci = ClassInfo.getClassInfo(clazz);
				String[] strs = new String[ci.allFields.size()];
				int i=0;
				for(Field f : ClassInfo.getClassInfo(clazz).allFields){
					strs[i++] = f.getName();
				}
				q.search(keywords, strs);
			}
		}
		
		// WHERE
		processWhere(q, where);
		
		return q.fetch(size, offset);
	}

	public static <T> Long count(PersistenceManager pm, Class<T> clazz, 
			List<String> searchFields, 
			String keywords, String where) {
		Query<T> q = pm.createQuery(clazz);
		
		// SEARCH
		// TODO define the search strings
		if(keywords != null){
			if(searchFields != null && searchFields.size() != 0){
				q.search(keywords, (String[])searchFields.toArray());
			}else{
				ClassInfo ci = ClassInfo.getClassInfo(clazz);
				String[] strs = new String[ci.allFields.size()];
				int i=0;
				for(Field f : ClassInfo.getClassInfo(clazz).allFields){
					strs[i++] = f.getName();
				}
				q.search(keywords, strs);
			}
		}
		
		// WHERE
		processWhere(q, where);
		
		return new Long(q.count());
	}

	public static <T> void deleteAll(PersistenceManager pm, Class<T> clazz) {
		pm.createQuery(clazz).delete();
	}
}
