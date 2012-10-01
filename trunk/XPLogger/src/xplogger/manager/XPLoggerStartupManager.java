package xplogger.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import xplogger.models.IXPLoggerModel;
import xplogger.models.XPLoggerModel;
import xplogger.presenters.IXPLoggerPresenter;
import xplogger.presenters.XPLoggerPresenter;
import xplogger.views.IXPLoggerView;
import xplogger.views.XPLoggerView;

import gov.usgs.nwrc.internal.manager.AbstractStartupManager;
import gov.usgs.nwrc.internal.model.interfaces.IModel;
import gov.usgs.nwrc.internal.presenter.interfaces.IPresenter;
import gov.usgs.nwrc.internal.view.interfaces.IView;

public class XPLoggerStartupManager extends AbstractStartupManager<IModel, IView>
{
	public XPLoggerStartupManager(){
		super();
		
	}
	
	@Override
	protected Collection<? extends IPresenter<IModel, IView>> initPresenters()
			throws Exception
	{
		final Collection<IPresenter<? extends IModel, ? extends IView>> list = new ArrayList<IPresenter<? extends IModel, ? extends IView>>();

		Callable<IXPLoggerModel> model = new Callable<IXPLoggerModel>()
				{
			@Override
			public IXPLoggerModel call() throws Exception
			{
				return new XPLoggerModel();
			}
		};
		
		Callable<IXPLoggerView> view = new Callable<IXPLoggerView>()
		{
			@Override
			public IXPLoggerView call() throws Exception
			{
				return (IXPLoggerView) m_WorkbenchWindow
						.getActivePage().showView(XPLoggerView.ID);
			}
		};
		
		final IXPLoggerPresenter xpLoggerPresenter = new XPLoggerPresenter(model, view);

		list.add(xpLoggerPresenter);

		m_WorkbenchWindowConfigurer.setTitle("XPLogger");
		

		return (Collection<? extends IPresenter<IModel, IView>>) list;
	}

}
