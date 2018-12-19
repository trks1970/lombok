package lombok.experimental.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) 
public @interface LombokOneToMany
{
	public static final String ONE_TO_MANY = "javax.persistence.OneToMany";
	public static final String JOIN_COLUMN = "javax.persistence.JoinColumn";
	
	Cascade[] cascade() default {};
	Fetch fetch() default Fetch.LAZY;
	String mappedBy() default "";
	boolean orphanRemoval() default false;
	boolean bidirectional() default true;
	String joinColumn() default "";
	String referencedColumn() default "";
	boolean nullable() default true; 
}
