package xplogger.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import xplogger.events.XPLoggerEvents;
import xplogger.util.*;

import gov.usgs.nwrc.internal.model.classes.AbstractModel;


public class XPLoggerModel extends AbstractModel implements IXPLoggerModel
{
	String m_InputPath = "";
	String m_OutputPath = "";
	List<String> m_ImageFilenames = new ArrayList<String>();
	Run m_CurrentRun = new Run();
	List<ZoneData> m_ZoneData = new ArrayList<ZoneData>();
	
	List<Run> m_Runs = new ArrayList<Run>();
	
	@Override
	public void setPath(final XPLoggerEvents p_Event, final String p_Path){
		switch (p_Event){
			case INPUT_BROWSE:
				m_InputPath = p_Path;
				break;
			case OUTPUT_BROWSE:
				m_OutputPath = p_Path;
		}
	}
	
	@Override
	public String getPath(final XPLoggerEvents p_Event){
		switch (p_Event){
			case INPUT_BROWSE:
				return m_InputPath;
				
			case OUTPUT_BROWSE:
				return	m_OutputPath;
				
		}
		return null;
	}

	@Override
	public void setImageFilenames(List<String> m_ImageFilenames)
	{
		this.m_ImageFilenames = m_ImageFilenames;
	}

	@Override
	public void validateModel() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clearAllData(){
		m_CurrentRun.clear();
		m_ZoneData.clear();
		m_Runs.clear();
	}
	
	@Override
	public void addRunEntry(final RunEntry p_Entry){
		m_CurrentRun.add(p_Entry);
	}
	
	public void setCurrentRun(final Run p_Run){
		m_CurrentRun = p_Run;
	}
	
	@Override
	public Run getCurrentRunData(){
		return m_CurrentRun;
	}
	
	@Override
	public void addRun(final Run p_Run){
		m_Runs.add(p_Run);
	}
	
	@Override 
	public List<Run> getAllRunData(){
		return m_Runs;
	}
	
	@Override
	public ZoneData getZoneData(final int p_Index){
		if(m_ZoneData.size() < p_Index){
			return null;
		}
		return m_ZoneData.get(p_Index-1);
	}
	
	@Override
	public void addZoneData(final int p_ZoneIndex, final ZoneEntry p_Data){
		if(m_ZoneData.size() <= p_ZoneIndex){
			m_ZoneData.add(new ZoneData());
		}
		
		m_ZoneData.get(p_ZoneIndex-1).add(p_Data);
	}
	
}
