package lombok.eclipse.handlers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.mangosdk.spi.ProviderFor;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import static lombok.eclipse.handlers.HandleLog.createTypeReference;

import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;
import lombok.experimental.SequencedEntity;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleSequencedEntity extends EclipseAnnotationHandler<SequencedEntity>
{
	@Override
	public void handle( AnnotationValues<SequencedEntity> annotation, Annotation ast, EclipseNode annotationNode )
	{
		EclipseNode typeNode = annotationNode.up();
		TypeDeclaration typeDecl = (TypeDeclaration) typeNode.get();
		addAnnotation( typeDecl, "javax.persistence.Entity" );
		String idFieldName = annotation.getInstance().id();
		String versionFieldName = annotation.getInstance().version();
		if (fieldExists(idFieldName, typeNode) != MemberExistsResult.NOT_EXISTS) {
			annotationNode.addWarning("Field '" + idFieldName + "' already exists.");
			return;
		}
		if (fieldExists(versionFieldName, typeNode) != MemberExistsResult.NOT_EXISTS) {
			annotationNode.addWarning("Field '" + versionFieldName + "' already exists.");
			return;
		}
		// add field private Long <id>
		FieldDeclaration idFieldDecl = new FieldDeclaration(idFieldName.toCharArray(), 0, -1);
		setGeneratedBy(idFieldDecl, ast);
		idFieldDecl.declarationSourceEnd = -1;
		idFieldDecl.modifiers = Modifier.PRIVATE;
		idFieldDecl.type = createTypeReference("java.lang.Long", ast);
		injectField(typeNode, idFieldDecl);
		// add @javax.persistence.Id to field
		addAnnotation( idFieldDecl, "javax.persistence.Id" );
		
		// add @javax.persistence.SequenceGenerator(name = "<typeName>Gen",sequenceName = "seq_<typeName>")
		String typeName = typeNode.getName().toLowerCase();
		addSequenceAnnotation(idFieldDecl, typeName );


	}
	
	private void addAnnotation( TypeDeclaration typeDecl, String annotation )
	{
		addAnnotation( typeDecl, typeDecl.annotations, Eclipse.fromQualifiedName(annotation), null );
	}

	private void addAnnotation( FieldDeclaration fieldDecl, String annotation )
	{
		addAnnotation( fieldDecl, fieldDecl.annotations, Eclipse.fromQualifiedName(annotation), null );
	}

	private void addSequenceAnnotation( FieldDeclaration fieldDecl, String typeName )
	{
		MemberValuePair name = new MemberValuePair( "name".toCharArray(), 0, 0, new StringLiteral( (typeName + "Gen").toCharArray(), 0,0,0 ) );
		MemberValuePair sequenceName = new MemberValuePair( "sequenceName".toCharArray(), 0, 0, new StringLiteral( ("seq_"+ typeName).toCharArray(), 0,0,0 ) );
		List<MemberValuePair> argList = new ArrayList<>();
		argList.add( name );
		argList.add( sequenceName );
		addAnnotation( fieldDecl, fieldDecl.annotations, Eclipse.fromQualifiedName("javax.persistence.SequenceGenerator"), null );
	}

	private static Annotation[] addAnnotation( ASTNode source, Annotation[] originalAnnotationArray, char[][] annotationTypeFqn, List<ASTNode> argList )
	{
		char[] simpleName = annotationTypeFqn[annotationTypeFqn.length - 1];

		if( originalAnnotationArray != null )
			for( Annotation ann : originalAnnotationArray )
			{
				if( ann.type instanceof QualifiedTypeReference )
				{
					char[][] t = ( (QualifiedTypeReference) ann.type ).tokens;
					if( Arrays.deepEquals( t, annotationTypeFqn ) )
						return originalAnnotationArray;
				}

				if( ann.type instanceof SingleTypeReference )
				{
					char[] lastToken = ( (SingleTypeReference) ann.type ).token;
					if( Arrays.equals( lastToken, simpleName ) )
						return originalAnnotationArray;
				}
			}

		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		long[] poss = new long[annotationTypeFqn.length];
		Arrays.fill( poss, p );
		QualifiedTypeReference qualifiedType = new QualifiedTypeReference( annotationTypeFqn, poss );
		setGeneratedBy( qualifiedType, source );
		Annotation ann = null;
		if( argList != null )
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
		if( originalAnnotationArray == null )
			return new Annotation[] { ann };
		Annotation[] newAnnotationArray = new Annotation[originalAnnotationArray.length + 1];
		System.arraycopy( originalAnnotationArray, 0, newAnnotationArray, 0, originalAnnotationArray.length );
		newAnnotationArray[originalAnnotationArray.length] = ann;
		return newAnnotationArray;
	}

}
