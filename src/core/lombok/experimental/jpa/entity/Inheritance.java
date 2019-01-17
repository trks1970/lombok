package lombok.experimental.jpa.entity;

public enum Inheritance 
{
	NONE(""),
	SINGLE_TABLE("javax.persistence.InheritanceType.SINGLE_TABLE"),
	JOINED("javax.persistence.InheritanceType.JOINED"),
	TABLE_PER_CLASS("javax.persistence.InheritanceType.TABLE_PER_CLASS");
	
	private final String type;
	
	private Inheritance(String type)
	{
		this.type = type;
	}
	
	public String type()
	{
		return type;
	}
	
	public static Inheritance fromValue(String v) 
	{
        return valueOf(v);
	}

}
