package lombok.eclipse.handlers.generators;

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
	private MemberValuePairGenerator mvpGen;
	Annotation source;
	
	@Before
	public void setUp()
	{
		annotationGen = AnnotationGenerator.instance();
		mvpGen = MemberValuePairGenerator.instance();
		
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
		indexArgs = mvpGen.addStringParameter( "name", "test1", indexArgs );
		indexArgs = mvpGen.addStringParameter( "columns", "a, b, c", indexArgs );
		indexArgs = mvpGen.addBooleanAttribute( "unique", true, indexArgs );
		Annotation annotation = annotationGen.doCreateAnnotation( source, "javax.persistence.Index", indexArgs );
		Assert.assertEquals( "@javax.persistence.Index(name = \"test1\",columns = \"a, b, c\",unique = true)", annotation.toString( ) );
	}

}
