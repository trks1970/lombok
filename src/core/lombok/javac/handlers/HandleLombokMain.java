package lombok.javac.handlers;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

import lombok.core.AnnotationValues;
import lombok.experimental.LombokMain;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

@ProviderFor( JavacAnnotationHandler.class )
public class HandleLombokMain extends JavacAnnotationHandler<LombokMain>
{

	@Override
	public void handle( AnnotationValues<LombokMain> annotation, JCAnnotation ast, JavacNode annotationNode )
	{
		LombokMain man = annotation.getInstance();
		
	}

}
