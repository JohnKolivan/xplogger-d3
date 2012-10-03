package xplogger.models;

import gov.usgs.nwrc.internal.model.interfaces.IModel;

import java.util.List;

import xplogger.events.XPLoggerEvents;
import xplogger.util.Run;
import xplogger.util.RunEntry;
import xplogger.util.ZoneData;
import xplogger.util.ZoneEntry;

public interface IXPLoggerModel extends IModel
{

	void addRun(final Run p_Run);

	void addRunEntry(final RunEntry p_Entry);

	void addZoneData(final int p_ZoneIndex, final ZoneEntry p_Data);

	void clearAllData();

	List<Run> getAllRunData();

	Run getCurrentRunData();

	String getPath(final XPLoggerEvents p_Event);

	ZoneData getZoneData(final int p_Index);

	void setCurrentRun(final Run p_Run);

	void setImageFilenames(final List<String> m_ImageFilenames);

	void setPath(final XPLoggerEvents p_Event, final String p_Path);
}
