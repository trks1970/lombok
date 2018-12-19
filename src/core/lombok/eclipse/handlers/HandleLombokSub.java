package lombok.eclipse.handlers;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.mangosdk.spi.ProviderFor;

import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.LombokSub;

@ProviderFor( EclipseAnnotationHandler.class )
public class HandleLombokSub extends EclipseAnnotationHandler<LombokSub>
{

	@Override
	public void handle( AnnotationValues<LombokSub> annotation, Annotation ast, EclipseNode annotationNode )
	{
		LombokSub sub = annotation.getInstance();
	}

}
