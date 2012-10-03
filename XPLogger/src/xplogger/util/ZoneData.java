package xplogger.util;

import java.util.ArrayList;

import org.joda.time.Period;
import org.joda.time.Seconds;

public class ZoneData extends ArrayList<ZoneEntry>
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 3376508805776597052L;

	public Period getAverageDuration()
	{
		final Period totalDur = getTotalDuration();
		return new Period(Seconds.standardSecondsIn(totalDur).getSeconds()
				/ size() * 1000);
	}

	public int getAverageExpGained()
	{
		return getTotalExpGained() / size();
	}

	public float getAverageExpGainedMillions()
	{
		return Math.round(getTotalExpGained() / size() / 10000f) / 100f;
	}

	public float getAverageExpPerHour()
	{
		return (float) getAverageExpGained()
				/ (float) Seconds.standardSecondsIn(getAverageDuration())
						.getSeconds() * 3600;
	}

	public Period getTotalDuration()
	{
		return new Period(getTotalDurationInSeconds() * 1000);
	}

	public int getTotalDurationInSeconds()
	{
		int sum = 0;
		for (final ZoneEntry entry : this)
		{
			sum += Seconds.standardSecondsIn(entry.m_Duration).getSeconds();
		}
		return sum;
	}

	public int getTotalExpGained()
	{
		int sum = 0;
		for (final ZoneEntry entry : this)
		{
			sum += entry.m_ExpGained;
		}
		return sum;
	}

}
