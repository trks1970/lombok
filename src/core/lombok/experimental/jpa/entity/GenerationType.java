package lombok.experimental.jpa.entity;

public enum GenerationType 
{
	AUTO("javax.persistence.GenerationType.AUTO"),
	IDENTITY("javax.persistence.GenerationType.IDENTITY"),
	SEQUENCE("javax.persistence.GenerationType.SEQUENCE");
	//TABLE("javax.persistence.GenerationType.TABLE");
	
	private final String generator;
	
	private GenerationType(String type)
	{
		this.generator = type;
	}
	
	public String generator()
	{
		return generator;
	}
	
	public static GenerationType fromValue(String v) 
	{
        return valueOf(v);
	}

}
