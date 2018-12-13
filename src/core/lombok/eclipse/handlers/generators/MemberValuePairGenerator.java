package lombok.eclipse.handlers.generators;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;

public class MemberValuePairGenerator
{
	public static MemberValuePair createMemberValuePair( String attribute, String value )
	{
		return new MemberValuePair( attribute.toCharArray(), 0, 0, new StringLiteral( value.toCharArray(), 0,0,0 ) );
	}

	public static MemberValuePair createMemberValuePair( String attribute, boolean value )
	{
		return new MemberValuePair( attribute.toCharArray(), 0, 0, value ? new TrueLiteral( 0, 0 ) : new FalseLiteral( 0, 0 ) );
	}

	public static List<ASTNode> createMemberValuePair( String attribute, String value, List<ASTNode> argList )
	{
		argList.add( createMemberValuePair( attribute, value ) );
		return argList;
	}

	public static List<ASTNode> createMemberValuePair( String attribute, boolean value, List<ASTNode> argList )
	{
		argList.add( createMemberValuePair( attribute, value ) );
		return argList;
	}

}
