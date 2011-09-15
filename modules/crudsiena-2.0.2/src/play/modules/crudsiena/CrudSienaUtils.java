package play.modules.crudsiena;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.data.binding.BeanWrapper;
import play.data.binding.Binder;
import play.data.validation.Validation;
import play.exceptions.UnexpectedException;
import play.modules.siena.SienaPlugin;

public class CrudSienaUtils {
	public static <T> T addListElement(T o, String fieldName) {
    	try {
    		Class<?> clazz = o.getClass();
			BeanWrapper bw = new BeanWrapper(o.getClass());
			Field field = clazz.getField(fieldName);
			
			if(List.class.isAssignableFrom(field.getType())){
				List l = (List)field.get(o);
				if(l == null)
					l = new ArrayList();
				
				Class<?> embedClass = 
					(Class<?>) ((ParameterizedType) 
							field.getGenericType()).getActualTypeArguments()[0];
				BeanWrapper embedbw = new BeanWrapper(embedClass);
				Object embedObj = siena.Util.createObjectInstance(embedClass);
				
				l.add(embedObj);
				
				Logger.debug(embedObj.toString());
				
				bw.set(field.getName(), o, l);			
			}
			else Validation.addError(
					clazz.getName() + "."+field.getName(), 
					"validation.fieldList.badType", fieldName);			
			
			SienaPlugin.pm().update(o);
			return (T) o;
    	} catch (Exception e) {
			throw new UnexpectedException(e);
		}
	}
	
	public static <T> T deleteListElement(T o, String fieldName, int idx) {
    	try {
    		Class<?> clazz = o.getClass();
			BeanWrapper bw = new BeanWrapper(o.getClass());
			Field field = clazz.getField(fieldName);
			
			if(List.class.isAssignableFrom(field.getType())){
				List l = (List)field.get(o);
				if(l == null)
					Validation.addError(
							clazz.getName() + "."+field.getName(), 
							"validation.fieldList.empty", fieldName);
				else {
					if(idx < 0 || idx > l.size()-1)
						Validation.addError(
							clazz.getName() + "."+field.getName(), 
							"validation.fieldList.indexOutOfBound", fieldName);
					else l.remove(idx);
				}
			}
			else Validation.addError(
					clazz.getName() + "."+field.getName(), 
					"validation.fieldList.badType", fieldName);

			SienaPlugin.pm().update(o);
			
    		return (T) o;
    	} catch (Exception e) {
			throw new UnexpectedException(e);
		}
    }
	
	public static <T> T addMapElement(T o, String fieldName, String key) 
    {
    	try {
    		Class<?> clazz = o.getClass();
			BeanWrapper bw = new BeanWrapper(o.getClass());
			Field field = clazz.getField(fieldName);
			
			if(Map.class.isAssignableFrom(field.getType())){
				Map l = (Map)field.get(o);
								
				Class<?> embedKeyClass = 
					(Class<?>) ((ParameterizedType) 
							field.getGenericType()).getActualTypeArguments()[0];
				
				Class<?> embedClass = 
					(Class<?>) ((ParameterizedType) 
						field.getGenericType()).getActualTypeArguments()[1];
					
				if(l == null){
					l = new HashMap();
				}

				Object embedObj = embedClass.newInstance();
				Object embedKey = Binder.directBind(key, embedKeyClass);
				
				if(l.get(embedKey) != null){
					Logger.debug("element with key %s already existing", embedKey);
					Validation.addError(
							fieldName, 
							"validation.fieldMap.alreadyExists", embedKey.toString());	
				}
				else {
					l.put(embedKey, embedObj);
					Logger.debug("map added {%s:%s}", embedKey, embedObj);
				}		
				
				bw.set(field.getName(), o, l);			
			}
			else Validation.addError(
					clazz.getName() + "."+field.getName(), 
					"validation.fieldMap.badType", fieldName);			
			
			SienaPlugin.pm().update(o);
			return (T) o;
    	} catch (Exception e) {
			throw new UnexpectedException(e);
		}
	}
	
	public static <T> T deleteMapElement(T o, String fieldName, String key)
    {
    	try {
    		Class<?> clazz = o.getClass();
			BeanWrapper bw = new BeanWrapper(o.getClass());
			Field field = clazz.getField(fieldName);
			
			if(Map.class.isAssignableFrom(field.getType())){
				Map l = (Map)field.get(o);
				if(l == null)
					Validation.addError(
							clazz.getName() + "."+field.getName(), 
							"validation.fieldMap.empty", fieldName);
				else {
					Class<?> embedKeyClass = 
						(Class<?>) ((ParameterizedType) 
								field.getGenericType()).getActualTypeArguments()[0];
					BeanWrapper keybw = new BeanWrapper(embedKeyClass);
					try {
						Object embedKey = Binder.directBind(key, embedKeyClass);
						l.remove(embedKey);
					}catch(Exception ex){
						Validation.addError(
							clazz.getName() + "."+field.getName() + "." + key, 
							"validation.fieldMap.keyBadFormat", fieldName, key);
					}					
				}
			}
			else Validation.addError(
					clazz.getName() + "."+field.getName(), 
					"validation.fieldMap.badType", fieldName);

			SienaPlugin.pm().update(o);
			
    		return (T) o;
    	} catch (Exception e) {
			throw new UnexpectedException(e);
		}
    }
	

 	
}