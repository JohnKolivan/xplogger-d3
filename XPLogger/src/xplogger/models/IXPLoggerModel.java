package xplogger.models;

import java.util.List;

import org.joda.time.DateTime;

import xplogger.events.XPLoggerEvents;
import xplogger.util.Run;
import xplogger.util.RunEntry;
import xplogger.util.ZoneData;
import xplogger.util.ZoneEntry;
import gov.usgs.nwrc.internal.model.interfaces.IModel;

public interface IXPLoggerModel extends IModel
{

	void setPath(final XPLoggerEvents p_Event, final String p_Path);

	String getPath(final XPLoggerEvents p_Event);

	void setImageFilenames(final List<String> m_ImageFilenames);

	void clearAllData();

	void addRun(final Run p_Run);

	List<Run> getAllRunData();

	Run getCurrentRunData();

	void addRunEntry(final RunEntry p_Entry);

	void setCurrentRun(final Run p_Run);

	void addZoneData(final int p_ZoneIndex, final ZoneEntry p_Data);

	ZoneData getZoneData(final int p_Index);
}
