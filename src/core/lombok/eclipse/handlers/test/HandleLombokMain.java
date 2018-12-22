package lombok.eclipse.handlers.test;

import java.util.logging.Logger;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.mangosdk.spi.ProviderFor;

import lombok.core.AnnotationValues;
import lombok.core.LombokLogger;
import lombok.core.AnnotationValues.AnnotationValue;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.test.MainAnnotation;
import lombok.experimental.test.SubAnnotation;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleLombokMain extends EclipseAnnotationHandler<MainAnnotation>
{
	private static final Logger LOG = LombokLogger.getLogger();
	@Override
	public void handle( AnnotationValues<MainAnnotation> annotation, Annotation ast, EclipseNode annotationNode )
	{
		LOG.fine("HandleLombokMain enter");
		LOG.fine( annotation.getNode().get().toString() );
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
				if( i < value.expressions.size() )
				{
					Object obj = value.expressions.get(i);
					LOG.fine("expr " + obj.getClass().getName() );
					if( obj instanceof NormalAnnotation )
					{
						NormalAnnotation ann = (NormalAnnotation) obj;
						LOG.fine( "type " + ann.type );
					}
				}
			}
			LOG.fine( "value node " + (value.node==null? "---" : value.node.get().toString() ) );
		}
		LOG.fine("before getInstace");
		MainAnnotation main = annotation.getInstance();
		LOG.fine("after getInstace");
		LOG.fine("before subs");
		SubAnnotation[] sub = main.subs();
		LOG.fine("after subs");
		for( int i = 0; i < sub.length; i++ )
		{
			LOG.fine( "sub " + sub[i].name() + " "   + sub[i].value() );
		}
		LOG.fine("HandleLombokMain exit");
	}

}
