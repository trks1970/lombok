package lombok.eclipse.handlers.jpa.entity;

import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.eclipse.Eclipse;
import lombok.eclipse.handlers.jpa.entity.HandleJpaEntity;
import lombok.experimental.jpa.entity.Idx;
import lombok.experimental.jpa.entity.LombokJpaEntity;

@RunWith(MockitoJUnitRunner.class)
public class HandleJpaEntityTest
{
	private static final char[][] annotationTypeFqn = Eclipse.fromQualifiedName("lombok.experimental.jpa.LombokJpaEntity");
	
	LombokJpaEntity a = mock( LombokJpaEntity.class );
	Idx idx1 = mock( Idx.class );
	Idx idx2 = mock( Idx.class );
	
	HandleJpaEntity handler;
	Annotation source;
	TypeDeclaration typeDecl;
	
	@Before
	public void setUp()
	{
		handler = new HandleJpaEntity();
		int pS = 0;
		int pE = annotationTypeFqn.length;
		long p = (long) pS << 32 | pE;
		long[] poss = new long[annotationTypeFqn.length];
		Arrays.fill( poss, p );
		QualifiedTypeReference qualifiedType = new QualifiedTypeReference( annotationTypeFqn, poss );
		source = new NormalAnnotation( qualifiedType, pS );
		
		CompilationResult result = new CompilationResult( "file.java".toCharArray(), 0, 0, 10 );
		typeDecl = new TypeDeclaration( result );
		
		when( idx1.name() ).thenReturn( "index1" );
		when( idx1.unique() ).thenReturn( true );
		when( idx1.columns() ).thenReturn( "a, b, c" );

		when( idx2.name() ).thenReturn( "index2" );
		when( idx2.unique() ).thenReturn( false );
		when( idx2.columns() ).thenReturn( "a, b" );

		when( a.table() ).thenReturn( "aTable" );
		when( a.catalog() ).thenReturn( "aCatalog" );
		when( a.schema() ).thenReturn( "aSchema" );
		when( a.indexes() ).thenReturn( new Idx[] { idx1, idx2 } );
	}

	@Test
	public void testAddTableAnnotation()
	{
		handler.addTableAnnotation( source, a, typeDecl );
		for( int i = 0; i < typeDecl.annotations.length; i++ )
		{
			System.out.println( typeDecl.annotations[i] );
		}
		System.out.println( typeDecl );
	}

}
