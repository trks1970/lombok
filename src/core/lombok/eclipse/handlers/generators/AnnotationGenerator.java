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
		
	public Annotation createAnnotation( TypeDeclaration typeDecl, String annotationFQN )
	{
		return createAnnotation( typeDecl, annotationFQN, new ArrayList<ASTNode>() );
	}

	public  Annotation createAnnotation( TypeDeclaration typeDecl, String annotationFQN, ASTNode arg )
	{
		List<ASTNode> argList = new ArrayList<ASTNode>();
		argList.add(  arg );
		return createAnnotation( typeDecl, annotationFQN, argList );
	}

	public Annotation createAnnotation( TypeDeclaration typeDecl, String annotationFQN, List<ASTNode> argList )
	{
		Annotation ann = doCreateAnnotation( typeDecl, annotationFQN, argList );
		Annotation[] annotations = createAnnotationArray( typeDecl, ann );
		injectAnnotation( typeDecl, annotations );
		return ann;
	}
	
	public Annotation createAnnotation( FieldDeclaration fieldDecl, String annotationFQN )
	{
		return createAnnotation( fieldDecl, annotationFQN, new ArrayList<ASTNode>() );
	}

	public Annotation createAnnotation( FieldDeclaration fieldDecl, String annotationFQN, ASTNode arg )
	{
		List<ASTNode> argList = new ArrayList<ASTNode>();
		argList.add(  arg );
		return createAnnotation( fieldDecl, annotationFQN, argList );
	}

	public Annotation createAnnotation( FieldDeclaration fieldDecl, String annotationFQN, List<ASTNode> argList )
	{
		Annotation ann = doCreateAnnotation( fieldDecl, annotationFQN, argList );
		Annotation[] annotations = createAnnotationArray( fieldDecl, ann );
		injectAnnotation( fieldDecl, annotations );
		return ann;
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
		Annotation[] newAnnotationArray = new Annotation[] {};
		if( !annotationExists( node, ann.type.getTypeName() ) )
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
	
	public  Annotation doCreateAnnotation(ASTNode source, String annotationFQN, List<ASTNode> argList )
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
	
	
	private  boolean annotationExists( ASTNode node, char[][] annotationTypeFqn )
	{
		boolean retVal = false;
		Annotation[]  originalAnnotationArray = getCurrentAnnotations( node );
		char[] simpleName = annotationTypeFqn[annotationTypeFqn.length - 1];
		if( originalAnnotationArray != null )
		{
			for( Annotation ann : originalAnnotationArray )
			{
				if( ann.type instanceof QualifiedTypeReference )
				{
					char[][] t = ( (QualifiedTypeReference) ann.type ).tokens;
					if( Arrays.deepEquals( t, annotationTypeFqn ) )
					{
						retVal = true;
						break;
					}
				}
	
				if( ann.type instanceof SingleTypeReference )
				{
					char[] lastToken = ( (SingleTypeReference) ann.type ).token;
					if( Arrays.equals( lastToken, simpleName ) )
					{
						retVal = true;
						break;
					}
				}
			}
		}
		return retVal;
	}
		
	private Annotation[] getCurrentAnnotations(ASTNode node)
	{
		Annotation[] originalAnnotationArray = new Annotation[] {};
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
		return originalAnnotationArray;
	}
	
	private void injectAnnotation( ASTNode node, Annotation[] annotationArray )
	{
		if( node instanceof FieldDeclaration )
		{
			((FieldDeclaration)node).annotations = annotationArray;
		}
		else if( node instanceof MethodDeclaration )
		{
			((MethodDeclaration)node).annotations = annotationArray;
		}
		else if( node instanceof ConstructorDeclaration )
		{
			((ConstructorDeclaration)node).annotations = annotationArray;
		}
		else if( node instanceof TypeDeclaration )
		{
			((TypeDeclaration)node).annotations = annotationArray;
		}
		else
		{
			throw new IllegalStateException("Unsupported ASTNode <" + node.getClass().getName() + "> for annotation injection." );
		}
	}


}
