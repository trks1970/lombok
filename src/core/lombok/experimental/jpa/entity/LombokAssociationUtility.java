package lombok.experimental.jpa.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds {@code javax.persistence.OneToMany} annotations to field. Also adds {@code javax.persistence.JoinColumn} annotation
 * if association is unidirectional (mappedBy is not defined).
 * Generates utility methods in case of bidirectional association.
 * 
 * <li>cascade (Optional) Array of Cascade elements, maps to {@code javax.persistence.CascadeType}. Default {}, is not generated.</li>
 * <li>fetch (Optional) Maps to {@code javax.persistence.FetchType} Default {@code Fetch.LAZY}.</li>
 * <li>mappedBy (Optional) {@code javax.persistence.OneToMany#mappedBy}. Association is unidirectional if undefined. Default "", is not generated.</li>
 * <li>orphanRemoval (Optional) {@code javax.persistence.OneToMany#orphanRemoval}. Default {@code false}, is not generated.</li>
 * <li>targetEntity (Optional) {@code javax.persistence.OneToMany#targetEntity}. Default {@code void.class}, is not generated.</li>
 * 
 * <li>joinColumn (Optional) {@code javax.persistence.JoinColumn#name}. Default "", is not generated.</li>
 * <li>referencedColumn (Optional) {@code javax.persistence.JoinColumn#referencedColumnName}. Default "", is not generated.</li>
 * <li>nullable (Optional) {@code javax.persistence.JoinColumn#nullable}. Default {@code true}, is not generated.</li>
 * <li>unique (Optional) {@code javax.persistence.JoinColumn#unique}. Default {@code false}, is not generated.</li>
 * <li>insertable (Optional) {@code javax.persistence.JoinColumn#insertable}. Default {@code true}, is not generated.</li>
 * 
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) 
public @interface LombokAssociationUtility
{
}
