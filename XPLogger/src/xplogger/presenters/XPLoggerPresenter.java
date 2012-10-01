package xplogger.presenters;

import gov.usgs.nwrc.internal.presenter.classes.AbstractPresenter;
import gov.usgs.nwrc.internal.util.Method;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import static java.nio.file.StandardWatchEventKinds.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.eclipse.swt.SWT;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import xplogger.events.XPLoggerEvents;
import xplogger.models.IXPLoggerModel;
import xplogger.util.ColumnNames;
import xplogger.util.Run;
import xplogger.util.RunEntry;
import xplogger.views.IXPLoggerView;


public class XPLoggerPresenter extends AbstractPresenter<IXPLoggerModel, IXPLoggerView> implements IXPLoggerPresenter
{

	static boolean FILE_WATCHER_IS_RUNNING = false;
	
	Job m_WatcherJob = null;
	
	PeriodFormatter m_TimeFormat;
	
	public XPLoggerPresenter(final Callable<IXPLoggerModel> p_ModelCallable, final Callable<IXPLoggerView> p_ViewCallable)
			throws Exception
	{
		super(p_ModelCallable, p_ViewCallable);
		m_TimeFormat = new PeriodFormatterBuilder().printZeroAlways().minimumPrintedDigits(2).appendHours().appendLiteral(":").appendMinutes().appendLiteral(":").appendSeconds().toFormatter();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent p_Event)
	{
		XPLoggerEvents event = XPLoggerEvents.valueOf(p_Event.getPropertyName());
		
		switch(event){
			
			
			case INPUT_BROWSE:
				if(m_Model.getPath(event).length() > 0){
					m_Model.setPath(event, "");
					m_View.setPath(event, "");
					m_Model.setImageFilenames(new ArrayList<String>());
					m_View.setWidgetEnabled(XPLoggerEvents.START, false);
				}
				
				String directory = m_View.showFolderPathPrompt(
						"Screen Directory Selection", 
						"Select the location where screenshots are stored", 
						getLastFilterPath());
				
				if(directory == null)
				{
					return;
				}
				
				setLastFilterPath(directory);
				m_Model.setPath(event, directory);
				m_View.setPath(event, directory);
				
				break;
			case OUTPUT_BROWSE:
				String filepath = m_View.showFilePrompt(
						"Select Output File", SWT.SAVE,
						new String[] {""}, new String[] {""},
						getLastFilterPath());
				if(filepath != null){
					setLastFilterPath(filepath);
					m_Model.setPath(event, filepath);
					m_View.setPath(event, filepath);
				}
				break;
				
			case NEW_FILE:
	
				RunEntry entry = processImage(p_Event.getNewValue().toString());
				
				if(entry != null){
					handleEntry(entry);
				}
				updateTable();
				break;
				
			case START:
				
				createWatcherJob();
				
				break;
				
			case STOP:
				FILE_WATCHER_IS_RUNNING = false;
				m_WatcherJob.getThread().interrupt();
				break;
				
			case CLEAR:
				m_Model.clearAllData();
				m_View.clearTable();
				
				break;
		}
		
		m_View.runInAsyncUIThread(new Callable<Boolean>(){

			@Override
			public Boolean call() throws Exception
			{
				m_View.setWidgetEnabled(XPLoggerEvents.START, m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) != null && !FILE_WATCHER_IS_RUNNING);
				m_View.setWidgetEnabled(XPLoggerEvents.STOP, m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) != null && FILE_WATCHER_IS_RUNNING);
				return true;
			}
		});
	}
	
	protected void createWatcherJob(){
		final PropertyChangeListener listener = this;
		
		m_WatcherJob = new Job("File Watcher"){

			final PropertyChangeSupport m_PropChangeSupport = new PropertyChangeSupport("WatcherJob");
			
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				m_PropChangeSupport.addPropertyChangeListener(listener);
				
				FILE_WATCHER_IS_RUNNING = true;
				
				Path myDir = Paths.get(m_Model.getPath(XPLoggerEvents.INPUT_BROWSE));
				WatchKey key = null;
				WatchService watcher = null;
				
				while(FILE_WATCHER_IS_RUNNING){
					try
					{
						watcher = myDir.getFileSystem().newWatchService();
						myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
						key = watcher.take();
						Thread.sleep(10);
					}
					catch (InterruptedException e1)
					{
						continue;
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					for(WatchEvent<?> event : key.pollEvents()){
						WatchEvent.Kind<?> kind = event.kind();
						
						if(kind == OVERFLOW){
							continue;
						}
						
						m_PropChangeSupport.firePropertyChange(XPLoggerEvents.NEW_FILE.toString(), "", event.context().toString());
					}
				}
				
				m_PropChangeSupport.removePropertyChangeListener(listener);
				return Status.CANCEL_STATUS;
			}
		};
		m_WatcherJob.schedule();
	}
	
	
	
	protected RunEntry processImage(final String p_Filename){
		Tesseract tess = Tesseract.getInstance();
		tess.setDatapath("%PROGRAMFILES(X86)%\\Tesseract-OCR\\");
		tess.setLanguage("eng");
			
		try
		{
			File file = new File(m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) + File.separator + p_Filename);
			DateTime endTime = new DateTime(file.lastModified());
			
			BufferedImage image;
			image = ImageIO.read(file);
			BufferedImage expImage = grabSubImage(image, new Rectangle(750, 1000, 412, 26), 4);
			String expBarHoverText = tess.doOCR(expImage).trim().replaceAll("\\s", "").replace(",", "");
			String paragonLevel = expBarHoverText.split("(\\(|\\))")[1].intern();
			
			//test if the parse succeeded, 
			//NumberFormatException thrown if failed, meaning exp amounts were not correctly read
			Integer.parseInt(paragonLevel);
			
			String exps = expBarHoverText.split(":")[1];
			
			String currentExp = exps.split("/")[0];
			String endExp = exps.split("/")[1];

			RunEntry entry = new RunEntry(currentExp, endExp, endTime, paragonLevel);
			log.debug(entry.toString());
			
			return entry;

		}
		catch (final Exception p_Exception)
		{	log.debug(p_Exception.getStackTrace().toString(), p_Exception.getCause()); }
		
		return null;
	}
	
	protected void collectDataFromScreenshots(){
		
		m_Model.clearAllData();
		m_View.clearTable();
		
		Tesseract tess = Tesseract.getInstance();
		tess.setDatapath("C:\\Program Files (x86)\\Tesseract-OCR\\");
		tess.setLanguage("eng");
		
		final DateTime startTime = DateTime.now();
		
		List<RunEntry> entryList = new ArrayList<RunEntry>();
		int runCount = 0;
		String prevExp = "";
			
			try
			{
				
				File file = new File(m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) + File.separator );//+ filename);
				DateTime endTime = new DateTime(file.lastModified());
				
				BufferedImage image;
				image = ImageIO.read(file);
				BufferedImage expImage = grabSubImage(image, new Rectangle(750, 1000, 412, 26), 4);
				String expBarHoverText = tess.doOCR(expImage).trim().replaceAll("\\s", "").replace(",", "");
				String paragonLevel = expBarHoverText.split("(\\(|\\))")[1].intern();
				 
				String exps = expBarHoverText.split(":")[1];
				
				String currentExp = exps.split("/")[0];
				String endExp = exps.split("/")[1];
				RunEntry entry = new RunEntry(currentExp, endExp, endTime, paragonLevel);
				entryList.add(entry);
				System.out.println(entry.toString());
				
				if(prevExp.equals(currentExp)){
					runCount++;
				}
				prevExp = currentExp;
			}
			catch (IOException e)
			{	e.printStackTrace(); }
			catch (TesseractException e)
			{	e.printStackTrace(); }
			
		
		//---------------------------------------------
		/*
		for(String filename : m_Model.getImageFilenames()){
			try
			{
				File file = new File(m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) + File.separator + filename);
				DateTime endTime = new DateTime(file.lastModified());
				
				BufferedImage image = ImageIO.read(file);
			
				BufferedImage expImage = grabSubImage(image, new Rectangle(750, 1000, 412, 26), 4);
				String expBarHoverText = tess.doOCR(expImage).trim().replaceAll("\\s", "").replace(",", "");
				String paragonLevel = expBarHoverText.split("(\\(|\\))")[1].intern();
				 
				String exps = expBarHoverText.split(":")[1];
				
				String currentExp = exps.split("/")[0];
				String endExp = exps.split("/")[1];
				
				
				
				
				if(prevExpAmount.equals(currentExp)){
					if(currentRun.size() == 0){
						runCount++;
						currentRun.add(new RunEntry(runCount, currentExp, endExp, endTime, paragonLevel));
					}else{
						currentRun.add(new RunEntry(runCount, currentExp, endExp, endTime, paragonLevel));
						m_Model.addRun(currentRun);
						currentRun = new Run();
					}
				}else{
					
					currentRun.add(new RunEntry(runCount, currentExp, endExp, endTime, paragonLevel));
				}
				prevExpAmount = currentExp;
			}
			catch (TesseractException e)
			{	e.printStackTrace();	}
			catch (IOException e)
			{ 	e.printStackTrace();	}	
		}
		
		Run currentRun = new Run();
		int runCount = 0;
		String prevExpAmount = "";
		
		if(currentRun.size() > 0){
			m_Model.addRun(currentRun);
		}
		
		//add column for checkbox
		m_View.addTableColumn("",25);
		
		
		for(ColumnNames column : ColumnNames.values()){
			m_View.addTableColumn(column.toString());
		}
		
		//add rows
		Integer prevExp = -1;
		DateTime prevTime = null;
		
		//organizes the runs by specific zone
		final Map<String, ZoneData> zoneRuns = new LinkedHashMap<String, ZoneData>();
		
		//insert each of the runs
		for(Run run : m_Model.getRunData()){
			List<String> values = new ArrayList<String>();
			//empty value for checkbox column
			values.add("");
			for(RunEntry entry : run){
				
				if(prevExp > 0){
					values.add(Integer.toString(Integer.parseInt(entry.m_CurrentExp) - prevExp) + 
							" : " + m_TimeFormat.print(new Period(prevTime, entry.m_Time)));
					
					//this lets us get the average values per zone
					if(!zoneRuns.containsKey(entry.m_ZoneName)){
						zoneRuns.put(entry.m_ZoneName, new ZoneData());
					}
					
					zoneRuns.get(entry.m_ZoneName).add(new ZoneEntry(entry.m_ZoneName, 
							new Period(prevTime, entry.m_Time), 
							Integer.parseInt(entry.m_CurrentExp) - prevExp));
				}
				prevExp = Integer.parseInt(entry.m_CurrentExp);
				prevTime = entry.m_Time;
			}
			values.add(m_TimeFormat.print(run.getDuration()));
			values.add(Integer.toString(run.getStartingExp()));
			values.add(Integer.toString(run.getEndingExp()));
			values.add(Integer.toString(run.getEndingExp() - run.getStartingExp()));
			
			//get exp per hour, rounded 2 decimal places to millions
			values.add(Float.toString(((float)Math.round(run.getExpPerHour() / 10000f)) / 100f));
			
			values.add(run.getStartingParagonLevel());
			m_View.addTableItem(values.toArray(new String[0]));			
		}
		
		List<String> values = new ArrayList<String>();
		//empty value for checkbox column
		values.add("");
		int duration = 0;
		int expGained = 0;
		int totalSeconds = 0;
		for(String zoneName : zoneRuns.keySet()){
			ZoneData zone = zoneRuns.get(zoneName);
			duration += zone.getTotalDurationInSeconds();
			expGained += zone.getTotalExpGained();
			values.add(Float.toString((float)Math.round(zone.getAverageExpPerHour() / 10000f) / 100f));
		}
		
		values.add(m_TimeFormat.print(new Period((duration * 1000) )));
		values.add(""); values.add("");
		
		int aveExpGained = Math.round((float)expGained / (float)runCount);
		values.add(Integer.toString(aveExpGained));
		values.add(Float.toString((float)Math.round(aveExpGained / duration * 3600 / 10000f)/100f));
		
		m_View.insertTableItem(values.toArray(new String[0]), 0);
		
		*/
	}
	
	protected void handleEntry(final RunEntry p_Entry){

		Run currentRun = m_Model.getCurrentRunData();
		RunEntry lastEntry = currentRun.getLast();
		
		//handle first run
		if(lastEntry == null){
			currentRun.add(p_Entry);
			System.out.print(currentRun.toString() + "\n\n");
			return;
		}
		
		
		if(lastEntry.m_CurrentExp.equals(p_Entry.m_CurrentExp)){
			//start a new run
			m_Model.addRun(currentRun);
			currentRun = new Run();
			m_Model.setCurrentRun(currentRun);
			
		}
		
		currentRun.add(p_Entry);
		System.out.print(currentRun.toString() + "\n\n");
		log.debug(currentRun.toString() + "\n\n");

	}
	
	protected void updateTable(){
		
		m_View.runInAsyncUIThread(new Callable<Boolean>(){

			@Override
			public Boolean call() throws Exception
			{
				m_View.clearTable();
				
				
				int columnCount;
				if(m_Model.getAllRunData().size() > 0){
					columnCount = m_Model.getAllRunData().get(0).size()-1;
				}else{
					columnCount = m_Model.getCurrentRunData().size()-1;
				}
				
				m_View.addTableColumn("Run");
								
				for(int i=0; i<columnCount; i++){
					m_View.addTableColumn(Integer.toString(i+1));
				}
				
				for(ColumnNames column : ColumnNames.values()){
					m_View.addTableColumn(column.toString());
				}
				
				if(columnCount == 0){
					return true;
				}
				
				int runCount = 1;
				for(Run run : m_Model.getAllRunData()){
					
					List<String> tableValues = new ArrayList<String>(Arrays.asList(run.toFormattedString()));
					tableValues.add(0,Integer.toString(runCount));
					tableValues.add(m_TimeFormat.print(run.getTotalDuration()));
					tableValues.add(Integer.toString(run.getTotalExpGained()));
					tableValues.add(Float.toString((float)Math.round(run.getExpPerHour()/10000f)/100f));
					tableValues.add(run.getStartingParagonLevel());
					m_View.addTableItem(tableValues.toArray(new String[0]));
					runCount++;
				}
				
				if(m_Model.getCurrentRunData().size() > 0){
					List<String> tableValues = new ArrayList<String>(Arrays.asList(m_Model.getCurrentRunData().toFormattedString()));
					while(tableValues.size() < columnCount){
						tableValues.add("");
					}
					tableValues.add(0,Integer.toString(runCount));
					tableValues.add(m_TimeFormat.print(m_Model.getCurrentRunData().getTotalDuration()));
					tableValues.add(Integer.toString(m_Model.getCurrentRunData().getTotalExpGained()));
					tableValues.add(Float.toString((float)Math.round(m_Model.getCurrentRunData().getExpPerHour()/10000f)/100f));
					tableValues.add(m_Model.getCurrentRunData().getStartingParagonLevel());
					m_View.addTableItem(tableValues.toArray(new String[0]));
				}
				return true;
			}
		
		});
	}
	
	protected BufferedImage grabSubImage(final BufferedImage p_Image, final Rectangle p_subImage, final float p_Scale){
		BufferedImage newImage = new BufferedImage((int)(p_subImage.width * p_Scale),(int)( p_subImage.height * p_Scale), p_Image.getType());
		final Graphics2D g = newImage.createGraphics();
		final AffineTransform tx = new AffineTransform();
		
		tx.scale(p_Scale, p_Scale);
		tx.translate(-p_subImage.getX(), -p_subImage.getY());
		g.setTransform(tx);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(p_Image, 0, 0, p_Image.getWidth(), p_Image.getHeight(), null);
		g.dispose();
		//op.filter(newImage, newImage);
		
		FilteredImageSource filteredSrc = new FilteredImageSource(newImage.getSource(), new Filter());
		Image filteredImage = Toolkit.getDefaultToolkit().createImage(filteredSrc);
		
		BufferedImage finalImage = new BufferedImage(filteredImage.getWidth(null), filteredImage.getHeight(null), p_Image.getType());
		Graphics g2 = finalImage.createGraphics();
		g2.drawImage(filteredImage, 0, 0, null);
		g2.dispose();
//		try
//		{
//			
//			ImageIO.write(finalImage, "png", new File("C:\\Users\\primeauxb\\Documents\\pics\\" + Integer.toString((int)(Math.random() * Integer.MAX_VALUE))+".png"));
//		}
//		catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return finalImage;
	}
	
	protected class Filter extends RGBImageFilter {
	    public Filter() {
	        // When this is set to true, the filter will work with images
	        // whose pixels are indices into a color table (IndexColorModel).
	        // In such a case, the color values in the color table are filtered.
	        canFilterIndexColorModel = true;
	    }

	    // This method is called for every pixel in the image
	    public int filterRGB(int x, int y, int rgb) {
	        if (x == -1) {
	            // The pixel value is from the image's color table rather than the image itself
	        }
	        // Return only the red component
	        
//	        float red = (float) (((rgb >> 16) & 0xFF) / 255.0);
//	        float blue = (float) (((rgb >> 8) & 0xFF) / 255.0);
//	        float green = (float) (((rgb) & 0xFF) / 255.0);
//	        final double threshhold = 0.3;
//	        if((Math.abs(red - blue) < threshhold) && (Math.abs(red - green) < threshhold) && (Math.abs(green - blue) < threshhold) )
//	        	return rgb;
//	        
//	        if( red < 0.4 && green > 0.4 && blue > 0.4)
//	        	return rgb;
	        
	       
	        
	        return rgb;
	        //return rgb & 0xff0000ff;
	    }
	}
}
