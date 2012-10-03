package xplogger.util;

import org.joda.time.Period;

public class ZoneEntry
{
	Period	m_Duration;
	int		m_ExpGained;

	/**
	 * @param p_RunNumber
	 * @param p_Duration
	 * @param p_ExpGained
	 */
	public ZoneEntry(final Period p_Duration, final int p_ExpGained)
	{
		this.m_Duration = p_Duration;
		this.m_ExpGained = p_ExpGained;
	}

}
