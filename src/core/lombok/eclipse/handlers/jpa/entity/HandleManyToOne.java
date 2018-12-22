package lombok.eclipse.handlers.jpa.entity;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.mangosdk.spi.ProviderFor;

import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil;
import lombok.eclipse.handlers.generators.AnnotationGenerator;
import lombok.eclipse.handlers.generators.MemberValuePairGenerator;
import lombok.experimental.jpa.entity.LombokManyToOne;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleManyToOne extends EclipseAnnotationHandler<LombokManyToOne> 
{
	private AnnotationGenerator annotationGen = AnnotationGenerator.instance();
	private MemberValuePairGenerator mvpGen = MemberValuePairGenerator.instance();

	@Override public void handle(AnnotationValues<LombokManyToOne> annotation, Annotation ast, EclipseNode annotationNode) 
	{
		EclipseNode fieldNode = EclipseHandlerUtil.upToFieldNode( annotationNode );
		FieldDeclaration fieldDecl = (FieldDeclaration) fieldNode.get();
		LombokManyToOne a = annotation.getInstance();

		addManyToOneAnnotation(fieldDecl, a);
	}
	
	private void addManyToOneAnnotation( FieldDeclaration fieldDecl, LombokManyToOne a )
	{
		List<ASTNode> args = mvpGen.addNameReference( "fetch", a.fetch().fetchType(), new ArrayList<ASTNode>() );
		if( 0 < a.cascade().length )
		{
			//List<String> cascades = Stream.of(a.cascade()).map(Cascade::cascadeType).collect(Collectors.toList());
			List<String> cascades = new ArrayList<String>();
			for (int i = 0; i < a.cascade().length; i++) 
			{
				cascades.add( a.cascade()[i].cascadeType() );
			}
			args = mvpGen.addNameReferenceArray("cascade", cascades, args );
		}
		if( !a.optional() )
		{
			args = mvpGen.addBooleanParameter("optional", a.optional(), args );
		}
		if( !a.targetEntity().equals( void.class ) )
		{
			args = mvpGen.addTypeRefParameter("targetEntity", a.targetEntity().getName(), args );
		}
		annotationGen.createAnnotation( fieldDecl, LombokManyToOne.MANY_TO_ONE, args );
	}

	
}
