package controllers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.Play;
import play.data.binding.Binder;
import play.data.validation.MaxSize;
import play.data.validation.Password;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.modules.crudsiena.CrudSienaUtils;
import play.modules.siena.SienaModelUtils;
import play.modules.siena.SienaPlugin;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.utils.Java;
import siena.ClassInfo;
import siena.DateTime;
import siena.Id;
import siena.Json;
import siena.embed.Embedded;

public abstract class CRUD extends Controller {

    @Before
    static void addType() {
        ObjectType type = ObjectType.get(getControllerClass());
        renderArgs.put("type", type);
    }

    public static void index() {
    	if (getControllerClass() == CRUD.class) {
            forbidden();
        }
        render("CRUD/index.html");
    }

    public static void list(int page, String search, String searchFields, String orderBy, String order, String[] fields) {
    	// if fields is of length 1, it means this is one field or a list of field separated by commas
    	if(fields != null && fields.length == 1){
    		fields = fields[0].split(",");
    	}
    	
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        if (page < 1) {
            page = 1;
        }
        List<Object> objects = type.findPage(page, search, searchFields, orderBy, order, (String) request.args.get("where"));
        Long count = type.count(search, searchFields, (String) request.args.get("where"));
        Long totalCount = type.count(null, null, (String) request.args.get("where"));
        try {
            render(type, objects, count, totalCount, page, orderBy, order);
        } catch (TemplateNotFoundException e) {
            render("CRUD/list.html", type, objects, count, totalCount, page, orderBy, order, fields);
        }
    }

    public static void show(String id) {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Object object = SienaModelUtils.findById(type.entityClass, id);
        try {
            render(type, object);
        } catch (TemplateNotFoundException e) {
            render("CRUD/show.html", type, object);
        }
    }

    public static void addListElement(String id, String field) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Object object = SienaModelUtils.findById(type.entityClass, id);
        if(object == null){
        	Validation.addError(field, "crud.addListElement.saveBefore", new String[0]);
        	renderArgs.put("error", Validation.error(field));
        }else {        
        	CrudSienaUtils.addListElement(object, field);
            validation.valid(object);
            if (Validation.hasError(field)) {
                renderArgs.put("error", Validation.error(field));
            }
        }        
        
