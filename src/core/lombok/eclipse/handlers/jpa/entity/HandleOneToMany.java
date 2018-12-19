package lombok.eclipse.handlers.jpa.entity;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.mangosdk.spi.ProviderFor;


import static lombok.eclipse.handlers.EclipseHandlerUtil.injectField;

import lombok.AccessLevel;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil;
import lombok.eclipse.handlers.HandleGetter;
import lombok.eclipse.handlers.HandleSetter;
import lombok.eclipse.handlers.generators.AnnotationGenerator;
import lombok.eclipse.handlers.generators.FieldGenerator;
import lombok.eclipse.handlers.generators.MemberValuePairGenerator;
import lombok.experimental.jpa.LombokOneToMany;
import lombok.experimental.jpa.entity.IDGenerator;
import lombok.experimental.jpa.entity.LombokJpaEntity;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleOneToMany extends EclipseAnnotationHandler<LombokOneToMany>
{
	private HandleGetter getterHandler = new HandleGetter();
	private HandleSetter setterHandler = new HandleSetter();
	
	private AnnotationGenerator annotationGen = AnnotationGenerator.instance();
	private MemberValuePairGenerator mvpGen = MemberValuePairGenerator.instance();
	private FieldGenerator fieldGen = FieldGenerator.instance();
		
	@Override
	public void handle( AnnotationValues<LombokOneToMany> annotation, Annotation ast, EclipseNode annotationNode )
	{
		EclipseNode fieldNode = EclipseHandlerUtil.upToFieldNode( annotationNode );
		FieldDeclaration fieldDecl = (FieldDeclaration) fieldNode.get();
		LombokOneToMany a = annotation.getInstance();

		annotationGen.createAnnotation( fieldDecl, LombokOneToMany.ONE_TO_MANY );
		
		if( a.bidirectional() )
		{
			// add utility methods
		}
		else
		{
			// add joinColumn annotation
		}
		


		
		/*getterHandler.createGetterForField( AccessLevel.PUBLIC, idNode, annotationNode, ast, true, false, new ArrayList<Annotation>() );
		setterHandler.createSetterForField( AccessLevel.PUBLIC, idNode, annotationNode, true, new ArrayList<Annotation>(), new ArrayList<Annotation>() );
		getterHandler.createGetterForField( AccessLevel.PUBLIC, versionNode, annotationNode, ast, true, false, new ArrayList<Annotation>() );
		setterHandler.createSetterForField( AccessLevel.PUBLIC, versionNode, annotationNode, true, new ArrayList<Annotation>(), new ArrayList<Annotation>() );*/
		
	}

/*	void addTableAnnotation( Annotation ast, LombokJpaEntity a, TypeDeclaration typeDecl )
	{
		List<ASTNode> tableArgs = new ArrayList<ASTNode>();
		if( !a.table().isEmpty() )
		{
			tableArgs = mvpGen.addStringParameter( "name", a.table(), new ArrayList<ASTNode>() );
		}
		if( !a.catalog().isEmpty() )
		{
			tableArgs = mvpGen.addStringParameter( "catalog", a.catalog(), tableArgs );
		}
		if( !a.schema().isEmpty() )
		{
			tableArgs = mvpGen.addStringParameter( "schema", a.schema(), tableArgs );
		}
		List<Annotation> indexes = new ArrayList<Annotation>();
		if( 0 < a.indexes().length )
		{
			for( int i = 0; i < a.indexes().length; i++ )
			{
				List<ASTNode> indexArgs = new ArrayList<ASTNode>();
				if( !a.indexes()[i].name().isEmpty() )
				{
					indexArgs = mvpGen.addStringParameter( "name", a.indexes()[i].name(), indexArgs );
				}
				indexArgs = mvpGen.addStringParameter( "columnList", a.indexes()[i].columns(), indexArgs );
				if( a.indexes()[i].unique() )
				{
					indexArgs = mvpGen.addBooleanAttribute( "unique", a.indexes()[i].unique(), indexArgs );
				}
				Annotation idxAnn = annotationGen.doCreateAnnotation(ast, "javax.persistence.Index", indexArgs);
				indexes.add( idxAnn );
			}
			MemberValuePair idxArray = mvpGen.createArrayParameter( "indexes", indexes );
			tableArgs.add( idxArray );
		}
		annotationGen.createAnnotation( typeDecl, "javax.persistence.Table", tableArgs );
	} */

	private EclipseNode addVersionField( Annotation ast, EclipseNode annotationNode, EclipseNode typeNode, LombokJpaEntity a )
	{
		FieldDeclaration versionFieldDecl = fieldGen.createField( Modifier.PRIVATE, a.versionField(), a.versionType().type(), ast, annotationNode );
		EclipseNode versionNode = injectField(typeNode, versionFieldDecl);
		// add @javax.persistence.Version
		annotationGen.createAnnotation( versionFieldDecl, "javax.persistence.Version" );
		if( !a.versionColumn().isEmpty() )
		{
			// add @javax.persistence.Column(name = "<version>")
			annotationGen.createAnnotation( versionFieldDecl, "javax.persistence.Column", mvpGen.createStringParameter( "name", a.versionColumn() ) );
		}
		return versionNode;
	}

	private EclipseNode addIDField( Annotation source, EclipseNode annotationNode, EclipseNode typeNode, LombokJpaEntity a )
	{
		FieldDeclaration idFieldDecl = fieldGen.createField( Modifier.PRIVATE, a.idField(), a.idType().type(), source, annotationNode );
		EclipseNode idNode = injectField(typeNode, idFieldDecl);
		// add @javax.persistence.Id to field
		annotationGen.createAnnotation( idFieldDecl, "javax.persistence.Id" );
		if( !a.idColumn().isEmpty() )
		{
			// add @javax.persistence.Column(name = "<id>")
			annotationGen.createAnnotation( idFieldDecl, "javax.persistence.Column", mvpGen.createStringParameter( "name", a.idColumn() ) );
		}
		if( !a.idSequence().isEmpty() || IDGenerator.SEQUENCE.equals( a.idGeneration() ) )
		{	
			String sequenceName = a.idSequence();
			if( sequenceName.isEmpty() )
			{
				sequenceName = typeNode.getName().toLowerCase();
			}
			String generatorName = sequenceName + "Gen";
			
			// add @javax.persistence.GeneratedValue(strategy = <idGeneration>, generator = "<idSequence>Gen")
			List<ASTNode> valueArgs = mvpGen.addNameReference( "strategy", a.idGeneration().generator(), new ArrayList<ASTNode>() );
			valueArgs = mvpGen.addStringParameter( "generator", generatorName, valueArgs );
			annotationGen.createAnnotation( idFieldDecl, "javax.persistence.GeneratedValue", valueArgs );
			
			// add @javax.persistence.SequenceGenerator(name = "<idSequence>Gen",sequenceName = "idSequence")
			List<ASTNode> generatorArgs = mvpGen.addStringParameter( "name", generatorName, new ArrayList<ASTNode>() );
			generatorArgs = mvpGen.addStringParameter( "sequenceName", sequenceName, generatorArgs );
			annotationGen.createAnnotation( idFieldDecl, "javax.persistence.SequenceGenerator", generatorArgs );
			
		}
		else 
		{
			// add @javax.persistence.GeneratedValue(strategy = <idGeneration>)
			annotationGen.createAnnotation( idFieldDecl, "javax.persistence.GeneratedValue", 
					mvpGen.createNameReference( "strategy", a.idGeneration().generator() ) );
		}
		return idNode;
	}
	
}
