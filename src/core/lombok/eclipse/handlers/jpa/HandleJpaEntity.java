package lombok.eclipse.handlers.jpa;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.mangosdk.spi.ProviderFor;


import static lombok.eclipse.handlers.EclipseHandlerUtil.injectField;
import static lombok.eclipse.handlers.generators.FieldGenerator.createField;
import static lombok.eclipse.handlers.generators.MemberValuePairGenerator.createMemberValuePair;

import lombok.AccessLevel;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleGetter;
import lombok.eclipse.handlers.HandleSetter;
import lombok.eclipse.handlers.generators.AnnotationGenerator;
import lombok.experimental.jpa.GenerationType;
import lombok.experimental.jpa.LombokJpaEntity;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleJpaEntity extends EclipseAnnotationHandler<LombokJpaEntity>
{
	private HandleGetter getterHandler = new HandleGetter();
	private HandleSetter setterHandler = new HandleSetter();
	
	private AnnotationGenerator annotationGen = new AnnotationGenerator();
		
	@Override
	public void handle( AnnotationValues<LombokJpaEntity> annotation, Annotation ast, EclipseNode annotationNode )
	{
		EclipseNode typeNode = annotationNode.up();
		TypeDeclaration typeDecl = (TypeDeclaration) typeNode.get();
		annotationGen.addAnnotation( typeDecl, "javax.persistence.Entity" );
		LombokJpaEntity a = annotation.getInstance();
		// add ID field
		EclipseNode idNode = addIDField( ast, annotationNode, typeNode, a );		
		// add field private Integer <version>
		EclipseNode versionNode = addVersionField( ast, annotationNode, typeNode, a );
		
		addTableAnnotation( ast, a, typeDecl );

		
		getterHandler.createGetterForField( AccessLevel.PUBLIC, idNode, annotationNode, ast, true, false, new ArrayList<Annotation>() );
		setterHandler.createSetterForField( AccessLevel.PUBLIC, idNode, annotationNode, true, new ArrayList<Annotation>(), new ArrayList<Annotation>() );
		getterHandler.createGetterForField( AccessLevel.PUBLIC, versionNode, annotationNode, ast, true, false, new ArrayList<Annotation>() );
		setterHandler.createSetterForField( AccessLevel.PUBLIC, versionNode, annotationNode, true, new ArrayList<Annotation>(), new ArrayList<Annotation>() );
	}

	Annotation[] addTableAnnotation( Annotation ast, LombokJpaEntity a, TypeDeclaration typeDecl )
	{
		List<ASTNode> tableArgs = new ArrayList<ASTNode>();
		if( !a.table().isEmpty() )
		{
			tableArgs = createMemberValuePair( "name", a.table(), new ArrayList<ASTNode>() );
		}
		if( !a.catalog().isEmpty() )
		{
			tableArgs = createMemberValuePair( "catalog", a.catalog(), tableArgs );
		}
		if( !a.schema().isEmpty() )
		{
			tableArgs = createMemberValuePair( "schema", a.schema(), tableArgs );
		}
		List<Annotation> indexes = new ArrayList<Annotation>();
		if( 0 < a.indexes().length )
		{
			for( int i = 0; i < a.indexes().length; i++ )
			{
				List<ASTNode> indexArgs = new ArrayList<ASTNode>();
				if( !a.indexes()[i].name().isEmpty() )
				{
					indexArgs = createMemberValuePair( "name", a.indexes()[i].name(), indexArgs );
				}
				indexArgs = createMemberValuePair( "columns", a.indexes()[i].columns(), indexArgs );
				if( a.indexes()[i].unique() )
				{
					indexArgs = createMemberValuePair( "unique", a.indexes()[i].unique(), indexArgs );
				}
				Annotation idxAnn = annotationGen.createAnnotation(ast, "javax.persistence.Index", indexArgs);
				indexes.add( idxAnn );
			}
			MemberValuePair idxArray = createMemberValuePair( "indexes", indexes );
			tableArgs.add( idxArray );
		}
		return annotationGen.addAnnotation( typeDecl, "javax.persistence.Table", tableArgs );
	}

	private EclipseNode addVersionField( Annotation ast, EclipseNode annotationNode, EclipseNode typeNode, LombokJpaEntity a )
	{
		FieldDeclaration versionFieldDecl = createField( Modifier.PRIVATE, a.versionField(), a.versionType().type(), ast, annotationNode );
		EclipseNode versionNode = injectField(typeNode, versionFieldDecl);
		// add @javax.persistence.Version
		annotationGen.addAnnotation( versionFieldDecl, "javax.persistence.Version" );
		// add @javax.persistence.Column(name = "<version>")
		annotationGen.addAnnotation( versionFieldDecl, "javax.persistence.Column", createMemberValuePair( "name", a.versionColumn() ) );
		return versionNode;
	}

	private EclipseNode addIDField( Annotation source, EclipseNode annotationNode, EclipseNode typeNode, LombokJpaEntity a )
	{
		FieldDeclaration idFieldDecl = createField( Modifier.PRIVATE, a.idField(), a.idType().type(), source, annotationNode );
		EclipseNode idNode = injectField(typeNode, idFieldDecl);
		// add @javax.persistence.Id to field
		annotationGen.addAnnotation( idFieldDecl, "javax.persistence.Id" );
		if( !a.idColumn().isEmpty() )
		{
			// add @javax.persistence.Column(name = "<id>")
			annotationGen.addAnnotation( idFieldDecl, "javax.persistence.Column", createMemberValuePair( "name", a.idColumn() ) );
		}
		if( !a.idSequence().isEmpty() || GenerationType.SEQUENCE.equals( a.idGeneration() ) )
		{	
			String sequenceName = a.idSequence();
			if( sequenceName.isEmpty() )
			{
				sequenceName = typeNode.getName().toLowerCase();
			}
			String generatorName = sequenceName + "Gen";
			
			// add @javax.persistence.GeneratedValue(strategy = <idGeneration>, generator = "<idSequence>Gen")
			List<ASTNode> valueArgs = createMemberValuePair( "strategy", a.idGeneration().generator(), new ArrayList<ASTNode>() );
			valueArgs = createMemberValuePair( "generator", generatorName, valueArgs );
			annotationGen.addAnnotation( idFieldDecl, "javax.persistence.GeneratedValue", valueArgs );
			// add @javax.persistence.SequenceGenerator(name = "<idSequence>Gen",sequenceName = "idSequence")
			List<ASTNode> generatorArgs = createMemberValuePair( "name", generatorName, new ArrayList<ASTNode>() );
			generatorArgs = createMemberValuePair( "sequenceName", sequenceName, valueArgs );
			annotationGen.addAnnotation( idFieldDecl, "javax.persistence.SequenceGenerator", generatorArgs );
			
		}
		else 
		{
			// add @javax.persistence.GeneratedValue(strategy = <idGeneration>)
			annotationGen.addAnnotation( idFieldDecl, "javax.persistence.GeneratedValue", createMemberValuePair( "strategy", a.idGeneration().generator() ) );
		}
		return idNode;
	}
	
}
