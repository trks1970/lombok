package lombok.experimental.jpa;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention( SOURCE )
@Target( TYPE )
public @interface Idx
{
	String name() default "";
	String columns();
	String unique() default "";
}
