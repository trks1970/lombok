package lombok.eclipse.handlers.test;

import java.util.logging.Logger;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.mangosdk.spi.ProviderFor;

import lombok.core.AnnotationValues;
import lombok.core.LombokLogger;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.test.SubAnnotation;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleLombokSub extends EclipseAnnotationHandler<SubAnnotation>
{
	private static final Logger LOG = LombokLogger.getLogger();
	@Override
	public void handle( AnnotationValues<SubAnnotation> annotation, Annotation ast, EclipseNode annotationNode )
	{
		LOG.fine("HandleLombokSub enter");
		for( String key : annotation.getValues().keySet() )
		{
			LOG.fine( key );
			AnnotationValue value = annotation.getValues().get( key );
			for( int i = 0; i < value.raws.size(); i++ )
			{
				LOG.fine("    raw " + value.raws.get( i ) + 
						" expression " + ( i < value.expressions.size() ? value.expressions.get( i ) : "---" ) +
						" guess " + ( i < value.valueGuesses.size() ? value.valueGuesses.get( i ) : "---" )
				);
			}
			LOG.fine( "value node " + (value.node==null? "---" : value.node.get().toString() ) );
		}
		LOG.info("before getInstace");
		annotation.getInstance();
		LOG.info("after getInstace");
		LOG.fine( annotation.getNode().get().toString() );
		LOG.fine("HandleLombokSub exit");

	}

}
