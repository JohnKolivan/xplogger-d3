package xplogger.manager;

import gov.usgs.nwrc.internal.manager.ManagerHandler;

public class XPLoggerHandler implements ManagerHandler<XPLoggerStartupManager>
{

	protected static XPLoggerStartupManager	MANAGER	= null;

	@Override
	public XPLoggerStartupManager getManager()
	{
		return MANAGER;
	}

	@Override
	public void setManager(final XPLoggerStartupManager p_Manager)
	{
		if (MANAGER == null)
		{
			MANAGER = p_Manager;
		}
	}
}
