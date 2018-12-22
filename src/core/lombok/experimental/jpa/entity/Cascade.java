package lombok.experimental.jpa.entity;

public enum Cascade
{
    ALL("javax.persistence.CascadeType.ALL"), 
    PERSIST("javax.persistence.CascadeType.PERSIST"), 
    MERGE("javax.persistence.CascadeType.MERGE"), 
    REMOVE("javax.persistence.CascadeType.REMOVE"),
    REFRESH("javax.persistence.CascadeType.REFRESH"),
    DETACH("javax.persistence.CascadeType.DETACH");
	
	private final String cascadeType;
	
	private Cascade(String type)
	{
		this.cascadeType = type;
	}
	
	public String cascadeType()
	{
		return cascadeType;
	}
	
	public static Cascade fromValue(String v) 
	{
        return valueOf(v);
	}

}
