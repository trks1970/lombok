package lombok.javac.handlers;

import static lombok.javac.handlers.JavacHandlerUtil.*;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.core.AnnotationValues;
import lombok.experimental.SequencedEntity;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;

/**
 * Handles the {@code lombok.experimental.SequencedEntity} annotation for javac.
 */
@ProviderFor( JavacAnnotationHandler.class )
public class HandleSequencedEntity extends JavacAnnotationHandler<SequencedEntity>
{
	private HandleGetter getterHandler = new HandleGetter();
	private HandleSetter setterHandler = new HandleSetter();
	
	@Override
	public void handle( AnnotationValues<SequencedEntity> annotation, JCAnnotation ast, JavacNode annotationNode )
	{
		deleteAnnotationIfNeccessary(annotationNode, SequencedEntity.class);
		// add @javax.persistence.Entity on type
		JavacNode typeNode = annotationNode.up();
		JCClassDecl typeDecl = (JCClassDecl) typeNode.get();
		addAnnotation( typeDecl, typeNode, "javax.persistence.Entity" );
		// add field private Long <id>
		String idFieldName = annotation.getInstance().id();
		String versionFieldName = annotation.getInstance().version();
		if (fieldExists(idFieldName, typeNode) != MemberExistsResult.NOT_EXISTS) {
			annotationNode.addWarning("Field '" + idFieldName + "' already exists.");
			return;
		}
		if (fieldExists(versionFieldName, typeNode) != MemberExistsResult.NOT_EXISTS) {
			annotationNode.addWarning("Field '" + versionFieldName + "' already exists.");
			return;
		}

		JavacTreeMaker maker = typeNode.getTreeMaker();
		JCExpression  longExpr = genJavaLangTypeRef(typeNode, "Long");
		JCVariableDecl idFieldDecl = recursiveSetGeneratedBy(maker.VarDef(
				maker.Modifiers( Flags.PRIVATE ),
				typeNode.toName( idFieldName ), longExpr, null), annotationNode.get(), typeNode.getContext());
		JavacNode idNode = injectFieldAndMarkGenerated(typeNode, idFieldDecl);
		
		// add @javax.persistence.Id to field
		addAnnotation(idFieldDecl, typeNode, "javax.persistence.Id");
		
		// add @javax.persistence.SequenceGenerator(name = "<typeName>Gen",sequenceName = "seq_<typeName>")
		String typeName = typeNode.getName().toLowerCase();
		addSequenceAnnotation(idFieldDecl, typeNode, typeName );
		
		// add @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "<typeName>Gen")
		addGeneratedValueAnnotation(idFieldDecl, typeNode, typeName );
		// add @javax.persistence.Column(name = "<id>")
		addColumnAnnotation(idFieldDecl, typeNode, idFieldName);
		
		// add field private Integer <version>
		
		JCExpression  integerExpr = genJavaLangTypeRef(typeNode, "Integer");
		JCVariableDecl versionFieldDecl = recursiveSetGeneratedBy(maker.VarDef(
				maker.Modifiers( Flags.PRIVATE ),
				typeNode.toName( versionFieldName ), integerExpr, null), annotationNode.get(), typeNode.getContext());
		JavacNode versionNode = injectFieldAndMarkGenerated(typeNode, versionFieldDecl);
		
		// add @javax.persistence.Version
		addAnnotation( versionFieldDecl, typeNode, "javax.persistence.Version" );
		// add @javax.persistence.Column(name = "<version>")
		addColumnAnnotation( versionFieldDecl, typeNode, versionFieldName);
		
		// add getter and setter for id and version
		getterHandler.createGetterForField(AccessLevel.PUBLIC, idNode, typeNode, true, false, List.<JCAnnotation> nil() );
		setterHandler.createSetterForField(AccessLevel.PUBLIC, idNode, typeNode, true, List.<JCAnnotation> nil(), List.<JCAnnotation> nil() );
		getterHandler.createGetterForField(AccessLevel.PUBLIC, versionNode, typeNode, true, false, List.<JCAnnotation> nil() );
		setterHandler.createSetterForField(AccessLevel.PUBLIC, versionNode, typeNode, true, List.<JCAnnotation> nil(), List.<JCAnnotation> nil() );
		
	}

	private void addAnnotation( JCClassDecl typeDecl, JavacNode node, String annotation )
	{
		addAnnotation( typeDecl.mods, node, typeDecl.pos, JavacHandlerUtil.getGeneratedBy(typeDecl), node.getContext(), annotation, null );
	}

	private void addAnnotation( JCVariableDecl fieldDecl, JavacNode node, String annotation )
	{
		addAnnotation( fieldDecl.mods, node, fieldDecl.pos, JavacHandlerUtil.getGeneratedBy(fieldDecl), node.getContext(), annotation, null );
	}

	private void addSequenceAnnotation( JCVariableDecl fieldDecl, JavacNode node, String typeName )
	{
		JavacTreeMaker maker = node.getTreeMaker(); 
		List<JCExpression> argList = List.<JCExpression> nil();
		JCExpression name = maker.Assign(
				maker.Ident(node.toName( "name" )), maker.Literal( typeName + "Gen") );
		JCExpression sequenceName = maker.Assign(
				maker.Ident(node.toName( "sequenceName" )), maker.Literal("seq_" + typeName ) );
		argList = argList.append(name);
		argList = argList.append(sequenceName);
		addAnnotation( fieldDecl.mods, node, fieldDecl.pos, JavacHandlerUtil.getGeneratedBy(fieldDecl), node.getContext(), "javax.persistence.SequenceGenerator", 
				argList );
	}
	
