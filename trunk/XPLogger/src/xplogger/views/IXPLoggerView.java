package xplogger.views;

import xplogger.events.XPLoggerEvents;
import gov.usgs.nwrc.internal.view.interfaces.IViewPart;

public interface IXPLoggerView extends IViewPart
{

	void setWidgetEnabled(final XPLoggerEvents p_Widget, final boolean p_Enabled);

	void setPath(final XPLoggerEvents p_Widget, final String p_Path);

	void clearTable();

	void addTableItem(final String[] p_Values);

	void addTableColumn(final String p_ColumnName, final int p_Width);

	void addTableColumn(final String p_ColumnName);

	void insertTableItem(final String[] p_Values, final int p_Index);

	void replaceTableItem(final String[] p_Values, final int p_Index);

}
