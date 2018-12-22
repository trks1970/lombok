package lombok.experimental.jpa.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to generate javax.persistence.Entity annotations.
 * Also generates public getter and setter for id and version.
 * 
 * <li>name (Optional) Maps to {@code javax.persistence.Entity#name}. Default "id".</li>
 * <li>idColumn (Optional) DB column of PK. Default "id".</li>
 * <li>idField (Optional) Field name of primary key. Defaults "id".</li>
 * <li>idType (Optional) Type of primary key. Defaults to {@code java.lang.Long}.</li>
 * <li>idGeneration (Optional) Maps to {@code javax.persistence.GenerationType}. Default "AUTO".</li>
 * <li>idSequence (Optional) Sequence to use for ID generation. If idGeneration is SEQUENCE and not specified, then "seq_" + name of annotated class in lowercase.</li>
 * 
 * <li>versionColumn (Optional) DB column of version field. Default "".</li>
 * <li>versionField (Optional) Field name of the version. Defaults to "version".</li>
 * <li>versionType (Optional) Type of version field. Defaults to {@code java.lang.Integer}</li>
 * 
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE) 
public @interface LombokJpaEntity 
{
	public static final String ENTITY = "javax.persistence.Entity";
	
	public static final String AUTO = "javax.persistence.GenerationType.AUTO";
	public static final String IDENTITY = "javax.persistence.GenerationType.IDENTITY";
	public static final String SEQUENCE = "javax.persistence.GenerationType.SEQUENCE";

	
	String name() default "";

	String idColumn() default "";
	String idField() default "id";
	FieldType idType() default FieldType.LONG;
	IDGenerator idGeneration() default IDGenerator.AUTO;
	String idSequence() default "";
	
	String versionColumn() default "";
	String versionField() default "version";
	FieldType versionType() default FieldType.INTEGER;
	
	/*String table() default "";
	String catalog() default "";
	String schema() default "";
	Idx[] indexes() default {};*/
	
	
}
