package lombok.eclipse.handlers;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.mangosdk.spi.ProviderFor;

import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.LombokMain;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleLombokMain extends EclipseAnnotationHandler<LombokMain>
{

	@Override
	public void handle( AnnotationValues<LombokMain> annotation, Annotation ast, EclipseNode annotationNode )
	{
		LombokMain main = annotation.getInstance();
	}

}
