package lombok.eclipse.handlers.generators;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;

public class MemberValuePairGenerator
{
	public static MemberValuePair createMemberValuePair( String attribute, String value )
	{
		return new MemberValuePair( attribute.toCharArray(), 0, 0, new StringLiteral( value.toCharArray(), 0,0,0 ) );
	}

	public static List<ASTNode> createMemberValuePair( String attribute, String value, List<ASTNode> argList )
	{
		MemberValuePair m = new MemberValuePair( attribute.toCharArray(), 0, 0, new StringLiteral( value.toCharArray(), 0,0,0 ) );
		argList.add( m );
		return argList;
	}
}
