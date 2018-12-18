package lombok.experimental.jpa.entity;

public enum FieldType 
{
	INTEGER("java.lang.Integer"),
	LONG("java.lang.Long"),
	STRING("java.lang.String"),
	TIMESTAMP("java.sql.Timestamp");
	
	private final String type;
	
	private FieldType(String type)
	{
		this.type = type;
	}
	
	public String type()
	{
		return type;
	}
	
	public static FieldType fromValue(String v) 
	{
        return valueOf(v);
	}

	
}
