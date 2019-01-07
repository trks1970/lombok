package lombok.eclipse.handlers.jpa.entity;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import java.lang.reflect.Modifier;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil;
import lombok.eclipse.handlers.SetGeneratedByVisitor;
import lombok.experimental.jpa.entity.LombokAssociationUtility;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleAssociationUtility extends EclipseAnnotationHandler<LombokAssociationUtility>
{
	//private AnnotationGenerator annotationGen = AnnotationGenerator.instance();
	//private MemberValuePairGenerator mvpGen = MemberValuePairGenerator.instance();
		
	@Override
	public void handle( AnnotationValues<LombokAssociationUtility> annotation, Annotation ast, EclipseNode annotationNode )
	{
		EclipseNode fieldNode = EclipseHandlerUtil.upToFieldNode( annotationNode );
		FieldDeclaration fieldDecl = (FieldDeclaration) fieldNode.get();
		LombokAssociationUtility a = annotation.getInstance();

		addAddMethod( EclipseHandlerUtil.upToTypeNode(annotationNode), annotationNode, fieldDecl, a );
		addRemoveMethod( EclipseHandlerUtil.upToTypeNode(annotationNode), annotationNode, fieldDecl, a );

		
		/*addOneToManyAnnotation(fieldDecl, a);
		
		if( !a.mappedBy().isEmpty() )
		{
			// bidirectional, add utility methods
			addAddMethod( EclipseHandlerUtil.upToTypeNode(annotationNode), annotationNode, fieldDecl, a );
			addRemoveMethod( EclipseHandlerUtil.upToTypeNode(annotationNode), annotationNode, fieldDecl, a );
		}
		else
		{
			// unidirectional, add joinColumn annotation
			addJoinColumn( fieldDecl, a );
		}*/
	}

	private void addRemoveMethod(EclipseNode typeNode, EclipseNode annotationNode, FieldDeclaration fieldDecl, LombokAssociationUtility a) 
	{
		TypeDeclaration parent = (TypeDeclaration)typeNode.get();
		String parameterName = parameterName(fieldDecl);
		String argumentName = parameterName.substring(0, 1).toLowerCase() + parameterName.substring(1);
		String methodName = "remove" + parameterName;
		
		ASTNode source = annotationNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;

		MethodDeclaration method = new MethodDeclaration( parent.compilationResult);
		Argument param = new Argument(argumentName.toCharArray(), p, new SingleTypeReference(parameterName.toCharArray(), 0), Modifier.FINAL);
		method.modifiers = Modifier.PUBLIC;
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		method.returnType.sourceStart = pS; method.returnType.sourceEnd = pE;
		param.sourceStart = pS; param.sourceEnd = pE;
		method.arguments = new Argument[] { param };
		method.selector = methodName.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;

		// children.remove( child )
		SingleNameReference childrenRef = new SingleNameReference(fieldDecl.name, 0);
		MessageSend removeChild = new MessageSend();
		removeChild.sourceStart = pS; removeChild.sourceEnd = pE;
		setGeneratedBy(removeChild, annotationNode.get());
		removeChild.receiver = childrenRef;
		setGeneratedBy( removeChild.receiver, annotationNode.get() );
		removeChild.selector = "remove".toCharArray();
		removeChild.arguments = new Expression[] { new SingleNameReference(argumentName.toCharArray(), 0) };

		// child.setParent( null );
		MessageSend setParent = new MessageSend();
		setParent.sourceStart = pS; setParent.sourceEnd = pE;
		setGeneratedBy(setParent, annotationNode.get());
		setParent.receiver = new SingleNameReference(argumentName.toCharArray(), 0);
		setGeneratedBy( setParent.receiver, annotationNode.get() );
		String parentName = String.valueOf( parent.name );
		String typeName = parentName.substring(0, 1).toUpperCase() + parentName.substring(1);
		setParent.selector = ("set" + typeName ).toCharArray();
		setParent.arguments = new Expression[] { new NullLiteral(pS, pE) };

		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		method.statements = new Statement[] { removeChild, setParent };
		method.traverse(new SetGeneratedByVisitor(source), parent.scope);
		injectMethod(typeNode, method);
	}

	private void addAddMethod(EclipseNode typeNode, EclipseNode annotationNode, FieldDeclaration fieldDecl, LombokAssociationUtility a) 
	{
		TypeDeclaration parent = (TypeDeclaration)typeNode.get();
		String parameterName = parameterName(fieldDecl);
		String argumentName = parameterName.substring(0, 1).toLowerCase() + parameterName.substring(1);
		String methodName = "add" + parameterName;
		
		ASTNode source = annotationNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;

		MethodDeclaration method = new MethodDeclaration( parent.compilationResult);
		Argument param = new Argument(argumentName.toCharArray(), p, new SingleTypeReference(parameterName.toCharArray(), 0), Modifier.FINAL);
		method.modifiers = Modifier.PUBLIC;
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		method.returnType.sourceStart = pS; method.returnType.sourceEnd = pE;
		param.sourceStart = pS; param.sourceEnd = pE;
		method.arguments = new Argument[] { param };
		method.selector = methodName.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		// child.setParent( this );
		MessageSend setParent = new MessageSend();
		setParent.sourceStart = pS; setParent.sourceEnd = pE;
		setGeneratedBy(setParent, annotationNode.get());
		setParent.receiver = new SingleNameReference(argumentName.toCharArray(), 0);
		setGeneratedBy( setParent.receiver, annotationNode.get() );
		String parentName = String.valueOf( parent.name );
		String typeName = parentName.substring(0, 1).toUpperCase() + parentName.substring(1);
		setParent.selector = ("set" + typeName ).toCharArray();
		setParent.arguments = new Expression[] { new ThisReference(pS, pE) };
		
		// children.add( child )
		SingleNameReference childrenRef = new SingleNameReference(fieldDecl.name, 0);
		MessageSend addChild = new MessageSend();
		addChild.sourceStart = pS; addChild.sourceEnd = pE;
		setGeneratedBy(addChild, annotationNode.get());
		addChild.receiver = childrenRef;
		setGeneratedBy( addChild.receiver, annotationNode.get() );
		addChild.selector = "add".toCharArray();
		addChild.arguments = new Expression[] { new SingleNameReference(argumentName.toCharArray(), 0) };

		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		method.statements = new Statement[] { setParent, addChild };
		method.traverse(new SetGeneratedByVisitor(source), parent.scope);
		injectMethod(typeNode, method);
	}
	

	/*private void addOneToManyAnnotation( FieldDeclaration fieldDecl, LombokOneToMany a )
	{
		List<ASTNode> args = mvpGen.addNameReference( "fetch", a.fetch().fetchType(), new ArrayList<ASTNode>() );
		if( !a.mappedBy().isEmpty() )
		{
			args = mvpGen.addStringParameter( "mappedBy", a.mappedBy(), args );
		}
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
		if( a.orphanRemoval() )
		{
			args = mvpGen.addBooleanParameter("orphanRemoval", a.orphanRemoval(), args );
		}
		if( !a.targetEntity().equals( void.class ) )
		{
			args = mvpGen.addTypeRefParameter("targetEntity", a.targetEntity().getName(), args );
		}
		annotationGen.createAnnotation( fieldDecl, LombokOneToMany.ONE_TO_MANY, args );
	}*/
	
	/*private void addJoinColumn( FieldDeclaration fieldDecl, LombokOneToMany a )
	{
		List<ASTNode> args = new ArrayList<ASTNode>();
		if( !a.joinColumn().isEmpty() )
		{
			args = mvpGen.addStringParameter("name", a.joinColumn(), args ); 
		}
		if( !a.referencedColumn().isEmpty() )
		{
			args = mvpGen.addStringParameter("referencedColumnName", a.referencedColumn(), args ); 
		}
		if( !a.nullable() )
		{
			args = mvpGen.addBooleanParameter("nullable", a.nullable(), args ); 
		}
		if( !a.insertable() )
		{
			args = mvpGen.addBooleanParameter("insertable", a.insertable(), args ); 
		}
		if( a.unique() )
		{
			args = mvpGen.addBooleanParameter("unique", a.nullable(), args ); 
		}
		annotationGen.createAnnotation( fieldDecl, LombokOneToMany.JOIN_COLUMN, args );
	}*/

	private String parameterName( FieldDeclaration fieldDecl )
	{
		String parameter = null;
		TypeReference ref = fieldDecl.type;
		if( ref instanceof ParameterizedSingleTypeReference )
		{
			ParameterizedSingleTypeReference iRef = (ParameterizedSingleTypeReference) ref;
			if( iRef.typeArguments.length == 1 )
			{
				parameter = Eclipse.toQualifiedName( iRef.typeArguments[0].getTypeName() );
			}
		}
		return parameter;
	}	
}
