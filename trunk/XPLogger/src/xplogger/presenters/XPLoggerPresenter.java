package xplogger.presenters;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import gov.usgs.nwrc.internal.presenter.classes.AbstractPresenter;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import xplogger.events.XPLoggerEvents;
import xplogger.models.IXPLoggerModel;
import xplogger.util.ColumnNames;
import xplogger.util.ResolutionNotSupportedException;
import xplogger.util.Run;
import xplogger.util.RunEntry;
import xplogger.util.ZoneData;
import xplogger.util.ZoneEntry;
import xplogger.views.IXPLoggerView;

public class XPLoggerPresenter extends
		AbstractPresenter<IXPLoggerModel, IXPLoggerView> implements
		IXPLoggerPresenter
{

	protected class Filter extends RGBImageFilter
	{
		public Filter()
		{
			// When this is set to true, the filter will work with images
			// whose pixels are indices into a color table (IndexColorModel).
			// In such a case, the color values in the color table are filtered.
			canFilterIndexColorModel = true;
		}

		// This method is called for every pixel in the image
		@Override
		public int filterRGB(final int x, final int y, final int rgb)
		{
			if (x == -1)
			{
				// The pixel value is from the image's color table rather than
				// the image itself
			}
			// Return only the red component

			// float red = (float) (((rgb >> 16) & 0xFF) / 255.0);
			// float blue = (float) (((rgb >> 8) & 0xFF) / 255.0);
			// float green = (float) (((rgb) & 0xFF) / 255.0);
			// final double threshhold = 0.3;
			// if((Math.abs(red - blue) < threshhold) && (Math.abs(red - green)
			// < threshhold) && (Math.abs(green - blue) < threshhold) )
			// return rgb;
			//
			// if( red < 0.4 && green > 0.4 && blue > 0.4)
			// return rgb;

			return rgb;
			// return rgb & 0xff0000ff;
		}
	}

	static boolean	FILE_WATCHER_IS_RUNNING	= false;

	Job				m_WatcherJob			= null;

	PeriodFormatter	m_TimeFormat;

	public XPLoggerPresenter(final Callable<IXPLoggerModel> p_ModelCallable,
			final Callable<IXPLoggerView> p_ViewCallable) throws Exception
	{
		super(p_ModelCallable, p_ViewCallable);
		m_TimeFormat = new PeriodFormatterBuilder().printZeroAlways()
				.minimumPrintedDigits(2).appendHours().appendLiteral(":")
				.appendMinutes().appendLiteral(":").appendSeconds()
				.toFormatter();

		final String path = getLastFilterPath();
		if (path.length() > 0 && new File(path).isDirectory()
				&& new File(path).canRead())
		{
			setLastFilterPath(path);
			m_Model.setPath(XPLoggerEvents.INPUT_BROWSE, path);
			m_View.setPath(XPLoggerEvents.INPUT_BROWSE, path);
			m_View.setWidgetEnabled(XPLoggerEvents.START,
					m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) != null
							&& !FILE_WATCHER_IS_RUNNING);
			m_View.setWidgetEnabled(XPLoggerEvents.SCAN,
					m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) != null);
		}
	}

	protected void createWatcherJob()
	{
		final PropertyChangeListener listener = this;

		m_WatcherJob = new Job("Waiting for screenshots...")
		{

			final PropertyChangeSupport	m_PropChangeSupport	= new PropertyChangeSupport(
																	"WatcherJob");

			@Override
			protected IStatus run(final IProgressMonitor monitor)
			{
				m_PropChangeSupport.addPropertyChangeListener(listener);

				FILE_WATCHER_IS_RUNNING = true;

				final Path myDir = Paths.get(m_Model
						.getPath(XPLoggerEvents.INPUT_BROWSE));
				WatchKey key = null;
				WatchService watcher = null;

				while (FILE_WATCHER_IS_RUNNING)
				{
					try
					{
						watcher = myDir.getFileSystem().newWatchService();
						myDir.register(watcher,
								StandardWatchEventKinds.ENTRY_CREATE);
						key = watcher.take();
						Thread.sleep(10);
					}
					catch (final InterruptedException e1)
					{
						continue;
					}
					catch (final IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					for (final WatchEvent<?> event : key.pollEvents())
					{
						final WatchEvent.Kind<?> kind = event.kind();

						if (kind == OVERFLOW)
						{
							continue;
						}

						m_PropChangeSupport.firePropertyChange(
								XPLoggerEvents.NEW_FILE.toString(), "", event
										.context().toString());
					}
				}

				m_PropChangeSupport.removePropertyChangeListener(listener);
				return Status.CANCEL_STATUS;
			}
		};
		m_WatcherJob.schedule();
	}

	protected BufferedImage grabSubImage(final BufferedImage p_Image,
			final Rectangle p_subImage, final float p_Scale)
	{
		final BufferedImage newImage = new BufferedImage(
				(int) (p_subImage.width * p_Scale),
				(int) (p_subImage.height * p_Scale), p_Image.getType());
		final Graphics2D g = newImage.createGraphics();
		final AffineTransform tx = new AffineTransform();

		tx.scale(p_Scale, p_Scale);
		tx.translate(-p_subImage.getX(), -p_subImage.getY());
		g.setTransform(tx);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(p_Image, 0, 0, p_Image.getWidth(), p_Image.getHeight(),
				null);
		g.dispose();
		// op.filter(newImage, newImage);

		final FilteredImageSource filteredSrc = new FilteredImageSource(
				newImage.getSource(), new Filter());
		final Image filteredImage = Toolkit.getDefaultToolkit().createImage(
				filteredSrc);

		final BufferedImage finalImage = new BufferedImage(
				filteredImage.getWidth(null), filteredImage.getHeight(null),
				p_Image.getType());
		final Graphics g2 = finalImage.createGraphics();
		g2.drawImage(filteredImage, 0, 0, null);
		g2.dispose();

		/*
		//outputs the subimage grabbed
		try
		 {
			 ImageIO.write(finalImage, "png", new
			 File("C:\\Users\\primeauxb\\Documents\\pics\\" +
			 Integer.toString((int)(Math.random() * Integer.MAX_VALUE))+".png"));
		 }
		 catch (IOException e)
		 {
		 	e.printStackTrace();
		 }
		 */
			
		return finalImage;
	}

	protected void handleEntry(final RunEntry p_Entry)
	{
		Run currentRun = m_Model.getCurrentRunData();
		final RunEntry lastEntry = currentRun.getLast();

		// handle first run
		if (lastEntry == null)
		{
			currentRun.add(p_Entry);
			log.debug(currentRun.toString() + "\n\n");
			return;
		}

		if (lastEntry.m_CurrentExp.equals(p_Entry.m_CurrentExp)
				|| m_Model.getAllRunData().size() > 0
				&& currentRun.size() == m_Model.getAllRunData().get(0).size())
		{
			// start a new run
			m_Model.addRun(currentRun);
			currentRun = new Run();
			m_Model.setCurrentRun(currentRun);
			currentRun.add(p_Entry);
		}
		else
		{
			//add the entry to this run
			currentRun.add(p_Entry);
			
			// log the information for this zone (used to calculate column
			// averages)
			final int start = currentRun.size() - 2;
			final ZoneEntry zoneEntry = new ZoneEntry(currentRun.getDuration(
					start, start + 1),
					currentRun.getExpGained(start, start + 1));
			m_Model.addZoneData(currentRun.size() - 1, zoneEntry);
		}

		log.debug(currentRun.toString() + "\n\n");
	}

	protected RunEntry processImage(final String p_Filename)
	{
		final Tesseract tess = Tesseract.getInstance();
		tess.setDatapath("%PROGRAMFILES(X86)%\\Tesseract-OCR\\");
		tess.setLanguage("eng");

		try
		{
			final File file = new File(
					m_Model.getPath(XPLoggerEvents.INPUT_BROWSE)
							+ File.separator + p_Filename);
			final DateTime endTime = new DateTime(file.lastModified());
			BufferedImage fullImage = ImageIO.read(file);
		
			if(m_Model.getLetterboxing()){
				int newHeight = Math.round((float)fullImage.getWidth() / 1.7777777f);
				int barSize = (fullImage.getHeight() - newHeight) / 2;
				fullImage = grabSubImage(fullImage, new Rectangle(0, barSize, fullImage.getWidth(), newHeight), 1f);
			}
			
			if(fullImage.getHeight() < 800){
				throw new ResolutionNotSupportedException("The vertical pixel count is too low. Please use screenshots with a vertical pixel count of no less than 800.\nLetterboxing may give the same results.");
			}
			
			final BufferedImage expImage = grabSubImage(fullImage,
					getExpLocation(fullImage), 7); 
			
			final String expBarHoverText = tess.doOCR(expImage).trim()
					.replaceAll("\\s", "").replace(",", "").replace(".","");
			final String paragonLevel = expBarHoverText.split("(\\(|\\))")[1]
					.intern();

			// test if the parse succeeded,
			// NumberFormatException thrown if failed, meaning exp amounts were
			// not correctly read
			Integer.parseInt(paragonLevel);

			final String exps = expBarHoverText.split(":")[1];

			final String currentExp = exps.split("/")[0];
			final String endExp = exps.split("/")[1];

			final RunEntry entry = new RunEntry(currentExp, endExp, endTime,
					paragonLevel, p_Filename);
			log.debug(entry.toString());

			return entry;

		}
		catch(final ResolutionNotSupportedException p_Exception){

			m_View.runInAsyncUIThread(new Callable<Boolean>()
			{
				@Override
				public Boolean call() throws Exception
				{
					m_View.showErrorDialog(p_Exception.getMessage());
					return true;
				}
			});
		}
		catch (final Exception p_Exception)
		{
			log.error(p_Exception.getMessage());
		}

		return null;
	}
	
	protected Rectangle getExpLocation(final BufferedImage p_Image) throws ResolutionNotSupportedException{
		int x=0, y=0, width=0, height=0;

		float ratio = (float)p_Image.getWidth() / (float)p_Image.getHeight();
		
		if( Math.abs(ratio - 1.6f) < 0.05f ){
			//16:10 aspect ratio
			x = Math.round((float)p_Image.getWidth() * 0.388f);
			y = Math.round((float)p_Image.getHeight() * 0.825f);
			width = Math.round((float)p_Image.getWidth() * 0.6093f) - x;
			height = Math.round((float)p_Image.getHeight() * 0.8495f) - y;
		}else if(Math.abs(ratio - 1.77f) < 0.05f){
			//16:9 aspect ratio
			x = Math.round((float)p_Image.getWidth() * 0.399f);
			y = Math.round((float)p_Image.getHeight() * 0.825f);
			width = Math.round((float)p_Image.getWidth() * 0.5995f) - x;
			height = Math.round((float)p_Image.getHeight() * 0.8485f) - y;
		}else if(Math.abs(ratio - 1.33) < 0.05f){
			//4:3 aspect ratio
			x = Math.round((float)p_Image.getWidth() * 0.3675f);
			y = Math.round((float)p_Image.getHeight() * 0.8333f);
			width = Math.round((float)p_Image.getWidth() * 0.6331f) - x;
			height = Math.round((float)p_Image.getHeight() * 0.85f) - y;
		}else if(Math.abs(ratio - 1.25) < 0.05f){
			//5:4 aspect ratio
			x = Math.round((float)p_Image.getWidth() * 0.3539f);
			y = Math.round((float)p_Image.getHeight() * 0.8301f);
			width = Math.round((float)p_Image.getWidth() * 0.6445f) - x;
			height = Math.round((float)p_Image.getHeight() * 0.8544f) - y;
		}
		if(x != 0){
			return new Rectangle(x, y, width, height);
		}
		
		throw new ResolutionNotSupportedException("Sorry, this image resolution is not supported: \n(" + p_Image.getWidth() + " x " + p_Image.getHeight() + ")");	
	}

	@Override
	public void propertyChange(final PropertyChangeEvent p_Event)
	{
		final XPLoggerEvents event = XPLoggerEvents.valueOf(p_Event
				.getPropertyName());

		switch (event)
		{
			case LETTERBOX:
				boolean newValue = (Boolean)p_Event.getNewValue();
				m_Model.setLetterboxing(newValue);
				break;
			case INPUT_BROWSE:
				if (m_Model.getPath(event).length() > 0)
				{
					m_Model.setPath(event, "");
					m_View.setPath(event, "");
					m_Model.setImageFilenames(new ArrayList<String>());
					m_View.setWidgetEnabled(XPLoggerEvents.START, false);
				}

				final String directory = m_View.showFolderPathPrompt(
						"Screen Directory Selection",
						"Select the location where screenshots are stored",
						getLastFilterPath());

				if (directory == null)
				{
					break;
				}

				setLastFilterPath(directory);
				m_Model.setPath(event, directory);
				m_View.setPath(event, directory);

				if (FILE_WATCHER_IS_RUNNING)
				{
					FILE_WATCHER_IS_RUNNING = false;
					m_WatcherJob.getThread().interrupt();
				}

				break;
			case OUTPUT_BROWSE:
				final String filepath = m_View.showFilePrompt(
						"Select Output File", SWT.SAVE, new String[] { "" },
						new String[] { "" }, getLastFilterPath());
				if (filepath != null)
				{
					setLastFilterPath(filepath);
					m_Model.setPath(event, filepath);
					m_View.setPath(event, filepath);
				}
				break;

			case NEW_FILE:

				final RunEntry entry = processImage(p_Event.getNewValue()
						.toString());

				if (entry != null)
				{
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

			case NEW_RUN:

				m_Model.addRun(m_Model.getCurrentRunData());
				m_Model.setCurrentRun(new Run());

				break;
			case CLEAR:
				m_Model.clearAllData();
				m_View.clearTable();

				break;
			case SCAN:
				m_Model.clearAllData();
				m_View.clearTable();
				runScanJob();

				break;
		}

		//update the gui buttons
		m_View.runInAsyncUIThread(new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				m_View.setWidgetEnabled(XPLoggerEvents.START,
						m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) != null
						&& 	m_Model.getPath(XPLoggerEvents.INPUT_BROWSE).length() > 0
								&& !FILE_WATCHER_IS_RUNNING);
				m_View.setWidgetEnabled(XPLoggerEvents.STOP,
						m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) != null
								&& FILE_WATCHER_IS_RUNNING);
				m_View.setWidgetEnabled(XPLoggerEvents.NEW_RUN, m_Model
						.getCurrentRunData().size() > 0);
				m_View.setWidgetEnabled(XPLoggerEvents.SCAN,
						m_Model.getPath(XPLoggerEvents.INPUT_BROWSE) != null
						&& 	m_Model.getPath(XPLoggerEvents.INPUT_BROWSE).length() > 0);
				return true;
			}
		});
	}
	
	protected float roundToMillions(final float p_Value)
	{
		return Math.round(p_Value / 10000f) / 100f;
	}

	protected float roundToMillions(final int p_Value)
	{
		return Math.round(p_Value / 10000f) / 100f;
	}

	protected void runScanJob()
	{
		final IRunnableWithProgress runnable = new IRunnableWithProgress()
		{

			@Override
			public void run(final IProgressMonitor p_Monitor)
					throws InvocationTargetException, InterruptedException
			{
				final Path myDir = Paths.get(m_Model
						.getPath(XPLoggerEvents.INPUT_BROWSE));

				if (myDir.getFileName().toString().length() == 0
						|| !myDir.toFile().isDirectory())
				{
					return;
				}

				final FilenameFilter filter = new FilenameFilter()
				{
					@Override
					public boolean accept(final File p_File,
							final String p_Extension)
					{
						return p_Extension.endsWith(".jpg");
					}
				};

				p_Monitor.beginTask("Scanning images", myDir.toFile()
						.listFiles(filter).length);

				for (final String filename : myDir.toFile().list(filter))
				{
					p_Monitor.worked(1);
					final RunEntry entry = processImage(filename);

					if (entry != null)
					{
						handleEntry(entry);
					}

					updateTable();
				}

			}
		};

		m_View.runInAsyncUIThreadWithProgress(runnable);
	}

	protected void updateTable()
	{

		m_View.runInAsyncUIThread(new Callable<Boolean>()
		{

			@Override
			public Boolean call() throws Exception
			{
				m_View.getShell().setRedraw(false);
				try
				{
					m_View.clearTable();

					int columnCount = 0;
					if (m_Model.getAllRunData().size() > 0)
					{
						columnCount = m_Model.getAllRunData().get(0).size() - 1;
					}
					else
					{
						columnCount = m_Model.getCurrentRunData().size() - 1;
					}

					if (columnCount <= 0)
					{
						return true;
					}
					
					m_View.addTableColumn("");

					for (int i = 0; i < columnCount; i++)
					{
						m_View.addTableColumn(Integer.toString(i + 1));
					}

					for (final ColumnNames column : ColumnNames.values())
					{
						m_View.addTableColumn(column.toString());
					}

					

					int runCount = 1;

					// put into table data for all complete runs
					for (final Run run : m_Model.getAllRunData())
					{
						final List<String> tableValues = new ArrayList<String>(
								Arrays.asList(run.toFormattedString()));
						while (tableValues.size() < columnCount)
						{
							tableValues.add("");
						}
						tableValues.add(0, Integer.toString(runCount));
						tableValues.add(m_TimeFormat.print(run
								.getTotalDuration()));
						tableValues.add(Float.toString(roundToMillions(run
								.getTotalExpGained())));
						tableValues.add(Float.toString(roundToMillions(run
								.getExpPerHour())));
						
						tableValues.add(run.getStartingParagonLevel() + " -> "
								+ run.getEndingParagonLevel());
						tableValues.add(run.getFilenames());
						m_View.addTableItem(tableValues.toArray(new String[0]));
						runCount++;
					}

					// put into table data for current run
					if (m_Model.getCurrentRunData().size() > 0)
					{
						final List<String> tableValues = new ArrayList<String>(
								Arrays.asList(m_Model.getCurrentRunData()
										.toFormattedString()));
						while (tableValues.size() < columnCount)
						{
							tableValues.add("");
						}
						tableValues.add(0, Integer.toString(runCount));
						tableValues.add(m_TimeFormat.print(m_Model
								.getCurrentRunData().getTotalDuration()));
						tableValues.add(Float.toString(roundToMillions(m_Model
								.getCurrentRunData().getTotalExpGained())));
						
						
						tableValues.add(Float.toString(roundToMillions(m_Model
								.getCurrentRunData().getExpPerHour())));
						
						tableValues.add(m_Model.getCurrentRunData()
								.getStartingParagonLevel()
								+ " -> "
								+ m_Model.getCurrentRunData()
										.getEndingParagonLevel());
						tableValues.add(m_Model.getCurrentRunData()
								.getFilenames());
						m_View.addTableItem(tableValues.toArray(new String[0]));
					}

					// put into table average data (first rows)
					final List<String> averageValues = new ArrayList<String>();
					final List<String> averageExpPerHourValues = new ArrayList<String>();
					averageValues.add("Avg");
					averageExpPerHourValues.add("Xp/Hr");
					int totalExpAverage = 0;
					Period totalPeriodAverage = new Period(0);

					for (int i = 1; i <= columnCount; i++)
					{
						final ZoneData data = m_Model.getZoneData(i);
						averageValues.add(data.getAverageExpGainedMillions()
								+ " - "
								+ Run.PERIOD_FORMAT.print(data
										.getAverageDuration()));
						totalExpAverage += data.getAverageExpGained();
						totalPeriodAverage = totalPeriodAverage.plus(data
								.getAverageDuration());

						
						averageExpPerHourValues.add(Float.toString(Math
								.round(data.getAverageExpGainedMillions()
										/ Seconds.standardSecondsIn(
												data.getAverageDuration())
												.getSeconds() * 360000f) / 100f));
					}

					averageValues.add(m_TimeFormat.print(totalPeriodAverage
							.normalizedStandard()));
					averageValues.add(Float
							.toString(roundToMillions(totalExpAverage)));
					
					float xpPerHour = roundToMillions(totalExpAverage
							/ Seconds.standardSecondsIn(
									totalPeriodAverage).getSeconds()
							* 3600f);
					averageValues.add(Float
							.toString(xpPerHour));
					
					float exptolevel = roundToMillions(m_Model.getCurrentRunData().getExpTilLevel());
					float milliseconds = (exptolevel / xpPerHour) * 3600 * 1000;
					Period timeToLevel = new Period((long)milliseconds);
					averageValues.add(m_TimeFormat.print(timeToLevel));

					
					
					m_View.insertTableItem(
							averageValues.toArray(new String[0]), 0);
					m_View.insertTableItem(
							averageExpPerHourValues.toArray(new String[0]), 1);
					
					m_View.insertTableItem(new String[0], 2);

					return true;
				}
				finally
				{
					m_View.getShell().setRedraw(true);
				}
			}

		});
	}
}
