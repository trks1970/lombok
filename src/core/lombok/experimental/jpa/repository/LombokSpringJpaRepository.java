package lombok.experimental.jpa.repository;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
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
@Retention( SOURCE )
@Target( TYPE )
public @interface LombokSpringJpaRepository
{
	public String JPA_REPOSITORY = "org.springframework.data.jpa.repository.JpaRepository";
	public String SPEC_EXECUTOR = "org.springframework.data.jpa.repository.JpaSpecificationExecutor";
	
	String entity();
	String pkType() default "Long";
}
