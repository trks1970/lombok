package lombok.core;

import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LombokLogger
{
	private static Logger LOG;
	private static Logger CONSOLE_LOG;
	private static Handler fileHandler;
	
	static
	{
		LOG = Logger.getLogger( "LOMBOK" );
		CONSOLE_LOG = Logger.getLogger( "LOMBOK_CONSOLE" );
		try
		{
			fileHandler = new FileHandler( "./lombok.log" );
			LOG.addHandler( fileHandler );
			fileHandler.setLevel( Level.ALL );
			LOG.setLevel( Level.ALL );
			fileHandler.setFormatter( new SimpleFormatter()
			{
				private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

				@Override
				public synchronized String format( LogRecord lr )
				{
					return String.format( format, new Date( lr.getMillis() ), lr.getLevel().getLocalizedName(), lr.getMessage() );
				}
			} );
		}
		catch( SecurityException e )
		{
			throw new RuntimeException( e );
		}
		catch( IOException e )
		{
			throw new RuntimeException( e );
		}
	}
	
	private LombokLogger()
	{
	}
	
	public static Logger getLogger()
	{
		return LOG;
	}

	public static Logger getConsoleLogger()
	{
		return CONSOLE_LOG;
	}

}
