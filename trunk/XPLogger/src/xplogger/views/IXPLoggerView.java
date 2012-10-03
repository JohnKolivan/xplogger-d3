package xplogger.views;

import gov.usgs.nwrc.internal.view.interfaces.IViewPart;
import xplogger.events.XPLoggerEvents;

public interface IXPLoggerView extends IViewPart
{

	void addTableColumn(final String p_ColumnName);

	void addTableColumn(final String p_ColumnName, final int p_Width);

	void addTableItem(final String[] p_Values);

	void clearTable();

	void insertTableItem(final String[] p_Values, final int p_Index);

	void replaceTableItem(final String[] p_Values, final int p_Index);

	void setPath(final XPLoggerEvents p_Widget, final String p_Path);

	void setWidgetEnabled(final XPLoggerEvents p_Widget, final boolean p_Enabled);

}
