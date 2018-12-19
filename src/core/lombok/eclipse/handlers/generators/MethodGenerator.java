package lombok.eclipse.handlers.generators;

public class MethodGenerator
{
	private static final MethodGenerator INSTANCE = new MethodGenerator();
	
	private MethodGenerator() {}
	
	public static MethodGenerator getInstance()
	{
		return INSTANCE;
	}
	
}
