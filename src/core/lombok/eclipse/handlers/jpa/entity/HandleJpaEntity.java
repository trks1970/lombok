package lombok.eclipse.handlers.jpa.entity;

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

import lombok.AccessLevel;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleGetter;
import lombok.eclipse.handlers.HandleSetter;
import lombok.eclipse.handlers.generators.AnnotationGenerator;
import lombok.eclipse.handlers.generators.FieldGenerator;
import lombok.eclipse.handlers.generators.MemberValuePairGenerator;
import lombok.experimental.jpa.entity.GenerationType;
import lombok.experimental.jpa.entity.LombokJpaEntity;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleJpaEntity extends EclipseAnnotationHandler<LombokJpaEntity>
{
	private HandleGetter getterHandler = new HandleGetter();
	private HandleSetter setterHandler = new HandleSetter();
	
	private AnnotationGenerator annotationGen = AnnotationGenerator.instance();
	private MemberValuePairGenerator mvpGen = MemberValuePairGenerator.instance();
	private FieldGenerator fieldGen = FieldGenerator.instance();
		
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

	void addTableAnnotation( Annotation ast, LombokJpaEntity a, TypeDeclaration typeDecl )
	{
		List<ASTNode> tableArgs = new ArrayList<ASTNode>();
		if( !a.table().isEmpty() )
		{
			tableArgs = mvpGen.createMemberValuePair( "name", a.table(), new ArrayList<ASTNode>() );
		}
		if( !a.catalog().isEmpty() )
		{
			tableArgs = mvpGen.createMemberValuePair( "catalog", a.catalog(), tableArgs );
		}
		if( !a.schema().isEmpty() )
		{
			tableArgs = mvpGen.createMemberValuePair( "schema", a.schema(), tableArgs );
		}
		List<Annotation> indexes = new ArrayList<Annotation>();
		if( 0 < a.indexes().length )
		{
			for( int i = 0; i < a.indexes().length; i++ )
			{
				List<ASTNode> indexArgs = new ArrayList<ASTNode>();
				if( !a.indexes()[i].name().isEmpty() )
				{
					indexArgs = mvpGen.createMemberValuePair( "name", a.indexes()[i].name(), indexArgs );
				}
				indexArgs = mvpGen.createMemberValuePair( "columns", a.indexes()[i].columns(), indexArgs );
				if( a.indexes()[i].unique() )
				{
					indexArgs = mvpGen.createMemberValuePair( "unique", a.indexes()[i].unique(), indexArgs );
				}
				Annotation idxAnn = annotationGen.createAnnotation(ast, "javax.persistence.Index", indexArgs);
				indexes.add( idxAnn );
			}
			MemberValuePair idxArray = mvpGen.createMemberValuePair( "indexes", indexes );
			tableArgs.add( idxArray );
		}
		annotationGen.addAnnotation( typeDecl, "javax.persistence.Table", tableArgs );
	}

	private EclipseNode addVersionField( Annotation ast, EclipseNode annotationNode, EclipseNode typeNode, LombokJpaEntity a )
	{
		FieldDeclaration versionFieldDecl = fieldGen.createField( Modifier.PRIVATE, a.versionField(), a.versionType().type(), ast, annotationNode );
		EclipseNode versionNode = injectField(typeNode, versionFieldDecl);
		// add @javax.persistence.Version
		annotationGen.addAnnotation( versionFieldDecl, "javax.persistence.Version" );
		if( !a.versionColumn().isEmpty() )
		{
			// add @javax.persistence.Column(name = "<version>")
			annotationGen.addAnnotation( versionFieldDecl, "javax.persistence.Column", mvpGen.createMemberValuePair( "name", a.versionColumn() ) );
		}
		return versionNode;
	}

	private EclipseNode addIDField( Annotation source, EclipseNode annotationNode, EclipseNode typeNode, LombokJpaEntity a )
	{
		FieldDeclaration idFieldDecl = fieldGen.createField( Modifier.PRIVATE, a.idField(), a.idType().type(), source, annotationNode );
		EclipseNode idNode = injectField(typeNode, idFieldDecl);
		// add @javax.persistence.Id to field
		annotationGen.addAnnotation( idFieldDecl, "javax.persistence.Id" );
		if( !a.idColumn().isEmpty() )
		{
			// add @javax.persistence.Column(name = "<id>")
			annotationGen.addAnnotation( idFieldDecl, "javax.persistence.Column", mvpGen.createMemberValuePair( "name", a.idColumn() ) );
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
			List<ASTNode> valueArgs = mvpGen.createMemberValuePair( "strategy", a.idGeneration().generator(), new ArrayList<ASTNode>() );
			valueArgs = mvpGen.createMemberValuePair( "generator", generatorName, valueArgs );
			annotationGen.addAnnotation( idFieldDecl, "javax.persistence.GeneratedValue", valueArgs );
			// add @javax.persistence.SequenceGenerator(name = "<idSequence>Gen",sequenceName = "idSequence")
			List<ASTNode> generatorArgs = mvpGen.createMemberValuePair( "name", generatorName, new ArrayList<ASTNode>() );
			generatorArgs = mvpGen.createMemberValuePair( "sequenceName", sequenceName, valueArgs );
			annotationGen.addAnnotation( idFieldDecl, "javax.persistence.SequenceGenerator", generatorArgs );
			
		}
		else 
		{
			// add @javax.persistence.GeneratedValue(strategy = <idGeneration>)
			annotationGen.addAnnotation( idFieldDecl, "javax.persistence.GeneratedValue", mvpGen.createMemberValuePair( "strategy", a.idGeneration().generator() ) );
		}
		return idNode;
	}
	
}
