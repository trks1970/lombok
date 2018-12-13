package lombok.javac.handlers.generators;


import com.sun.tools.javac.util.List;

import static lombok.javac.handlers.JavacHandlerUtil.genTypeRef;

import com.sun.tools.javac.tree.JCTree.JCExpression;

import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

public class AnnotationParameterGenerator
{
	public static JCExpression createParameter( JavacNode node, String attribute, String value )
	{
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression parameter = maker.Assign(
				maker.Ident(node.toName( attribute )), maker.Literal( value ) );
		return parameter;
	}

	public static JCExpression createParameter( JavacNode node, String attribute, boolean value )
	{
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression parameter = maker.Assign(
				maker.Ident(node.toName( attribute )), maker.Literal( value ) );
		return parameter;
	}

	public static JCExpression createTypeRefParameter( JavacNode node, String attribute, String value )
	{
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression parameter = maker.Assign(
				maker.Ident(node.toName( attribute )), genTypeRef( node, value ) );
		return parameter;
	}

	public static List<JCExpression> createParameter( JavacNode node, String attribute, String value, List<JCExpression> argList )
	{
		return argList.append( createParameter( node, attribute, value ) );
	}

	public static List<JCExpression> createParameter( JavacNode node, String attribute, boolean value, List<JCExpression> argList )
	{
		return argList.append( createParameter( node, attribute, value ) );
	}

	public static List<JCExpression> createTypeRefParameter( JavacNode node, String attribute, String value, List<JCExpression> argList )
	{
		return argList.append( createTypeRefParameter( node, attribute, value ) );
	}

}