        try {
            render(type, object, field);
        } catch (TemplateNotFoundException e) {
            render("CRUD/addListElement.html", id, type, object, field);
        }
    }

    public static void deleteListElement(String id, String field, Integer idx) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Object object = SienaModelUtils.findById(type.entityClass, id);
                
        CrudSienaUtils.deleteListElement(object, field, idx);

        validation.valid(object);
        if (Validation.hasError(field)) {
            renderArgs.put("error", Validation.error(field));
        }
        
        try {
            render(id, type, object, field);
        } catch (TemplateNotFoundException e) {
            render("CRUD/deleteListElement.html", id, type, object, field);
        }
    }
    
    public static void addMapElement(String id, String field, String mkey) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Object object = SienaModelUtils.findById(type.entityClass, id);
        
        if(object == null){
        	Validation.addError(field, "crud.addMapElement.saveBefore", new String[0]);
        	renderArgs.put("error", Validation.error(field));
        }else {            
            CrudSienaUtils.addMapElement(object, field, mkey);
            validation.valid(object);
            if (Validation.hasError(field)) {
                renderArgs.put("error", Validation.error(field));
            }
        }     
        try {
            render(id, type, object, field);
        } catch (TemplateNotFoundException e) {
            render("CRUD/addMapElement.html", id, type, object, field);
        }
    }
    
    public static void deleteMapElement(String id, String field, String mkey) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Object object = SienaModelUtils.findById(type.entityClass, id);
                
        CrudSienaUtils.deleteMapElement(object, field, mkey);

        validation.valid(object);
        if (Validation.hasError(field)) {
            renderArgs.put("error", Validation.error(field));
        }
        try {
            render(id, type, object, field);
        } catch (TemplateNotFoundException e) {
            render("CRUD/deleteMapElement.html", id, type, object, field);
        }
    }
    
    @SuppressWarnings("deprecation")
    public static void attachment(String id, String field) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Object object = SienaModelUtils.findById(type.entityClass, id);

        notFoundIfNull(object);
        Object att = object.getClass().getField(field).get(object);
        if(att instanceof Model.BinaryField) {
            Model.BinaryField attachment = (Model.BinaryField)att;
            if (attachment == null || !attachment.exists()) {
                notFound();
            }
            response.contentType = attachment.type();
            renderBinary(attachment.get(), attachment.length());
        }
        // DEPRECATED
        if(att instanceof play.db.jpa.FileAttachment) {
            play.db.jpa.FileAttachment attachment = (play.db.jpa.FileAttachment)att;
            if (attachment == null || !attachment.exists()) {
                notFound();
            }
            renderBinary(attachment.get(), attachment.filename);
        }
        notFound();
    }
    
    public static void save(String id) throws Exception {
    	ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Object object = SienaModelUtils.findById(type.entityClass, id);
        notFoundIfNull(object);
        Binder.bind(object, "object", params.all());
        validation.valid(object);
        if (Validation.hasErrors()) {
            renderArgs.put("error", Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/show.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/show.html", type, object);
            }
        }
        SienaPlugin.pm().save(object);

        flash.success(Messages.get("crud.saved", type.modelName));
        if (params.get("_save") != null) {
            redirect(request.controller + ".list");
        }
        redirect(request.controller + ".show", SienaModelUtils.keyValue(object));
    }

    public static void blank() throws Exception{
    	ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object object = constructor.newInstance();
        try {
            render(type, object);
        } catch (TemplateNotFoundException e) {
            render("CRUD/blank.html", type, object);
        }
    }

    public static void create() throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object object = constructor.newInstance();
        // BAD IDEA because if there are constraints, the object is not saveds
        // if this is an automatic generated ID, saves it once before to have an id
