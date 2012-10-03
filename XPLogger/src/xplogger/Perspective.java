package xplogger;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import xplogger.views.XPLoggerView;

public class Perspective implements IPerspectiveFactory
{

	@Override
	public void createInitialLayout(final IPageLayout layout)
	{
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		layout.getViewLayout(XPLoggerView.ID).setCloseable(false);
	}
}
