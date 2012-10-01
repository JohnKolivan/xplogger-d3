package xplogger;

import gov.usgs.nwrc.internal.manager.ManagerHandlerFactory;

import org.apache.log4j.Logger;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import xplogger.manager.XPLoggerHandler;
import xplogger.manager.XPLoggerStartupManager;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	XPLoggerStartupManager m_Manager = null;
	
    public ApplicationWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(final IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    @Override
    public void postWindowOpen(){
    	m_Manager.postStartup();
    }
    
    public void preWindowOpen() {
    	
    	try
		{
			m_Manager = ManagerHandlerFactory
					.create(XPLoggerHandler.class,
							XPLoggerStartupManager.class).createHandler()
					.getManager();
			m_Manager.setWorkbenchWindowConfigurer(getWindowConfigurer());
		}
		catch (final InstantiationException p_Exception)
		{
			Logger.getLogger(ApplicationWorkbenchWindowAdvisor.class).error(
					p_Exception);
		}
		catch (final IllegalAccessException p_Exception)
		{
			Logger.getLogger(ApplicationWorkbenchWindowAdvisor.class).error(
					p_Exception);
		}
    }
}