	private void addGeneratedValueAnnotation( JCVariableDecl fieldDecl, JavacNode node, String typeName )
	{
		JavacTreeMaker maker = node.getTreeMaker(); 
		List<JCExpression> argList = List.<JCExpression> nil();
		JCExpression generationAUTO = JavacHandlerUtil.chainDotsString( node, "javax.persistence.GenerationType.AUTO" );
		JCExpression strategy = maker.Assign(
				maker.Ident(node.toName( "strategy" )), generationAUTO );
		JCExpression generator = maker.Assign(
				maker.Ident(node.toName( "generator" )), maker.Literal( typeName + "Gen") );
		argList = argList.append( strategy );
		argList = argList.append( generator );
		addAnnotation( fieldDecl.mods, node, fieldDecl.pos, JavacHandlerUtil.getGeneratedBy(fieldDecl), node.getContext(), "javax.persistence.GeneratedValue", 
				argList );
		
	}

	private void addColumnAnnotation( JCVariableDecl fieldDecl, JavacNode node, String columnName )
	{
		JavacTreeMaker maker = node.getTreeMaker(); 
		List<JCExpression> argList = List.<JCExpression> nil();
		JCExpression column = maker.Assign(
				maker.Ident(node.toName( "name" )), maker.Literal( columnName ) );
		argList = argList.append(column);
		addAnnotation( fieldDecl.mods, node, fieldDecl.pos, JavacHandlerUtil.getGeneratedBy(fieldDecl), node.getContext(), "javax.persistence.Column", 
				argList );
	}

	/*private void addAnnotation( JCModifiers mods, JavacNode node, int pos, JCTree source, Context context, String annotationTypeFqn, JCExpression arg )
	{
		boolean isJavaLangBased;
		String simpleName;
		{
			int idx = annotationTypeFqn.lastIndexOf( '.' );
			simpleName = idx == -1 ? annotationTypeFqn : annotationTypeFqn.substring( idx + 1 );

			isJavaLangBased = idx == 9 && annotationTypeFqn.regionMatches( 0, "java.lang.", 0, 10 );
		}

		for( JCAnnotation ann : mods.annotations )
		{
			JCTree annType = ann.getAnnotationType();
			if( annType instanceof JCIdent )
			{
				Name lastPart = ( (JCIdent) annType ).name;
				if( lastPart.contentEquals( simpleName ) )
					return;
			}

			if( annType instanceof JCFieldAccess )
			{
				if( annType.toString().equals( annotationTypeFqn ) )
					return;
			}
		}

		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression annType = isJavaLangBased ? JavacHandlerUtil.genJavaLangTypeRef( node, simpleName )
				: JavacHandlerUtil.chainDotsString( node, annotationTypeFqn );
		annType.pos = pos;
		if( arg != null )
		{
			arg.pos = pos;
			if( arg instanceof JCAssign )
			{
				( (JCAssign) arg ).lhs.pos = pos;
				( (JCAssign) arg ).rhs.pos = pos;
			}
		}
		List<JCExpression> argList = arg != null ? List.of( arg ) : List.<JCExpression> nil();
		JCAnnotation annotation = JavacHandlerUtil.recursiveSetGeneratedBy( maker.Annotation( annType, argList ), source, context );
		annotation.pos = pos;
		mods.annotations = mods.annotations.append( annotation );
	}*/

	private void addAnnotation( JCModifiers mods, JavacNode node, int pos, JCTree source, Context context, String annotationTypeFqn, List<JCExpression> argList )
	{
		boolean isJavaLangBased;
		String simpleName;
		{
			int idx = annotationTypeFqn.lastIndexOf( '.' );
			simpleName = idx == -1 ? annotationTypeFqn : annotationTypeFqn.substring( idx + 1 );

			isJavaLangBased = idx == 9 && annotationTypeFqn.regionMatches( 0, "java.lang.", 0, 10 );
		}

		for( JCAnnotation ann : mods.annotations )
		{
			JCTree annType = ann.getAnnotationType();
			if( annType instanceof JCIdent )
			{
				Name lastPart = ( (JCIdent) annType ).name;
				if( lastPart.contentEquals( simpleName ) )
					return;
			}

			if( annType instanceof JCFieldAccess )
			{
				if( annType.toString().equals( annotationTypeFqn ) )
					return;
			}
		}

		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression annType = isJavaLangBased ? JavacHandlerUtil.genJavaLangTypeRef( node, simpleName )
				: JavacHandlerUtil.chainDotsString( node, annotationTypeFqn );
		annType.pos = pos;
		if( argList != null )
		{
			for( JCExpression arg : argList )
			{
				arg.pos = pos;
				if( arg instanceof JCAssign )
				{
					( (JCAssign) arg ).lhs.pos = pos;
					( (JCAssign) arg ).rhs.pos = pos;
				}
			}
		}
		if( argList == null )
		{
			argList = List.<JCExpression> nil();
		} 
		JCAnnotation annotation = JavacHandlerUtil.recursiveSetGeneratedBy( maker.Annotation( annType, argList ), source, context );
		annotation.pos = pos;
		mods.annotations = mods.annotations.append( annotation );
	}
}
