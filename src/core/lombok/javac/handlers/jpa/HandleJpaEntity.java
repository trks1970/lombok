package lombok.javac.handlers.jpa;


import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

import static lombok.javac.handlers.JavacHandlerUtil.*;
import static lombok.javac.handlers.generators.AnnotationGenerator.addAnnotation;
import static lombok.javac.handlers.generators.FieldGenerator.createField;


import static lombok.javac.handlers.generators.AnnotationParameterGenerator.createParameter;
import static lombok.javac.handlers.generators.AnnotationParameterGenerator.createTypeRefParameter;

import lombok.AccessLevel;
import lombok.core.AnnotationValues;
import lombok.experimental.jpa.GenerationType;
import lombok.experimental.jpa.LombokJpaEntity;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.HandleGetter;
import lombok.javac.handlers.HandleSetter;
import lombok.javac.handlers.JavacHandlerUtil;

/**
 * Handles the {@code lombok.experimental.SequencedEntity} annotation for javac.
 */
@ProviderFor( JavacAnnotationHandler.class )
public class HandleJpaEntity extends JavacAnnotationHandler<LombokJpaEntity>
{
	private HandleGetter getterHandler = new HandleGetter();
	private HandleSetter setterHandler = new HandleSetter();
	
	@Override
	public void handle( AnnotationValues<LombokJpaEntity> annotation, JCAnnotation ast, JavacNode annotationNode )
	{
		deleteAnnotationIfNeccessary(annotationNode, LombokJpaEntity.class);
		// add @javax.persistence.Entity on type
		JavacNode typeNode = annotationNode.up();
		JCClassDecl typeDecl = (JCClassDecl) typeNode.get();
		addAnnotation( typeDecl, typeNode, "javax.persistence.Entity" );
		LombokJpaEntity a = annotation.getInstance();
		// add ID field
		JavacNode idNode = addIDField( ast, annotationNode, typeNode, a );
		
		// add Version field
		JavacNode versionNode = addVersionField( ast, annotationNode, typeNode, a );

		// add Table annotation		
		addTableAnnotation( typeNode, typeDecl, a );
		
		// add getter and setter for id and version
		getterHandler.createGetterForField(AccessLevel.PUBLIC, idNode, annotationNode, true, false, List.<JCAnnotation> nil() );
		setterHandler.createSetterForField(AccessLevel.PUBLIC, idNode, annotationNode, true, List.<JCAnnotation> nil(), List.<JCAnnotation> nil() );
		getterHandler.createGetterForField(AccessLevel.PUBLIC, versionNode, annotationNode, true, false, List.<JCAnnotation> nil() );
		setterHandler.createSetterForField(AccessLevel.PUBLIC, versionNode, annotationNode, true, List.<JCAnnotation> nil(), List.<JCAnnotation> nil() );
		
	}

	private void addTableAnnotation( JavacNode typeNode, JCClassDecl typeDecl, LombokJpaEntity a )
	{
		List<JCExpression> tableArgs = List.<JCExpression> nil();
		if( !a.table().isEmpty() )
		{
			tableArgs = createParameter(typeNode, "name", a.table(), tableArgs );
		}
		if( !a.catalog().isEmpty() )
		{
			tableArgs = createParameter( typeNode, "catalog", a.catalog(), tableArgs );
		}
		if( !a.schema().isEmpty() )
		{
			tableArgs = createParameter( typeNode, "schema", a.schema(), tableArgs );
		}
		List<JCExpression> indexes = List.<JCExpression> nil();
		if( 0 < a.indexes().length )
		{
			for( int i = 0; i < a.indexes().length; i++ )
			{
				List<JCExpression> indexArgs = List.<JCExpression> nil();
				if( !a.indexes()[i].name().isEmpty() )
				{
					indexArgs = createParameter( typeNode, "name", a.indexes()[i].name(), indexArgs );
				}
				indexArgs = createParameter( typeNode, "columns", a.indexes()[i].columns(), indexArgs );
				if( !a.indexes()[i].unique() )
				{
					indexArgs = createParameter( typeNode, "unique", a.indexes()[i].unique(), indexArgs );
				}
				JCAnnotation index = typeNode.getTreeMaker().Annotation( JavacHandlerUtil.chainDotsString( typeNode, "javax.persistence.Index" ), indexArgs );
				indexes = indexes.append( index );
			}
			JavacTreeMaker maker = typeNode.getTreeMaker();
			List<JCExpression> dims = List.<JCExpression> nil();
			dims = dims.append( maker.Literal( new Integer( indexes.size() ) ) );
			JCNewArray array = maker.NewArray( null, null, indexes );
			JCExpression parameter = maker.Assign( maker.Ident(typeNode.toName( "indexes" )), array );
			tableArgs = tableArgs.append( parameter );
		}
		if( 0 < tableArgs.size() )
		{
			addAnnotation( typeDecl, typeNode, "javax.persistence.Table", tableArgs );
		}
	}

	private JavacNode addVersionField(JCAnnotation source, JavacNode annotationNode, JavacNode typeNode, LombokJpaEntity a)
	{
		JCVariableDecl versionFieldDecl = createField( Flags.PRIVATE, a.versionField(), a.versionType().type(), source, annotationNode );
		JavacNode versionNode = injectFieldAndMarkGenerated( typeNode, versionFieldDecl);
		// add @javax.persistence.Version
		addAnnotation( versionFieldDecl, typeNode, "javax.persistence.Version" );
		// add @javax.persistence.Column(name = "<version>")
		if( !a.versionColumn().isEmpty() )
		{
			addAnnotation( versionFieldDecl, typeNode, "javax.persistence.Column", createParameter( typeNode, "name", a.versionColumn() ) );
		}
		return versionNode;
	}


	private JavacNode addIDField(JCAnnotation source, JavacNode annotationNode, JavacNode typeNode, LombokJpaEntity a) 
	{
		JCVariableDecl fieldDecl = createField( Flags.PRIVATE, a.idField(), a.idType().type(), source, annotationNode );
		JavacNode node = injectFieldAndMarkGenerated( typeNode, fieldDecl );
		// add @javax.persistence.Id to field
		addAnnotation(fieldDecl, typeNode, "javax.persistence.Id");
		if( !a.idColumn().isEmpty() )
		{
			// add @javax.persistence.Column(name = "<id>")
			addAnnotation( fieldDecl, typeNode, "javax.persistence.Column", createParameter( typeNode, "name", a.idColumn() ) );
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
			List<JCExpression> valueArgs = createTypeRefParameter( typeNode, "strategy", a.idGeneration().generator(), List.<JCExpression> nil() );
			valueArgs = createParameter( typeNode, "generator", generatorName, valueArgs );
			addAnnotation( fieldDecl, typeNode, "javax.persistence.GeneratedValue", valueArgs );
			// add @javax.persistence.SequenceGenerator(name = "<idSequence>Gen",sequenceName = "idSequence")
			List<JCExpression> generatorArgs = createParameter( typeNode, "name", generatorName, List.<JCExpression> nil() );
			generatorArgs = createParameter( typeNode, "sequenceName", sequenceName, valueArgs );
			addAnnotation( fieldDecl, typeNode, "javax.persistence.SequenceGenerator", generatorArgs );
		}
		else 
		{
			// add @javax.persistence.GeneratedValue(strategy = <idGeneration>)
			addAnnotation( fieldDecl, typeNode, "javax.persistence.GeneratedValue", createTypeRefParameter( typeNode, "strategy", a.idGeneration().generator() ) );
		}
		return node;
	}
}
