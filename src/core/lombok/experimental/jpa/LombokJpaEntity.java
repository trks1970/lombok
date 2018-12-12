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
 * entity jpa --class KitchenSink --identifierStrategy
 * AUTO --table kitchen_sink --versionField vrs
 * --versionColumn vrs --versionType java.lang.Integer --schema mySchema
 * --catalog myCatalog --entityName KSEntity --readOnly true --identifierField
 * PK --identifierType java.lang.Long --serializable true --force
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE) 
public @interface LombokJpaEntity 
{
	/**
	 * The name of the field and column to be used as primary key. Default is
	 * id.
	 * 
	 * @return The name of the column/field to be used as primary key.
	 */
	String idColumn() default "id";
	String idField() default "id";
	FieldType idType() default FieldType.LONG;
	GenerationType idGeneration() default GenerationType.AUTO;
	String idSequence() default "";
	
	/**
	 * The name of the field and column to be used as version. Default is
	 * version.
	 * 
	 * @return The name of the column/field to be used as version.
	 */
	String versionColumn() default "version";
	String versionField() default "version";
	FieldType versionType() default FieldType.INTEGER;
	
}
