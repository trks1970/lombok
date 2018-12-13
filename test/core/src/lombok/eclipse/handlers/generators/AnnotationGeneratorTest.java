package lombok.eclipse.handlers.generators;

import static lombok.eclipse.handlers.generators.MemberValuePairGenerator.createMemberValuePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import lombok.eclipse.Eclipse;

public class AnnotationGeneratorTest
{
	private static final char[][] annotationTypeFqn = Eclipse.fromQualifiedName("lombok.experimental.jpa.LombokJpaEntity");
	private AnnotationGenerator annotationGen;
	Annotation source;
	
	@Before
	public void setUp()
	{
		annotationGen = new AnnotationGenerator();
		int pS = 0;
		int pE = annotationTypeFqn.length;
		long p = (long) pS << 32 | pE;
		long[] poss = new long[annotationTypeFqn.length];
		Arrays.fill( poss, p );
		QualifiedTypeReference qualifiedType = new QualifiedTypeReference( annotationTypeFqn, poss );
		source = new NormalAnnotation( qualifiedType, pS );
	}
	
	@Test
	public void testCreateAnnotation()
	{	
		List<ASTNode> indexArgs = new ArrayList<ASTNode>();
		indexArgs = createMemberValuePair( "name", "test1", indexArgs );
		indexArgs = createMemberValuePair( "columns", "a, b, c", indexArgs );
		indexArgs = createMemberValuePair( "unique", true, indexArgs );
		Annotation annotation = annotationGen.createAnnotation( source, "javax.persistence.Index", indexArgs );
		Assert.assertEquals( "@javax.persistence.Index(name = \"test1\",columns = \"a, b, c\",unique = true)", annotation.toString( ) );
	}

}
