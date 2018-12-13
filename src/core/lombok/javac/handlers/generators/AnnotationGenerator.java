package lombok.javac.handlers.generators;

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

import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;

public class AnnotationGenerator 
{
	public static void addAnnotation( JCClassDecl typeDecl, JavacNode node, String annotation )
	{
		addAnnotation( typeDecl.mods, node, typeDecl.pos, JavacHandlerUtil.getGeneratedBy(typeDecl), node.getContext(), annotation, null );
	}

	public static void addAnnotation( JCClassDecl classDecl, JavacNode node, String annotation, JCExpression arg )
	{
		List<JCExpression> argList = List.<JCExpression> nil();
		argList = argList.append( arg );
		addAnnotation( classDecl, node, annotation, argList );
	}

	public static void addAnnotation( JCClassDecl classDecl, JavacNode node, String annotation, List<JCExpression> argList )
	{
		addAnnotation( classDecl.mods, node, classDecl.pos, JavacHandlerUtil.getGeneratedBy(classDecl), node.getContext(), annotation, argList );
	}

	public static void addAnnotation( JCVariableDecl fieldDecl, JavacNode node, String annotation )
	{
		addAnnotation( fieldDecl.mods, node, fieldDecl.pos, JavacHandlerUtil.getGeneratedBy(fieldDecl), node.getContext(), annotation, null );
	}

	public static void addAnnotation( JCVariableDecl fieldDecl, JavacNode node, String annotation, JCExpression arg )
	{
		List<JCExpression> argList = List.<JCExpression> nil();
		argList = argList.append( arg );
		addAnnotation( fieldDecl, node, annotation, argList );
	}

	public static void addAnnotation( JCVariableDecl fieldDecl, JavacNode node, String annotation, List<JCExpression> argList )
	{
		addAnnotation( fieldDecl.mods, node, fieldDecl.pos, JavacHandlerUtil.getGeneratedBy(fieldDecl), node.getContext(), annotation, argList );
	}
	
	public static JCAnnotation createAnnotation( JCModifiers mods, JavacNode node, int pos, JCTree source, Context context, String annotationTypeFqn, List<JCExpression> argList )
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
					return null;
			}

			if( annType instanceof JCFieldAccess )
			{
				if( annType.toString().equals( annotationTypeFqn ) )
					return null;
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
				else if( arg instanceof JCAnnotation )
				{
					( (JCAnnotation) arg ).pos = pos;
					for( JCExpression  exp : ( (JCAnnotation) arg ).args )
					{
						if( arg instanceof JCAssign )
						{
							( (JCAssign) exp ).lhs.pos = pos;
							( (JCAssign) exp ).rhs.pos = pos;
						}						
					}
				}
			}
		}
		else
		{
			argList = List.<JCExpression> nil();
		} 
		JCAnnotation annotation = JavacHandlerUtil.recursiveSetGeneratedBy( maker.Annotation( annType, argList ), source, context );
		System.out.println( "Created annotation " + annotation );
		annotation.pos = pos;
		return annotation;
	}	


	private static void addAnnotation( JCModifiers mods, JavacNode node, int pos, JCTree source, Context context, String annotationTypeFqn, List<JCExpression> argList )
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
		else
		{
			argList = List.<JCExpression> nil();
		} 
		JCAnnotation annotation = JavacHandlerUtil.recursiveSetGeneratedBy( maker.Annotation( annType, argList ), source, context );
		System.out.println( "annotation " + annotation );
		annotation.pos = pos;
		mods.annotations = mods.annotations.append( annotation );
	}	
}
