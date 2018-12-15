package lombok.eclipse.handlers.generators;

import static lombok.eclipse.Eclipse.fromQualifiedName;
import static lombok.eclipse.handlers.EclipseHandlerUtil.setGeneratedBy;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

public class TypeReferenceGenerator
{
	private static final TypeReferenceGenerator instance = new TypeReferenceGenerator();

	private TypeReferenceGenerator()
	{
	}

	public static TypeReferenceGenerator instance()
	{
		return instance;
	}

	public TypeReference createTypeReference( String typeName, Annotation source )
	{
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;

		TypeReference typeReference;
		if( typeName.contains( "." ) )
		{

			char[][] typeNameTokens = fromQualifiedName( typeName );
			long[] pos = new long[typeNameTokens.length];
			Arrays.fill( pos, p );

			typeReference = new QualifiedTypeReference( typeNameTokens, pos );
		}
		else
		{
			typeReference = null;
		}

		setGeneratedBy( typeReference, source );
		return typeReference;
	}
}
