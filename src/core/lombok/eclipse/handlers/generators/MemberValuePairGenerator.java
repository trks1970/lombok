package lombok.eclipse.handlers.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;

public class MemberValuePairGenerator
{
	private static final MemberValuePairGenerator instance = new MemberValuePairGenerator();
	public static final long[] NULL_POSS = {0L};
	
	private MemberValuePairGenerator() {}
	
	public static MemberValuePairGenerator instance() 
	{
		return instance;
	}

	public MemberValuePair createStringParameter( String attribute, String value )
	{
		return new MemberValuePair( attribute.toCharArray(), 0, 0, new StringLiteral( value.toCharArray(), 0,0,0 ) );
	}

	public MemberValuePair createBooleanParameter( String attribute, boolean value )
	{
		return new MemberValuePair( attribute.toCharArray(), 0, 0, value ? new TrueLiteral( 0, 0 ) : new FalseLiteral( 0, 0 ) );
	}

	public MemberValuePair createTypeRefParameter( String attribute, String typeFQN )
	{
		return new MemberValuePair( attribute.toCharArray(), 0, 0, new QualifiedTypeReference(fromQualifiedName(typeFQN), NULL_POSS ) );
	}

	public MemberValuePair createNameReference( String attribute, String nameFQN )
	{
		return new MemberValuePair( attribute.toCharArray(), 0, 0, 
				new QualifiedNameReference( fromQualifiedName(nameFQN), NULL_POSS, 0, 0 ) );
	}

	public MemberValuePair createAnnotationArray( String attribute, List<Annotation> values )
	{
		ArrayInitializer initializer = new ArrayInitializer();
		initializer.expressions = values.toArray( new Expression[values.size()] );
		return new MemberValuePair( attribute.toCharArray(), 0, 0, initializer );
	}

	public MemberValuePair createNameReferenceArray( String attribute, List<String> fqnNames )
	{
		ArrayInitializer initializer = new ArrayInitializer();
		List<QualifiedNameReference> references = new ArrayList<QualifiedNameReference>();
		for( String nameFQN : fqnNames )
		{
			references.add( new QualifiedNameReference( fromQualifiedName(nameFQN), NULL_POSS, 0, 0 ) );
		}
		initializer.expressions = references.toArray( new Expression[references.size()] );
		return new MemberValuePair( attribute.toCharArray(), 0, 0, initializer );
	}

	public List<ASTNode> addStringParameter( String attribute, String value, List<ASTNode> argList )
	{
		argList.add( createStringParameter( attribute, value ) );
		return argList;
	}

	public List<ASTNode> addBooleanParameter( String attribute, boolean value, List<ASTNode> argList )
	{
		argList.add( createBooleanParameter( attribute, value ) );
		return argList;
	}

	public List<ASTNode> addAnnotationArray( String attribute, List<Annotation> values, List<ASTNode> argList )
	{
		argList.add( createAnnotationArray( attribute, values ) );
		return argList;
	}

	public List<ASTNode> addNameReferenceArray( String attribute, List<String> fqnValues, List<ASTNode> argList )
	{
		argList.add( createNameReferenceArray( attribute, fqnValues ) );
		return argList;
	}

	public List<ASTNode> addTypeRefParameter( String attribute, String typeFQN, List<ASTNode> argList )
	{
		argList.add( createTypeRefParameter( attribute, typeFQN ) );
		return argList;
	}

	public List<ASTNode> addNameReference( String attribute, String type, List<ASTNode> argList )
	{
		argList.add( createNameReference( attribute, type ) );
		return argList;
	}
	

	public char[][] fromQualifiedName(String typeName) {
		String[] split = Pattern.compile("\\.").split(typeName);
		char[][] result = new char[split.length][];
		for (int i = 0; i < split.length; i++) {
			result[i] = split[i].toCharArray();
		}
		return result;
	}

}
