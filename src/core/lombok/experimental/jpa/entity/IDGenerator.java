package lombok.experimental.jpa.entity;

public enum IDGenerator 
{
	AUTO("javax.persistence.GenerationType.AUTO"),
	IDENTITY("javax.persistence.GenerationType.IDENTITY"),
	SEQUENCE("javax.persistence.GenerationType.SEQUENCE");
	//TABLE("javax.persistence.GenerationType.TABLE");
	
	private final String generator;
	
	private IDGenerator(String type)
	{
		this.generator = type;
	}
	
	public String generator()
	{
		return generator;
	}
	
	public static IDGenerator fromValue(String v) 
	{
        return valueOf(v);
	}

}
