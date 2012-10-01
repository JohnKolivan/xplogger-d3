package xplogger.util;



import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class RunEntry
{
	static DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(":").appendMinuteOfHour(2).appendLiteral(":").appendSecondOfMinute(2).toFormatter();
	
	public DateTime m_Time;
	public String m_CurrentExp;
	public String m_MaxExp;
	public String m_ParagonLevel;
	
	public RunEntry( String m_CurrentExp, String m_MaxExp, DateTime m_Time, String m_ParagonLevel)
	{
		super();
		this.m_CurrentExp = m_CurrentExp;
		this.m_MaxExp = m_MaxExp;
		this.m_Time = m_Time;
		this.m_ParagonLevel = m_ParagonLevel;
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
