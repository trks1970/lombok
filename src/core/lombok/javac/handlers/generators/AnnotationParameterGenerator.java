package lombok.javac.handlers.generators;

import com.sun.tools.javac.util.List;

import static lombok.javac.handlers.JavacHandlerUtil.genTypeRef;

import com.sun.tools.javac.tree.JCTree.JCExpression;

import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

public class AnnotationParameterGenerator
{
	private static final AnnotationParameterGenerator instance = new AnnotationParameterGenerator();
	
	private AnnotationParameterGenerator() {}
	
	public static AnnotationParameterGenerator instance() 
	{
		return instance;
	}
	
	public JCExpression createParameter( JavacNode node, String attribute, String value )
	{
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression parameter = maker.Assign( maker.Ident( node.toName( attribute ) ), maker.Literal( value ) );
		return parameter;
	}

	public JCExpression createParameter( JavacNode node, String attribute, boolean value )
	{
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression parameter = maker.Assign( maker.Ident( node.toName( attribute ) ), maker.Literal( value ) );
		return parameter;
	}

	public JCExpression createTypeRefParameter( JavacNode node, String attribute, String value )
	{
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression parameter = maker.Assign( maker.Ident( node.toName( attribute ) ), genTypeRef( node, value ) );
		return parameter;
	}

	public List<JCExpression> createParameter( JavacNode node, String attribute, String value, List<JCExpression> argList )
	{
		return argList.append( createParameter( node, attribute, value ) );
	}

	public List<JCExpression> createParameter( JavacNode node, String attribute, boolean value, List<JCExpression> argList )
	{
		return argList.append( createParameter( node, attribute, value ) );
	}

	public List<JCExpression> createTypeRefParameter( JavacNode node, String attribute, String value, List<JCExpression> argList )
	{
		return argList.append( createTypeRefParameter( node, attribute, value ) );
	}

}
