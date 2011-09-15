package play.modules.crudsiena;

import java.util.List;

import play.exceptions.UnexpectedException;
import play.modules.siena.SienaModelUtils;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.FieldContext;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;
import siena.Model;
import siena.Query;

/**
 * @author spreiter301 (original code and contributor)
 * @author mandubian (just integrated code into module)
 *
 * Oval Check associated to CrudUnique annotation.
 * CrudUniqueCheck verifies the annotated Siena entity field is unique
 *
 */
public class CrudUniqueCheck extends AbstractAnnotationCheck<CrudUnique> {

	private static final long serialVersionUID = 4615916626718372468L;
		
	public static final String mes = "validation.unique";
	private CrudUnique unique;
	
	@Override
	public void configure(CrudUnique unique) {
		setMessage(unique.message());
		this.unique = unique;
	}
	@Override
	public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) throws OValException {
		if(value == null) {
			// return true in case we want to field to be nullable
			return true;
		}
		
		if(validatedObject instanceof Model) {
			Model ss = (Model) validatedObject;
			
			try {
	            if (context != null) {
	                if (context instanceof FieldContext) {
	                    FieldContext ctx = (FieldContext) context;
	                    String fieldName = ctx.getField().getName();
	                    
	                    Query<? extends Object> all = Model.all(validatedObject.getClass());
	        			List<? extends Object> fetched = all.filter(fieldName, value).fetch();
	        			if(fetched.size() == 0) return true;
	        			Object sskey = SienaModelUtils.keyValue(ss);
	        			if(sskey == null) {
	        				// new object => value already in database
	        				return false;
	        			} else {
	        				// existing object
	        				// check if we can find the id inside the fetched objects
	        				for(Object o : fetched) {
	        					Model s = (Model) o;
	        					Object skey = SienaModelUtils.keyValue(ss);
	        					if(skey!=null && skey.equals(sskey)) return true;
	        				}
	        				return false;
	        			}
	                }
	            }
	        } catch (Exception e) {
	            throw new UnexpectedException(e);
	        }	        
		}
		return false;
	}
}

