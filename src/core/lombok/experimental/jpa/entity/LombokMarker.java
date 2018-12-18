package lombok.experimental.jpa.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD} )
public @interface LombokMarker
{
	String value() default "";
}
