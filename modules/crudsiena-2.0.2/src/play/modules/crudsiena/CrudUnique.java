package play.modules.crudsiena;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.sf.oval.configuration.annotation.Constraint;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Constraint(checkWith = CrudUniqueCheck.class)
public @interface CrudUnique {
	String message() default CrudUniqueCheck.mes;
}

