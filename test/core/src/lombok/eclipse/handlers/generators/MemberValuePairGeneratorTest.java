package lombok.eclipse.handlers.generators;

import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.junit.Test;

public class MemberValuePairGeneratorTest
{
	private MemberValuePairGenerator mvpGen = MemberValuePairGenerator.instance();
	
	@Test
	public void testCreateTypeRefParameter()
	{
		MemberValuePair mvp = mvpGen.createTypeRefParameter( "generator", "javax.persistence.GenerationType.AUTO" );
		System.out.println( "typeRef " + mvp );
	}

	@Test
	public void testCreateStringParameter()
	{
		MemberValuePair mvp = mvpGen.createStringParameter( "generator", "javax.persistence.GenerationType.AUTO" );
		System.out.println( "string " + mvp );
	}

}
