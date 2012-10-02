package xplogger.util;

import java.util.ArrayList;


import org.joda.time.Period;
import org.joda.time.Seconds;

public class ZoneData extends ArrayList<ZoneEntry>
{
	public int getTotalDurationInSeconds(){
		int sum = 0;
		for(ZoneEntry entry : this){
			sum += Seconds.standardSecondsIn(entry.m_Duration).getSeconds();
		}
		return sum;
	}
	
	public Period getTotalDuration(){
		return new Period(getTotalDurationInSeconds() * 1000);
	}
	
	public Period getAverageDuration(){
		Period totalDur = getTotalDuration();
		return new Period(Seconds.standardSecondsIn(totalDur).getSeconds() / size() * 1000);
	}
	
	public int getTotalExpGained(){
		int sum = 0;
		for(ZoneEntry entry : this){
			sum += entry.m_ExpGained;
		}
		return sum;
	}
	
	public int getAverageExpGained(){
		return getTotalExpGained() / size();
	}
	
	public float getAverageExpGainedMillions(){
		return Math.round(getTotalExpGained() / size()/10000f)/100f;
	}
	
	public float getAverageExpPerHour(){
		return (float)getAverageExpGained() / (float)Seconds.standardSecondsIn(getAverageDuration()).getSeconds() * 3600;
	}
	

}