//        boolean isGenerated = ClassInfo.isGenerated(ClassInfo.getIdField(type.entityClass));
//        if(isGenerated){
//        	object._save();
//        }
        Binder.bind(object, "object", params.all());
        validation.valid(object);
        if (Validation.hasErrors()) {
        	// if it is a generated ID, deletes it not to keep an object with errors
//            if(isGenerated){
//            	object._delete();
//            }
            renderArgs.put("error", Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/blank.html", type);
            } catch (TemplateNotFoundException e) {
                render("CRUD/blank.html", type);
            }
        }
        SienaPlugin.pm().save(object);
        flash.success(Messages.get("crud.created", type.modelName));
        if (params.get("_save") != null) {
            redirect(request.controller + ".list");
        }
        if (params.get("_saveAndAddAnother") != null) {
            redirect(request.controller + ".blank");
        }
        redirect(request.controller + ".show", SienaModelUtils.keyValue(object));
    }

    public static void delete(String id) {
    	ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Object object = SienaModelUtils.findById(type.entityClass, id);
        notFoundIfNull(object);
        try {
            SienaPlugin.pm().delete(object);
        } catch (Exception e) {
            flash.error(Messages.get("crud.delete.error", type.modelName));
            redirect(request.controller + ".show", SienaModelUtils.keyValue(object));
        }
        flash.success(Messages.get("crud.deleted", type.modelName));
        redirect(request.controller + ".list");
    }

    protected static ObjectType createObjectType(Class<? extends Model> entityClass) {
        return new ObjectType(entityClass);
    }
    
    // ~~~~~~~~~~~~~
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface For {
        Class<?> value();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Exclude {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Hidden {}
        
    // ~~~~~~~~~~~~~
    static int getPageSize() {
        return Integer.parseInt(Play.configuration.getProperty("crud.pageSize", "30"));
    }

    public static class ObjectType implements Comparable<ObjectType> {

        public Class<? extends Controller> controllerClass;
        public Class<?> entityClass;
        public String name;
        public String modelName;
        public String controllerName;
        public String keyName;
        
        public ObjectType(Class<?> modelClass) {
            this.modelName = modelClass.getSimpleName();
            this.entityClass = modelClass;
            this.keyName = SienaModelUtils.keyName(entityClass);
        }

		public ObjectType(String modelClass) throws ClassNotFoundException {
            this((Class<?>) Play.classloader.loadClass(modelClass));
        }

        public static ObjectType forClass(String modelClass) throws ClassNotFoundException {
            return new ObjectType(modelClass);
        }

        public static ObjectType get(Class<? extends Controller> controllerClass) {
        	Class<?> entityClass = getEntityClassForController(controllerClass);
            if (entityClass == null /*|| !Model.class.isAssignableFrom(entityClass)*/) {
                return null;
            }
            ObjectType type;
            try {
                type = (ObjectType) Java.invokeStaticOrParent(controllerClass, "createObjectType", entityClass);
            } catch (Exception e) {
                Logger.error(e, "Couldn't create an ObjectType. Use default one.");
                type = new ObjectType(entityClass);
            }
            type.name = controllerClass.getSimpleName().replace("$", "");
            type.controllerName = controllerClass.getSimpleName().toLowerCase().replace("$", "");
            type.controllerClass = controllerClass;
            return type;
        }

		public static Class<?> getEntityClassForController(Class<? extends Controller> controllerClass) {
        	if (controllerClass.isAnnotationPresent(For.class)) {
        		Class<?> cl = ((For) (controllerClass.getAnnotation(For.class))).value();
        		if(cl!=null && ClassInfo.isModel(cl))
        			return cl;
        		return null;
            }
            for(Type it : controllerClass.getGenericInterfaces()) {
                if(it instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType)it;
                    if (((Class<?>)type.getRawType()).getSimpleName().equals("CRUDWrapper")) {
                        return (Class<?>)type.getActualTypeArguments()[0];
                    }
                }
            }
            String name = controllerClass.getSimpleName().replace("$", "");
            name = "models." + name.substring(0, name.length() - 1);
            try {
                return (Class<?>) Play.classloader.loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        public Object getListAction() {
            return Router.reverse(controllerClass.getName() + ".list");
        }

        public Object getBlankAction() {
            return Router.reverse(controllerClass.getName() + ".blank");
        }

        public Long count(String search, String searchFields, String where) {
            return SienaModelUtils.count(SienaPlugin.pm(), entityClass, searchFields == null ? new ArrayList<String>() : Arrays.asList(searchFields.split("[ ]")), search, where);
        }

        @SuppressWarnings("unchecked")
		public List<Object> findPage(int page, String search, String searchFields, String orderBy, String order, String where) {
            return (List<Object>)SienaModelUtils.fetch(SienaPlugin.pm(), entityClass, (page - 1) * getPageSize(), getPageSize(), orderBy, order, searchFields == null ? new ArrayList<String>() : Arrays.asList(searchFields.split("[ ]")), search, where);
        }
        
        public Object findById(Object id) {
            if (id == null) return null;
            return SienaPlugin.sienaModelFactory(entityClass).findById(id);
        }
        
        public List<ObjectField> getFields() {
            List<ObjectField> fields = new ArrayList<ObjectField>();
            List<ObjectField> hiddenFields = new ArrayList<ObjectField>();
            for (Model.Property f : SienaModelUtils.listProperties(SienaPlugin.pm(), entityClass)) {
                ObjectField of = new ObjectField(f);                
                if (of.type != null) {
                    if (of.type.equals("hidden")) {
                        hiddenFields.add(of);
                    } else {
                        fields.add(of);
                    }
                }
            }

            hiddenFields.addAll(fields);
            return hiddenFields;
        }

        public List<String> getRawFields() {
        	List<String> fields = new ArrayList<String>();
            for (Field f : ClassInfo.getClassInfo(entityClass).allFields) {
                fields.add(f.getName());
            }
            return fields;
        }
        
        public ObjectField getField(String name) {
            for (ObjectField field : getFields()) {
                if (field.name.equals(name)) {
                    return field;
                }
            }
            return null;
        }

        public int compareTo(ObjectType other) {
            return modelName.compareTo(other.modelName);
        }

        @Override
        public String toString() {
            return modelName;
        }

        
	    public static class ObjectField {
		
	    	private Model.Property property;
            public String type = "unknown";
            public String name;
            public boolean multiple;
            public boolean required;

            // SPECIFIC CRUDSIENA
            public String multipleType;
            
            public ObjectField(Model.Property property) {
            	Field field = property.field;
                this.property = property;
                
            	if (CharSequence.class.isAssignableFrom(field.getType())) {
	                type = "text";
	                if (field.isAnnotationPresent(MaxSize.class)) {
                        int maxSize = field.getAnnotation(MaxSize.class).value();
                        if (maxSize > 100) {
                            type = "longtext";
                        }
                    }
                    if (field.isAnnotationPresent(Password.class)) {
                        type = "password";
                    }
	            }
            	if (Number.class.isAssignableFrom(field.getType()) 
            			|| field.getType().equals(int.class) || field.getType().equals(long.class)
            			|| field.getType().equals(short.class)
            			|| field.getType().equals(double.class) || field.getType().equals(float.class)) {
                    type = "number";
                }
                if (Boolean.class.isAssignableFrom(field.getType()) || field.getType().equals(boolean.class)) {
                    type = "boolean";
                }
	            if (Date.class.isAssignableFrom(field.getType())) {
	                type = "date";
	                // SPECIFIC CRUDSIENA
	                if (field.isAnnotationPresent(DateTime.class)) {
	                	type = "datetime";
	                }
	            }          
	            if (property.isRelation) {
                    type = "relation";
                } 
	            if (property.isMultiple) {
                    multiple = true;
                }
	            if (field.getType().isEnum()) {
                    type = "enum";
                }
	            if (property.isGenerated) {
                    type = null;                   
                }
                if (field.isAnnotationPresent(Required.class)) {
                    required = true;
                }
                if (field.isAnnotationPresent(Hidden.class)) {
                    type = "hidden";
                }
                if (field.isAnnotationPresent(Exclude.class)) {
                    type = null;
                }
                if (java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                    type = null;
                }
	            // Json field
	            if (Json.class.isAssignableFrom(field.getType())) {
	            	type = "longtext";
	            }
	            if (byte[].class.isAssignableFrom(field.getType())) {
	            	type = "binary";
	            }
	            /*if (Blob.class.isAssignableFrom(field.getType())) {
	            	type = "blob";
	            }*/
	            // siena.@Id
            	// if autogenerated, ID is not rendered
	            if (field.isAnnotationPresent(Id.class) && property.isGenerated) {	            	
	                type = null;
	            } 
	            // @Embedded field
	            if (field.isAnnotationPresent(Embedded.class)) {
	            	type = "embedded";
	            	if(List.class.isAssignableFrom(field.getType())){
	            		multipleType = "list";
	            	}
	            	else if(Map.class.isAssignableFrom(field.getType())){
	            		multipleType = "map";
	            	}
	            	else if(ClassInfo.isModel(field.getType())){
	            		type = "relation";
	            	}
	            }
	            
	            name = field.getName();
	        }
	
            public List<Object> getChoices() {
                return property.choices.list();
            }
            
			public static List<ObjectField> getFields(Class<?> clazz) {
                List<ObjectField> fields = new ArrayList<ObjectField>();
                for (Field f : clazz.getFields()) {
                    if (Modifier.isTransient(f.getModifiers()) || Modifier.isFinal(f.getModifiers())) {
                        continue;
                    }
                    ObjectField of = new ObjectField(buildProperty(f));
                    if (of.type != null) {
                        fields.add(of);
                    }
                }
                return fields;
            }

            protected static Model.Property buildProperty(final Field field) {
                return SienaModelUtils.buildProperty(field, SienaPlugin.pm());
            }	        
	    }
    }
    

}

