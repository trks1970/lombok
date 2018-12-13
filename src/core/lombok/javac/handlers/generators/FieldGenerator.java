package lombok.javac.handlers.generators;

import static lombok.javac.handlers.JavacHandlerUtil.*;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import lombok.experimental.jpa.Idx;
import lombok.experimental.jpa.LombokJpaEntity;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

@LombokJpaEntity(indexes = {
	@Idx(name="test1",unique=true,columns="id, version"),
	@Idx(name="test2",columns="id, text")
})
public class FieldGenerator
{
	public static JCVariableDecl createField( int modifier, String fieldName, String fieldType, JCAnnotation source, JavacNode annotationNode )
	{
		JCVariableDecl fieldDecl = findField( fieldName, annotationNode );
		if( fieldDecl == null )
		{
			JavacNode typeNode = upToTypeNode( annotationNode );
			JavacTreeMaker maker = typeNode.getTreeMaker();
			JCExpression  expr = genTypeRef( typeNode, fieldType );
			fieldDecl = recursiveSetGeneratedBy(maker.VarDef(
					maker.Modifiers( modifier ),
					typeNode.toName( fieldName ), expr, null), annotationNode.get(), typeNode.getContext());
		}
		return fieldDecl;
	}

	private static JCVariableDecl findField( String fieldName, JavacNode node )
	{
		JCVariableDecl fieldDecl = null;
		node = upToTypeNode( node );

		if( node != null && node.get() instanceof JCClassDecl )
		{
			for( JCTree def : ( (JCClassDecl) node.get() ).defs )
			{
				if( def instanceof JCVariableDecl )
				{
					if( ( (JCVariableDecl) def ).name.contentEquals( fieldName ) )
					{
						fieldDecl = (JCVariableDecl) def;
						break;
					}
				}
			}
		}

		return fieldDecl;
	}

}
