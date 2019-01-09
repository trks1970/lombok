package lombok.javac.handlers.jpa.entity;


import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

import static lombok.javac.handlers.JavacHandlerUtil.*;
import lombok.javac.handlers.generators.AnnotationGenerator;
import lombok.javac.handlers.generators.FieldGenerator;
import lombok.javac.handlers.generators.AnnotationParameterGenerator;

import lombok.AccessLevel;
import lombok.core.AnnotationValues;
import lombok.experimental.jpa.entity.IDGenerator;
import lombok.experimental.jpa.entity.LombokJpaEntity;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.HandleGetter;
import lombok.javac.handlers.HandleSetter;

/**
 * Handles the {@code lombok.experimental.SequencedEntity} annotation for javac.
 */
@ProviderFor( JavacAnnotationHandler.class )
public class HandleJpaEntity extends JavacAnnotationHandler<LombokJpaEntity>
{
	private HandleGetter getterHandler = new HandleGetter();
	private HandleSetter setterHandler = new HandleSetter();
	
	private AnnotationGenerator annGen = AnnotationGenerator.instance();
	private AnnotationParameterGenerator parGen = AnnotationParameterGenerator.instance();
	private FieldGenerator fieldGen = FieldGenerator.instance();

	
	@Override
	public void handle( AnnotationValues<LombokJpaEntity> annotation, JCAnnotation ast, JavacNode annotationNode )
	{
		deleteAnnotationIfNeccessary(annotationNode, LombokJpaEntity.class);
		// add @javax.persistence.Entity on type
		JavacNode typeNode = annotationNode.up();
		JCClassDecl typeDecl = (JCClassDecl) typeNode.get();
		annGen.addAnnotation( typeDecl, typeNode, "javax.persistence.Entity" );
		LombokJpaEntity a = annotation.getInstance();
		// add ID field
		JavacNode idNode = addIDField( ast, annotationNode, typeNode, a );
		
		// add Version field
		JavacNode versionNode = addVersionField( ast, annotationNode, typeNode, a );

		// add Table annotation		
		//addTableAnnotation( typeNode, typeDecl, a );
		
		// add getter and setter for id and version
		getterHandler.createGetterForField(AccessLevel.PUBLIC, idNode, annotationNode, true, false, List.<JCAnnotation> nil() );
		setterHandler.createSetterForField(AccessLevel.PUBLIC, idNode, annotationNode, true, List.<JCAnnotation> nil(), List.<JCAnnotation> nil() );
		getterHandler.createGetterForField(AccessLevel.PUBLIC, versionNode, annotationNode, true, false, List.<JCAnnotation> nil() );
		setterHandler.createSetterForField(AccessLevel.PUBLIC, versionNode, annotationNode, true, List.<JCAnnotation> nil(), List.<JCAnnotation> nil() );
		
	}

	/*private void addTableAnnotation( JavacNode typeNode, JCClassDecl typeDecl, LombokJpaEntity a )
	{
		List<JCExpression> tableArgs = List.<JCExpression> nil();
		if( !a.table().isEmpty() )
		{
			tableArgs = parGen.createParameter(typeNode, "name", a.table(), tableArgs );
		}
		if( !a.catalog().isEmpty() )
		{
			tableArgs = parGen.createParameter( typeNode, "catalog", a.catalog(), tableArgs );
		}
		if( !a.schema().isEmpty() )
		{
			tableArgs = parGen.createParameter( typeNode, "schema", a.schema(), tableArgs );
		}
		List<JCExpression> indexes = List.<JCExpression> nil();
		if( 0 < a.indexes().length )
		{
			for( int i = 0; i < a.indexes().length; i++ )
			{
				List<JCExpression> indexArgs = List.<JCExpression> nil();
				if( !a.indexes()[i].name().isEmpty() )
				{
					indexArgs = parGen.createParameter( typeNode, "name", a.indexes()[i].name(), indexArgs );
				}
				indexArgs = parGen.createParameter( typeNode, "columnList", a.indexes()[i].columns(), indexArgs );
				if( !a.indexes()[i].unique() )
				{
					indexArgs = parGen.createParameter( typeNode, "unique", a.indexes()[i].unique(), indexArgs );
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
			annGen.addAnnotation( typeDecl, typeNode, "javax.persistence.Table", tableArgs );
		}
	}*/

