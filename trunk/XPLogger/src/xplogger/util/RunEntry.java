package xplogger.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class RunEntry
{
	static DateTimeFormatter	TIME_FORMAT	= new DateTimeFormatterBuilder()
													.appendHourOfDay(2)
													.appendLiteral(":")
													.appendMinuteOfHour(2)
													.appendLiteral(":")
													.appendSecondOfMinute(2)
													.toFormatter();

	public DateTime				m_Time;
	public String				m_CurrentExp;
	public String				m_MaxExp;
	public String				m_ParagonLevel;
	public String				m_Filename;

	public RunEntry(final String p_CurrentExp, final String p_MaxExp,
			final DateTime p_Time, final String p_ParagonLevel,
			final String p_Filename)
	{
		super();
		m_CurrentExp = p_CurrentExp;
		m_MaxExp = p_MaxExp;
		m_Time = p_Time;
		m_ParagonLevel = p_ParagonLevel;
		m_Filename = p_Filename;
	}

	@Override
	public String toString()
	{
		String returnString = m_CurrentExp;
		returnString += " | " + m_MaxExp;
		returnString += " | " + TIME_FORMAT.print(m_Time);
		returnString += " | " + m_ParagonLevel;
		return returnString;
	}
}
