package lombok.experimental.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <li>idColumn defaults to "id"</li>
 * <li>idField defaults to "id" </li>
 * <li>idType defaults to {@code java.lang.Long}</li>
 * <li>idGeneration defaults to "AUTO"</li>
 * <li>idSequence if idGeneration is SEQUENCE and not specified, then "seq_" + name of annotated class in lowercase</li>
 * 
 * <li>versionColumn defaults to "version"</li>
 * <li>versionField defaults to "id"</li>
 * <li>versionType defaults to {@code java.lang.Integer}</li>
 * 
 * entity jpa --class KitchenSink 
 * AUTO --table kitchen_sink
 *--readOnly true --serializable true 
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE) 
public @interface LombokJpaEntity 
{
	String name() default "";

	String idColumn() default "id";
	String idField() default "id";
	FieldType idType() default FieldType.LONG;
	GenerationType idGeneration() default GenerationType.AUTO;
	String idSequence() default "";
	
	String versionColumn() default "version";
	String versionField() default "version";
	FieldType versionType() default FieldType.INTEGER;
	
	String table() default "";
	String catalog() default "";
	String schema() default "";
	Idx[] indexes() default {};
	
	
}