	private JavacNode addVersionField(JCAnnotation source, JavacNode annotationNode, JavacNode typeNode, LombokJpaEntity a)
	{
		JCVariableDecl versionFieldDecl = fieldGen.createField( Flags.PRIVATE, a.versionField(), a.versionType().type(), source, annotationNode );
		JavacNode versionNode = injectFieldAndMarkGenerated( typeNode, versionFieldDecl);
		// add @javax.persistence.Version
		annGen.addAnnotation( versionFieldDecl, typeNode, "javax.persistence.Version" );
		// add @javax.persistence.Column(name = "<version>")
		if( !a.versionColumn().isEmpty() )
		{
			annGen.addAnnotation( versionFieldDecl, typeNode, "javax.persistence.Column", parGen.createParameter( typeNode, "name", a.versionColumn() ) );
		}
		annGen.addAnnotation( versionFieldDecl, typeNode, "lombok.EqualsAndHashCode.Exclude" );
		return versionNode;
	}


	private JavacNode addIDField(JCAnnotation source, JavacNode annotationNode, JavacNode typeNode, LombokJpaEntity a) 
	{
		JCVariableDecl fieldDecl = fieldGen.createField( Flags.PRIVATE, a.idField(), a.idType().type(), source, annotationNode );
		JavacNode node = injectFieldAndMarkGenerated( typeNode, fieldDecl );
		// add @javax.persistence.Id to field
		annGen.addAnnotation(fieldDecl, typeNode, "javax.persistence.Id");
		if( !a.idColumn().isEmpty() )
		{
			// add @javax.persistence.Column(name = "<id>")
			annGen.addAnnotation( fieldDecl, typeNode, "javax.persistence.Column", parGen.createParameter( typeNode, "name", a.idColumn() ) );
		}
		annGen.addAnnotation( fieldDecl, typeNode, "lombok.EqualsAndHashCode.Exclude" );
		if( !a.idSequence().isEmpty() || IDGenerator.SEQUENCE.equals( a.idGeneration() ) )
		{	
			String sequenceName = a.idSequence();
			if( sequenceName.isEmpty() )
			{
				sequenceName = typeNode.getName().toLowerCase();
			}
			String generatorName = sequenceName + "Gen";
			
			// add @javax.persistence.GeneratedValue(strategy = <idGeneration>, generator = "<idSequence>Gen")
			List<JCExpression> valueArgs = parGen.createTypeRefParameter( typeNode, "strategy", a.idGeneration().generator(), List.<JCExpression> nil() );
			valueArgs = parGen.createParameter( typeNode, "generator", generatorName, valueArgs );
			annGen.addAnnotation( fieldDecl, typeNode, "javax.persistence.GeneratedValue", valueArgs );
			// add @javax.persistence.SequenceGenerator(name = "<idSequence>Gen",sequenceName = "idSequence")
			List<JCExpression> generatorArgs = parGen.createParameter( typeNode, "name", generatorName, List.<JCExpression> nil() );
			generatorArgs = parGen.createParameter( typeNode, "sequenceName", sequenceName, generatorArgs );
			annGen.addAnnotation( fieldDecl, typeNode, "javax.persistence.SequenceGenerator", generatorArgs );
		}
		else 
		{
			// add @javax.persistence.GeneratedValue(strategy = <idGeneration>)
			annGen.addAnnotation( fieldDecl, typeNode, "javax.persistence.GeneratedValue", parGen.createTypeRefParameter( typeNode, "strategy", a.idGeneration().generator() ) );
		}
		return node;
	}
}
