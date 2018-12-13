package lombok.experimental.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <li>idColumn (Optional) DB column of PK</li>
 * <li>idField (Optional) defaults to "id" </li>
 * <li>idType (Optional) defaults to {@code java.lang.Long}</li>
 * <li>idGeneration (Optional) defaults to "AUTO"</li>
 * <li>idSequence (Optional) sequence to use for ID generation. If idGeneration is SEQUENCE and not specified, then "seq_" + name of annotated class in lowercase.</li>
 * 
 * <li>versionColumn (Optional) DB column of version field"</li>
 * <li>versionField (Optional) defaults to "version"</li>
 * <li>versionType (Optional) defaults to {@code java.lang.Integer}</li>
 * 
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE) 
public @interface LombokJpaEntity 
{
	String name() default "";

	String idColumn() default "";
	String idField() default "id";
	FieldType idType() default FieldType.LONG;
	GenerationType idGeneration() default GenerationType.AUTO;
	String idSequence() default "";
	
	String versionColumn() default "";
	String versionField() default "version";
	FieldType versionType() default FieldType.INTEGER;
	
	String table() default "";
	String catalog() default "";
	String schema() default "";
	Idx[] indexes() default {};
	
	
}
