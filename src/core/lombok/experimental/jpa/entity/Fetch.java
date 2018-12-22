package lombok.experimental.jpa.entity;

public enum Fetch
{
	LAZY("javax.persistence.FetchType.LAZY"),
	EAGER("javax.persistence.FetchType.EAGER");
	
	private final String fetchType;
	
	private Fetch(String type)
	{
		this.fetchType = type;
	}
	
	public String fetchType()
	{
		return fetchType;
	}
	
	public static Fetch fromValue(String v) 
	{
        return valueOf(v);
	}

}
