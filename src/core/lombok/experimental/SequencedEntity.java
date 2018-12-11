package lombok.experimental;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put on any field to make lombok build a standard getter.
 * <p>
 * Complete documentation is found at
 * <a href="https://projectlombok.org/features/GetterSetter">the project lombok
 * features page for &#64;Getter and &#64;Setter</a>.
 * <p>
 * Even though it is not listed, this annotation also has the {@code onMethod}
 * parameter. See the full documentation for more details.
 * <p>
 * Example:
 * 
 * <pre>
 * private &#64;Getter int foo;
 * </pre>
 * 
 * will generate:
 * 
 * <pre>
 * public int getFoo() {
 * 	return this.foo;
 * }
 * </pre>
 * <p>
 * This annotation can also be applied to a class, in which case it'll be as if
 * all non-static fields that don't already have a {@code @Getter} annotation
 * have the annotation.
 */
@Target({ElementType.TYPE}) 
@Retention(RetentionPolicy.SOURCE) 
public @interface SequencedEntity 
{
	/**
	 * The name of the field and column to be used as primary key. Default is id.
	 * 
	 * @return The name of the column/field to be used as primary key.
	 */
	String id() default "id";

	/**
	 * The name of the field and column to be used as version. Default is version.
	 * 
	 * @return The name of the column/field to be used as version.
	 */
	String version() default "version";

	
}
