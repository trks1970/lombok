package lombok.javac.handlers.jpa.entity;


import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;


import lombok.AccessLevel;
import lombok.core.AnnotationValues;
import lombok.experimental.jpa.entity.LombokAssociationUtility;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;

/**
 * Handles the {@code lombok.experimental.SequencedEntity} annotation for javac.
 */
@ProviderFor( JavacAnnotationHandler.class )
public class HandleAssociationUtility extends JavacAnnotationHandler<LombokAssociationUtility>
{
	@Override
	public void handle( AnnotationValues<LombokAssociationUtility> annotation, JCAnnotation ast, JavacNode annotationNode )
	{
		JavacHandlerUtil.deleteAnnotationIfNeccessary(annotationNode, LombokAssociationUtility.class);
		JavacNode fieldNode = JavacHandlerUtil.upToFieldNode( annotationNode );
		JCVariableDecl fieldDecl = (JCVariableDecl) fieldNode.get();
		LombokAssociationUtility a = annotation.getInstance();

		addAddMethod( JavacHandlerUtil.upToTypeNode(annotationNode), fieldNode, fieldDecl, a );
		addRemoveMethod( JavacHandlerUtil.upToTypeNode(annotationNode), annotationNode, fieldDecl, a );

	}

	private void addRemoveMethod( JavacNode typeNode, JavacNode fieldNode, JCVariableDecl fieldDecl, LombokAssociationUtility a )
	{
		String parameterName = parameterName(fieldDecl);
		String argumentName = parameterName.substring(0, 1).toLowerCase() + parameterName.substring(1);
		String methodName = "remove" + parameterName;

		Type returnType = Javac.createVoidType(fieldNode.getSymbolTable(), CTC_VOID);
		long access = JavacHandlerUtil.toJavacModifier( AccessLevel.PUBLIC ) | (fieldDecl.mods.flags & Flags.STATIC);
		//Type fieldType = JavacHandlerUtil.getMirrorForFieldType(fieldNode);
		JavacTreeMaker maker = fieldNode.getTreeMaker();

		// children.remove( child )
		List<JCExpression> addArgs = List.<JCExpression> nil();
		addArgs = addArgs.append( maker.Ident( fieldNode.toName( argumentName ) ) );
		JCMethodInvocation removeChild = maker.Apply( List.<JCExpression> nil(), maker.Select( maker.Ident( fieldDecl.name ), fieldNode.toName( "remove" ) ), addArgs );
		
		// child.setParent(null)
		JCClassDecl parent = (JCClassDecl)typeNode.get(); 
		JCExpression receiver = maker.Ident( fieldNode.toName( argumentName ) );
		List<JCExpression> parentArgs = List.<JCExpression> nil();
		parentArgs = parentArgs.append( maker.Literal( CTC_BOT, null ) );
		JCMethodInvocation setParent = maker.Apply( List.<JCExpression> nil(), maker.Select( receiver, fieldNode.toName( "set" + parent.name ) ), parentArgs );
		
		
		JCVariableDecl param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), fieldNode.toName( argumentName ), maker.Ident( fieldNode.toName( parameterName ) ), null);
		List<JCVariableDecl> params = List.<JCVariableDecl> nil();
		params = params.append( param );
		
		List<JCStatement> statements = List.<JCStatement> nil();
		statements = statements.append( maker.Exec( removeChild ) );
		statements = statements.append( maker.Exec( setParent ) );
		JCBlock methodBody = maker.Block(0, statements );
		
		JCExpression methodType = maker.Type( returnType );
		
		JCMethodDecl decl = recursiveSetGeneratedBy(maker.MethodDef(maker.Modifiers(access, List.<JCAnnotation> nil()), fieldNode.toName( methodName ), methodType,
				List.<JCTypeParameter> nil(), params, List.<JCExpression> nil(), methodBody, null), typeNode.get(), fieldNode.getContext());
		
		JavacHandlerUtil.injectMethod( typeNode, decl );
		
	}

	private void addAddMethod( JavacNode typeNode, JavacNode fieldNode, JCVariableDecl fieldDecl, LombokAssociationUtility a )
	{
		String parameterName = parameterName(fieldDecl);
		String argumentName = parameterName.substring(0, 1).toLowerCase() + parameterName.substring(1);
		String methodName = "add" + parameterName;

		Type returnType = Javac.createVoidType(fieldNode.getSymbolTable(), CTC_VOID);
		long access = JavacHandlerUtil.toJavacModifier( AccessLevel.PUBLIC ) | (fieldDecl.mods.flags & Flags.STATIC);
		//Type fieldType = JavacHandlerUtil.getMirrorForFieldType(fieldNode);
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		
		// child.setParent(this)
		JCClassDecl parent = (JCClassDecl)typeNode.get(); 
		JCExpression receiver = maker.Ident( fieldNode.toName( argumentName ) );
		List<JCExpression> parentArgs = List.<JCExpression> nil();
		parentArgs = parentArgs.append( maker.Ident( fieldNode.toName( "this" ) ) );
		JCMethodInvocation setParent = maker.Apply( List.<JCExpression> nil(), maker.Select( receiver, fieldNode.toName( "set" + parent.name ) ), parentArgs );
		
		// children.add( child )
		List<JCExpression> addArgs = List.<JCExpression> nil();
		addArgs = addArgs.append( maker.Ident( fieldNode.toName( argumentName ) ) );
		JCMethodInvocation addChild = maker.Apply( List.<JCExpression> nil(), maker.Select( maker.Ident( fieldDecl.name ), fieldNode.toName( "add" ) ), addArgs );
		
		JCVariableDecl param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), fieldNode.toName( argumentName ), maker.Ident( fieldNode.toName( parameterName ) ), null);
		List<JCVariableDecl> params = List.<JCVariableDecl> nil();
		params = params.append( param );
		
		List<JCStatement> statements = List.<JCStatement> nil();
		statements = statements.append( maker.Exec( setParent ) );
		statements = statements.append( maker.Exec( addChild ) );
		JCBlock methodBody = maker.Block(0, statements );
		
		JCExpression methodType = maker.Type( returnType );
		
		JCMethodDecl decl = recursiveSetGeneratedBy(maker.MethodDef(maker.Modifiers(access, List.<JCAnnotation> nil()), fieldNode.toName( methodName ), methodType,
				List.<JCTypeParameter> nil(), params, List.<JCExpression> nil(), methodBody, null), typeNode.get(), fieldNode.getContext());
		
		JavacHandlerUtil.injectMethod( typeNode, decl );
		
	}

	private String parameterName( JCVariableDecl fieldDecl )
	{
		String parameter = null;
		JCTypeApply typeApply = (JCTypeApply)fieldDecl.vartype;
		JCIdent ident = (JCIdent) typeApply.arguments.get( 0 ); 
		parameter = ident.toString();
		return parameter;
	}
	
	/*JCMethodDecl createSetter(long access, JavacNode field, JavacTreeMaker treeMaker, String setterName, JavacNode source) 
	{
		if (setterName == null) return null;
		
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		
		JCExpression fieldRef = createFieldAccessor(treeMaker, field, FieldAccess.ALWAYS_FIELD);
		JCAssign assign = treeMaker.Assign(fieldRef, treeMaker.Ident(fieldDecl.name));
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		List<JCAnnotation> copyableAnnotations = findCopyableAnnotations(field);
		
		Name methodName = field.toName(setterName);
		List<JCAnnotation> annsOnParam = copyAnnotations(onParam).appendList(copyableAnnotations);
		
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, field.getContext());
		JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(flags, annsOnParam), fieldDecl.name, fieldDecl.vartype, null);
		
		if (!hasNonNullAnnotations(field)) {
			statements.append(treeMaker.Exec(assign));
		} else {
			JCStatement nullCheck = generateNullCheck(treeMaker, field, source);
			if (nullCheck != null) statements.append(nullCheck);
			statements.append(treeMaker.Exec(assign));
		}
		
		if (booleanFieldToSet != null) {
			JCAssign setBool = treeMaker.Assign(treeMaker.Ident(booleanFieldToSet), treeMaker.Literal(CTC_BOOLEAN, 1));
			statements.append(treeMaker.Exec(setBool));
		}
		
		if (methodType == null) {
			//WARNING: Do not use field.getSymbolTable().voidType - that field has gone through non-backwards compatible API changes within javac1.6.
			methodType = treeMaker.Type(Javac.createVoidType(field.getSymbolTable(), CTC_VOID));
			returnStatement = null;
		}
		
		if (returnStatement != null) statements.append(returnStatement);
		
		JCBlock methodBody = treeMaker.Block(0, statements.toList());
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.of(param);
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		List<JCAnnotation> annsOnMethod = copyAnnotations(onMethod);
		if (isFieldDeprecated(field) || deprecate) {
			annsOnMethod = annsOnMethod.prepend(treeMaker.Annotation(genJavaLangTypeRef(field, "Deprecated"), List.<JCExpression>nil()));
		}
		
		JCMethodDecl decl = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(access, annsOnMethod), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source.get(), field.getContext());
		copyJavadoc(field, decl, CopyJavadoc.SETTER);
		return decl;
	}*/
}
