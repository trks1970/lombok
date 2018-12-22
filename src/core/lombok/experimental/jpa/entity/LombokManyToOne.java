package lombok.experimental.jpa.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds {@code javax.persistence.ManyToOne} annotations to field. 
 * 
 * <li>cascade (Optional) Array of Cascade elements, maps to {@code javax.persistence.CascadeType}. Default {}, is not generated.</li>
 * <li>fetch (Optional) Maps to {@code javax.persistence.FetchType} Default {@code Fetch.EAGER}</li>
 * <li>optional (Optional) {@code javax.persistence.ManyToOne#optional}. Default {@code true}, is not generated.</li>
 * <li>targetEntity (Optional) {@code javax.persistence.ManyToOne#targetEntity}. Default {@code void.class}, is not generated.</li>
 *  
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) 
public @interface LombokManyToOne
{
	public static final String MANY_TO_ONE = "javax.persistence.ManyToOne";
	
	Cascade[] cascade() default {};
	Fetch fetch() default Fetch.EAGER;
	boolean optional() default true;
	Class<?> targetEntity() default void.class;
}
