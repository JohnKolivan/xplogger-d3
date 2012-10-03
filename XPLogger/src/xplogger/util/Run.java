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
	
	public static PeriodFormatter PERIOD_FORMAT =  new PeriodFormatterBuilder().printZeroNever().minimumPrintedDigits(2).appendHours().appendSeparatorIfFieldsBefore(":").printZeroAlways().appendMinutes().appendLiteral(":").appendSeconds().toFormatter();
	
	public Period getTotalDuration(){
		return new Period(get(0).m_Time, get(size()-1).m_Time);
	}
	
	public Period getDuration(final int p_StartEntry, final int p_EndEntry){
		if(size() == 0 || size() < p_EndEntry || p_StartEntry < 0){
			return null;
		}
		return new Period(get(p_StartEntry).m_Time, get(p_EndEntry).m_Time);
	}
	
	public int getExpGained(final int p_StartEntry, final int p_EndEntry){
		if(size() == 0 || size() < p_EndEntry){
			return Integer.MIN_VALUE;
		}
		
		//if no level up occurred
		if(get(p_StartEntry).m_MaxExp.equals(get(p_EndEntry).m_MaxExp)){
			return Integer.parseInt(get(p_EndEntry).m_CurrentExp) - Integer.parseInt(get(p_StartEntry).m_CurrentExp);
		}
		
		//if leveled up
		return Integer.parseInt(get(p_StartEntry).m_MaxExp) - Integer.parseInt(get(p_StartEntry).m_CurrentExp) + Integer.parseInt(get(p_EndEntry).m_CurrentExp);
		
		
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
		return !getStartingParagonLevel().equals(getEndingParagonLevel());
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
	
	public String getEndingParagonLevel(){
		if(size() == 0){
			return "ERROR";
		}
		return getLast().m_ParagonLevel;
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
			returnValue[i-1] = Float.toString(Math.round(Integer.parseInt(returnValue[i-1])/10000f)/100f);
			returnValue[i-1] += " - " + PERIOD_FORMAT.print(getDuration(i-1, i));
		}
		
		
		return returnValue;
	}
	
	public String getFilenames(){
		String returnString = "";
		
		for(RunEntry entry : this){
			returnString += entry.m_Filename + ", ";
		}
		
		return returnString;
	}
	
	
}