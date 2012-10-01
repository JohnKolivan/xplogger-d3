package xplogger.util;

import java.util.ArrayList;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class Run extends ArrayList<RunEntry>{
	
	static PeriodFormatter PERIOD_FORMAT =  new PeriodFormatterBuilder().printZeroAlways().minimumPrintedDigits(2).appendHours().appendLiteral(":").appendMinutes().appendLiteral(":").appendSeconds().toFormatter();
	
	public Period getTotalDuration(){
		return new Period(get(0).m_Time, get(size()-1).m_Time);
	}
	
	public Period getDuration(final int p_StartEntry, final int p_EndEntry){
		if(size() == 0 || size() < p_EndEntry){
			return null;
		}
		return new Period(get(p_StartEntry).m_Time, get(p_EndEntry).m_Time);
		
	}
	
	public int getTotalExpGained(){
		//if only the first entry, no exp gained
		if(size() < 2){
			return 0;
		}
		
		//if no level up, last exp amount minus first exp amount
		return getEndingExp()- getStartingExp();
	}
	
	public boolean getLeveledUp(){
		if(size() == 0){
			return false;
		}
		return !get(0).m_ParagonLevel.equals(get(size()-1).m_ParagonLevel);
	}
	
	public int getStartingExp(){
		if(size() == 0){
			return -1;
		}
		return Integer.parseInt(get(0).m_CurrentExp);
	}
	
	public int getEndingExp(){
		if(size() == 0){
			return -1;
		}
		
		if(!getLeveledUp()){
			return Integer.parseInt(getLast().m_CurrentExp);
		}
		return Integer.parseInt(getLast().m_CurrentExp) + 
				Integer.parseInt(get(0).m_MaxExp);
	}
	
	public String getStartingParagonLevel(){
		if(size() == 0){
			return "ERROR";
		}
		return get(0).m_ParagonLevel;
	}
	
	public float getExpPerHour(){
		if(size() == 0){
			return 0;
		}
		return (float)(getTotalExpGained()) / (float)Seconds.standardSecondsIn(getTotalDuration()).getSeconds() * 3600;
	}
	
	public RunEntry getLast(){
		return size() > 0 ? get(size()-1) : null;
	}
	
	@Override
	public String toString()
	{	
		String returnValue = "";
	
		for(int i=0; i<size(); i++){
			returnValue += i + ":  " + get(i).toString() + "\n";
		}
		
		return returnValue;
	}
	
	public String[] toFormattedString(){
		String[] returnValue = new String[size()-1];
		
		for(int i=1; i<size(); i++){
			if(Integer.parseInt(get(i).m_CurrentExp) < Integer.parseInt(get(i-1).m_CurrentExp)){
				returnValue[i-1] = Integer.toString(Integer.parseInt(get(i-1).m_MaxExp) - Integer.parseInt(get(i-1).m_CurrentExp) + Integer.parseInt(get(i).m_CurrentExp));
			}else{
				returnValue[i-1] = Integer.toString(Integer.parseInt(get(i).m_CurrentExp) - Integer.parseInt(get(i-1).m_CurrentExp));
			}
			returnValue[i-1] += " - " + PERIOD_FORMAT.print(getDuration(i-1, i));
		}
		
		
		return returnValue;
	}
	
	
}