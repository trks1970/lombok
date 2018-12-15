package lombok.eclipse.handlers.generators;

import static lombok.eclipse.handlers.EclipseHandlerUtil.setGeneratedBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

import lombok.eclipse.Eclipse;

public class AnnotationGenerator
{
	private static final AnnotationGenerator instance = new AnnotationGenerator();
	
	private AnnotationGenerator() {}
	
	public static AnnotationGenerator instance() 
	{
		return instance;
	}
	
	public  void addAnnotation( TypeDeclaration typeDecl, String annotation )
	{
		addAnnotation( typeDecl, typeDecl.annotations, Eclipse.fromQualifiedName(annotation), null );
	}

	public  void addAnnotation( TypeDeclaration typeDecl, String annotation, ASTNode arg )
	{
		List<ASTNode> argList = new ArrayList<ASTNode>();
		argList.add(  arg );
		addAnnotation( typeDecl, typeDecl.annotations, Eclipse.fromQualifiedName(annotation), argList );
	}

	public  void addAnnotation( TypeDeclaration typeDecl, String annotation, List<ASTNode> argList )
	{
		addAnnotation( typeDecl, typeDecl.annotations, Eclipse.fromQualifiedName(annotation), argList );
	}
	
	public  void addAnnotation( FieldDeclaration fieldDecl, String annotation )
	{
		addAnnotation( fieldDecl, fieldDecl.annotations, Eclipse.fromQualifiedName(annotation), null );
	}

	public  void addAnnotation( FieldDeclaration fieldDecl, String annotation, ASTNode arg )
	{
		List<ASTNode> argList = new ArrayList<ASTNode>();
		argList.add(  arg );
		addAnnotation( fieldDecl, fieldDecl.annotations, Eclipse.fromQualifiedName(annotation), argList );
	}

	public  void addAnnotation( FieldDeclaration fieldDecl, String annotation, List<ASTNode> argList )
	{
		addAnnotation( fieldDecl, fieldDecl.annotations, Eclipse.fromQualifiedName(annotation), argList );
	}
	
	private  Annotation[] createAnnotationArray( ASTNode node, Annotation ann )
	{
		
		Annotation[] originalAnnotationArray = null;
		if( node instanceof FieldDeclaration )
		{
			originalAnnotationArray = ((FieldDeclaration)node).annotations;
		}
		else if( node instanceof MethodDeclaration )
		{
			originalAnnotationArray = ((MethodDeclaration)node).annotations;
		}
		else if( node instanceof ConstructorDeclaration )
		{
			originalAnnotationArray = ((ConstructorDeclaration)node).annotations;
		}
		else if( node instanceof TypeDeclaration )
		{
			originalAnnotationArray = ((TypeDeclaration)node).annotations;
		}
		else
		{
			throw new IllegalStateException("Unsupported ASTNode <" + node.getClass().getName() + "> for annotation injection." );
		}

		Annotation[] newAnnotationArray = annotationExists(originalAnnotationArray, ann.type.getTypeName() ); 
		if( newAnnotationArray == null )
		{
			if( originalAnnotationArray == null )
			{
				newAnnotationArray = new Annotation[] { ann };
			}
			else
			{
				newAnnotationArray = new Annotation[originalAnnotationArray.length + 1];
				System.arraycopy( originalAnnotationArray, 0, newAnnotationArray, 0, originalAnnotationArray.length );
				newAnnotationArray[originalAnnotationArray.length] = ann;
			}
		}
		return newAnnotationArray;
	}
	
	public  Annotation createAnnotation(ASTNode source, String annotationFQN, List<ASTNode> argList )
	{
		int pS = source.sourceStart;
		int pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		char[][] annotationTypeFqn = Eclipse.fromQualifiedName(annotationFQN);
		long[] poss = new long[annotationTypeFqn.length];
		Arrays.fill( poss, p );
		QualifiedTypeReference qualifiedType = new QualifiedTypeReference( annotationTypeFqn, poss );
		setGeneratedBy( qualifiedType, source );

		Annotation ann = null;
		if( argList != null && !argList.isEmpty() )
		{
			if( argList.size() == 1 )
			{
				ASTNode arg = argList.get( 0 );
				if( arg instanceof Expression )
				{
					SingleMemberAnnotation sma = new SingleMemberAnnotation( qualifiedType, pS );
					sma.declarationSourceEnd = pE;
					arg.sourceStart = pS;
					arg.sourceEnd = pE;
					sma.memberValue = (Expression) arg;
					setGeneratedBy( sma.memberValue, source );
					ann = sma;
				}
				else if( arg instanceof MemberValuePair )
				{
					NormalAnnotation na = new NormalAnnotation( qualifiedType, pS );
					na.declarationSourceEnd = pE;
					arg.sourceStart = pS;
					arg.sourceEnd = pE;
					na.memberValuePairs = new MemberValuePair[] { (MemberValuePair) arg };
					setGeneratedBy( na.memberValuePairs[0], source );
					setGeneratedBy( na.memberValuePairs[0].value, source );
					na.memberValuePairs[0].value.sourceStart = pS;
					na.memberValuePairs[0].value.sourceEnd = pE;
					ann = na;
				}
			}
			else
			{
				NormalAnnotation na = new NormalAnnotation( qualifiedType, pS );
				na.declarationSourceEnd = pE;
				for( ASTNode arg : argList )
				{
					arg.sourceStart = pS;
					arg.sourceEnd = pE;
				}
				na.memberValuePairs = argList.toArray( new MemberValuePair[argList.size()] );
				for( int i = 0; i < na.memberValuePairs.length; i++ )
				{
					na.memberValuePairs[i].value.sourceStart = pS;
					na.memberValuePairs[i].value.sourceEnd = pE;
				}
				ann = na;
			}
		}
		else
		{
			MarkerAnnotation ma = new MarkerAnnotation( qualifiedType, pS );
			ma.declarationSourceEnd = pE;
			ann = ma;
		}
		setGeneratedBy( ann, source );
		return ann;
	}
	
	private  Annotation[] addAnnotation( ASTNode source, Annotation[] originalAnnotationArray, char[][] annotationTypeFqn, List<ASTNode> argList )
	{
		Annotation annotation = createAnnotation(source, Eclipse.toQualifiedName(annotationTypeFqn), argList);
		Annotation[] anns = createAnnotationArray( source, annotation );
		
		return anns;
	}
	
	private  Annotation[] annotationExists(Annotation[] originalAnnotationArray, char[][] annotationTypeFqn )
	{
		Annotation[]  annotations = null;
		if( originalAnnotationArray != null )
		{
			char[] simpleName = annotationTypeFqn[annotationTypeFqn.length - 1];
			for( Annotation ann : originalAnnotationArray )
			{
				if( ann.type instanceof QualifiedTypeReference )
				{
					char[][] t = ( (QualifiedTypeReference) ann.type ).tokens;
					if( Arrays.deepEquals( t, annotationTypeFqn ) )
					{
						annotations = originalAnnotationArray;
						break;
					}
				}

				if( ann.type instanceof SingleTypeReference )
				{
					char[] lastToken = ( (SingleTypeReference) ann.type ).token;
					if( Arrays.equals( lastToken, simpleName ) )
					{
						annotations = originalAnnotationArray;
						break;
					}
				}
			}
		}
		return annotations;
	}

}
