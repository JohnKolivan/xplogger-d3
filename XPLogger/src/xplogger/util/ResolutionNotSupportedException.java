package xplogger.util;

import java.io.IOException;

public class ResolutionNotSupportedException extends IOException
{
	public ResolutionNotSupportedException(final String p_Message)
	{
		super(p_Message);
	}

	private static final long	serialVersionUID	= 6824186823006911240L;
}
