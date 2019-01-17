package lombok.eclipse.handlers.jpa.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.mangosdk.spi.ProviderFor;

import lombok.core.AnnotationValues;
import lombok.core.LombokLogger;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil;
import lombok.experimental.jpa.repository.LombokSpringJpaRepository;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleSpringJpaRepository extends EclipseAnnotationHandler<LombokSpringJpaRepository>
{
	private static final Logger LOG = LombokLogger.getLogger();
	
	@Override
	public void handle( AnnotationValues<LombokSpringJpaRepository> annotation, Annotation ast, EclipseNode annotationNode )
	{
		LOG.fine( "handle enter" );
		TypeDeclaration typeDecl = (TypeDeclaration) EclipseHandlerUtil.upToTypeNode( annotationNode ).get();
		int pS = typeDecl.sourceStart, pE = typeDecl.sourceEnd;
		long p = (long) pS << 32 | pE;
		LOG.fine( "instantiate" );
		LombokSpringJpaRepository a = annotation.getInstance();
		LOG.fine( "instantiated " );
		List<TypeParameter> jpaParams = new ArrayList<TypeParameter>();
		TypeParameter entity = new TypeParameter();
		LOG.fine( "entity" );
		entity.name = a.entity().toCharArray();
		TypeParameter pkType = new TypeParameter();
		LOG.fine( "pkType" );
		pkType.name = a.pkType().toCharArray();
		jpaParams.add( entity );
		jpaParams.add( pkType );
		TypeReference jpaRep = namePlusTypeParamsToTypeReference( LombokSpringJpaRepository.JPA_REPOSITORY.toCharArray(), jpaParams.toArray( new TypeParameter[jpaParams.size()]), p );
		List<TypeReference> references = new ArrayList<TypeReference>();
		references.add( jpaRep );
		
		List<TypeParameter> specParams = new ArrayList<TypeParameter>();
		specParams.add( entity );
		TypeReference specRep = namePlusTypeParamsToTypeReference( LombokSpringJpaRepository.SPEC_EXECUTOR.toCharArray(), specParams.toArray( new TypeParameter[specParams.size()]), p );
		references.add( specRep );
		
		typeDecl.superInterfaces = references.toArray( new TypeReference[references.size() ] );
		LOG.fine( "handle exit" );
	}
	
	public TypeReference namePlusTypeParamsToTypeReference( char[] typeName, TypeParameter[] params, long p )
	{
		if( params != null && params.length > 0 )
		{
			TypeReference[] refs = new TypeReference[params.length];
			int idx = 0;
			for( TypeParameter param : params )
			{
				TypeReference typeRef = new SingleTypeReference( param.name, p );
				refs[idx++] = typeRef;
			}
			return new ParameterizedSingleTypeReference( typeName, refs, 0, p );
		}

		return new SingleTypeReference( typeName, p );
	}


}
