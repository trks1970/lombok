package lombok.experimental.test;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( TYPE )
public @interface SubAnnotation
{
	String name();
	String value();
}
