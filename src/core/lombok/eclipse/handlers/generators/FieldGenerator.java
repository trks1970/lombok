package lombok.eclipse.handlers.generators;

import static lombok.eclipse.handlers.EclipseHandlerUtil.setGeneratedBy;
import static lombok.eclipse.handlers.EclipseHandlerUtil.upToTypeNode;
import static lombok.eclipse.handlers.HandleLog.createTypeReference;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

import lombok.eclipse.EclipseNode;

public class FieldGenerator
{
	private static final FieldGenerator instance = new FieldGenerator();
	
	private FieldGenerator() {}
	
	public static FieldGenerator instance() 
	{
		return instance;
	}

	
	public FieldDeclaration createField( int modifier, String fieldName, String fieldType, Annotation source, EclipseNode annotationNode )
	{
		FieldDeclaration fieldDecl = findField( fieldName, annotationNode );
		if( fieldDecl == null )
		{
			fieldDecl = new FieldDeclaration( fieldName.toCharArray(), 0, -1 );
			setGeneratedBy( fieldDecl, source );
			fieldDecl.declarationSourceEnd = -1;
			fieldDecl.modifiers = modifier;
			fieldDecl.type = createTypeReference( fieldType, source );
		}
		return fieldDecl;
	}

	private FieldDeclaration findField( String fieldName, EclipseNode node )
	{
		FieldDeclaration fieldDecl = null;
		node = upToTypeNode( node );
		if( node != null && node.get() instanceof TypeDeclaration )
		{
			TypeDeclaration typeDecl = (TypeDeclaration) node.get();
			if( typeDecl.fields != null )
				for( FieldDeclaration def : typeDecl.fields )
				{
					char[] fName = def.name;
					if( fName == null )
						continue;
					if( fieldName.equals( new String( fName ) ) )
					{
						fieldDecl = def;
						break;
					}
				}
		}

		return fieldDecl;
	}

}
