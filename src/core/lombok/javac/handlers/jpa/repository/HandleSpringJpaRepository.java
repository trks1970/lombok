package lombok.javac.handlers.jpa.repository;

import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.util.List;

import lombok.core.AnnotationValues;
import lombok.experimental.jpa.repository.LombokSpringJpaRepository;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

@ProviderFor( JavacAnnotationHandler.class )
public class HandleSpringJpaRepository extends JavacAnnotationHandler<LombokSpringJpaRepository>
{

	@Override
	public void handle( AnnotationValues<LombokSpringJpaRepository> annotation, JCAnnotation ast, JavacNode annotationNode )
	{
		deleteAnnotationIfNeccessary(annotationNode, LombokSpringJpaRepository.class);
		// add @javax.persistence.Entity on type
		JavacNode typeNode = annotationNode.up();
		
		JCClassDecl typeDecl = (JCClassDecl) typeNode.get();
		LombokSpringJpaRepository a = annotation.getInstance();
		String className = a.entity().getName();
		String pkName = a.pkType().getName();
		JavacTreeMaker maker = typeNode.getTreeMaker();
		List<JCExpression> params = List.<JCExpression> nil();
		params = params.append( maker.Ident( annotationNode.toName( className ) ) );
		params = params.append( maker.Ident( annotationNode.toName( pkName ) ) );
		JCTypeApply jpaRepository  = maker.TypeApply( maker.Ident( annotationNode.toName( LombokSpringJpaRepository.JPA_REPOSITORY ) ), params );
		typeDecl.implementing = typeDecl.implementing.append( jpaRepository );
		
		List<JCExpression> execP = List.<JCExpression> nil();
		execP = execP.append( maker.Ident( annotationNode.toName( className ) ) );
		JCTypeApply specExecutor  = maker.TypeApply( maker.Ident( annotationNode.toName( LombokSpringJpaRepository.SPEC_EXECUTOR ) ), execP );
		typeDecl.implementing = typeDecl.implementing.append( specExecutor );
	}

}
