package xplogger.views;

import gov.usgs.nwrc.internal.view.classes.AbstractViewPart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import xplogger.events.XPLoggerEvents;

public class XPLoggerView extends AbstractViewPart implements IXPLoggerView,
		SelectionListener
{
	public static String	ID	= "xplogger.views.xploggerview";
	private Text			m_TextDir;

	private Button			m_ButtonDirBrowse;
	private Button			m_ButtonStart;
	private Table			m_Table;
	private Composite		m_TableComposite;

	private Composite		composite;
	private Button			m_ButtonClear;
	private Button			m_ButtonStop;

	private Button			m_ButtonNewRun;
	private Button			m_ButtonScanDirectory;

	private Button 			m_ButtonLetterbox;

	public XPLoggerView()
	{
	}

	@Override
	public void addTableColumn(final String p_ColumnName)
	{
		final TableColumn column = new TableColumn(m_Table, SWT.CENTER,
				m_Table.getColumnCount());
		column.setText(p_ColumnName);
		column.setAlignment(SWT.CENTER);
		resizeColumns();
	}

	@Override
	public void addTableColumn(final String p_ColumnName, final int p_Width)
	{
		final TableColumn column = new TableColumn(m_Table, SWT.CENTER,
				m_Table.getColumnCount());
		column.setText(p_ColumnName);
		column.setAlignment(SWT.CENTER);
		column.setWidth(p_Width);
		resizeColumns();
	}

	@Override
	public void addTableItem(final String[] p_Values)
	{
		final TableItem item = new TableItem(m_Table, SWT.CENTER);
		item.setText(p_Values);
		resizeColumns();
	}

	@Override
	public void clearTable()
	{
		// m_Table.clearAll();
		m_Table.removeAll();
		m_Table.setRedraw(false);
		for (final TableColumn column : m_Table.getColumns())
		{
			column.dispose();
		}
		m_Table.setRedraw(true);
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		parent.setLayout(new GridLayout(3, false));

		final Label m_LabelDir = new Label(parent, SWT.NONE);
		m_LabelDir.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		m_LabelDir.setText("Screenshot Directory");

		m_TextDir = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		m_TextDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		m_ButtonDirBrowse = new Button(parent, SWT.NONE);
		final GridData gd_m_ButtonDirBrowse = new GridData(SWT.LEFT,
				SWT.CENTER, false, false, 1, 1);
		gd_m_ButtonDirBrowse.widthHint = 80;
		m_ButtonDirBrowse.setLayoutData(gd_m_ButtonDirBrowse);
		m_ButtonDirBrowse.setText("Browse...");
		m_ButtonDirBrowse.setData(XPLoggerEvents.INPUT_BROWSE);
		m_ButtonDirBrowse.addSelectionListener(this);
		
		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		composite_1.setLayout(new GridLayout(1, false));
		m_ButtonLetterbox = new Button(composite_1, SWT.CHECK);
		m_ButtonLetterbox.setText("Letterboxing");
		m_ButtonLetterbox.addSelectionListener(this);
		m_ButtonLetterbox.setData(XPLoggerEvents.LETTERBOX);

		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		composite.setLayout(new GridLayout(4, false));

		m_ButtonStart = new Button(composite, SWT.NONE);
		final GridData gd_m_ButtonStartService = new GridData(SWT.LEFT,
				SWT.CENTER, false, false, 1, 1);
		gd_m_ButtonStartService.widthHint = 80;
		m_ButtonStart.setLayoutData(gd_m_ButtonStartService);
		m_ButtonStart.setText("Start");
		m_ButtonStart.setEnabled(false);
		m_ButtonStart.setData(XPLoggerEvents.START);
		m_ButtonStart.addSelectionListener(this);

		m_ButtonStop = new Button(composite, SWT.NONE);
		m_ButtonStop.setEnabled(false);
		final GridData gd_m_ButtonStop = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		gd_m_ButtonStop.widthHint = 80;
		m_ButtonStop.setLayoutData(gd_m_ButtonStop);
		m_ButtonStop.setText("Stop");
		m_ButtonStop.setData(XPLoggerEvents.STOP);
		m_ButtonStop.addSelectionListener(this);

		m_ButtonNewRun = new Button(composite, SWT.NONE);
		final GridData gd_m_ButtonNewRun = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		gd_m_ButtonNewRun.widthHint = 80;
		m_ButtonNewRun.setLayoutData(gd_m_ButtonNewRun);
		m_ButtonNewRun.setText("New Run");
		m_ButtonNewRun.addSelectionListener(this);
		m_ButtonNewRun.setData(XPLoggerEvents.NEW_RUN);
		m_ButtonNewRun.setEnabled(false);

		m_ButtonClear = new Button(composite, SWT.NONE);
		final GridData gd_m_ButtonClear = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		gd_m_ButtonClear.widthHint = 80;
		m_ButtonClear.setLayoutData(gd_m_ButtonClear);
		m_ButtonClear.setText("Clear");
		m_ButtonClear.setData(XPLoggerEvents.CLEAR);
		m_ButtonClear.addSelectionListener(this);

		m_ButtonScanDirectory = new Button(parent, SWT.NONE);
		final GridData gd_m_ButtonScanDirectory = new GridData(SWT.LEFT,
				SWT.CENTER, false, false, 1, 1);
		gd_m_ButtonScanDirectory.widthHint = 80;
		m_ButtonScanDirectory.setLayoutData(gd_m_ButtonScanDirectory);
		m_ButtonScanDirectory.setText("Scan Existing");
		m_ButtonScanDirectory.addSelectionListener(this);
		m_ButtonScanDirectory.setData(XPLoggerEvents.SCAN);
		m_ButtonScanDirectory.setEnabled(false);

		final Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		final GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1);
		gd_label.widthHint = 317;
		label.setLayoutData(gd_label);

		m_TableComposite = new Composite(parent, SWT.NONE);
		m_TableComposite.setLayout(new GridLayout(1, false));
		m_TableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 3, 1));

		m_Table = new Table(m_TableComposite, SWT.HIDE_SELECTION | SWT.VIRTUAL);
		m_Table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		m_Table.setHeaderVisible(true);
		m_Table.setLinesVisible(true);
	}

	@Override
	public void insertTableItem(final String[] p_Values, final int p_Index)
	{
		final TableItem item = new TableItem(m_Table, SWT.CENTER, p_Index);
		item.setText(p_Values);
		resizeColumns();
	}

	@Override
	public void replaceTableItem(final String[] p_Values, final int p_Index)
	{
		m_Table.remove(p_Index);
		new TableItem(m_Table, SWT.CENTER, p_Index);
		resizeColumns();
	}

	protected void resizeColumns()
	{
		for (final TableColumn column : m_Table.getColumns())
		{
			column.pack();
		}
	}

	@Override
	public void setPath(final XPLoggerEvents p_Widget, final String p_Path)
	{
		m_TextDir.setText(p_Path);
	}

	@Override
	public void setWidgetEnabled(final XPLoggerEvents p_Widget,
			final boolean p_Enabled)
	{

		switch (p_Widget)
		{
			case INPUT_BROWSE:
				m_ButtonDirBrowse.setEnabled(p_Enabled);
				break;
			case START:
				m_ButtonStart.setEnabled(p_Enabled);
				break;
			case STOP:
				m_ButtonStop.setEnabled(p_Enabled);
				break;
			case NEW_RUN:
				m_ButtonNewRun.setEnabled(p_Enabled);
				break;
			case SCAN:
				m_ButtonScanDirectory.setEnabled(p_Enabled);
				break;
		}
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent p_Event)
	{
		// this is not the method you are looking for
	}

	@Override
	public void widgetSelected(final SelectionEvent p_Event)
	{
		final XPLoggerEvents event = XPLoggerEvents.valueOf(p_Event.widget
				.getData().toString());
		
		switch (event){
			case LETTERBOX:
				boolean value = ((Button) p_Event.widget).getSelection();
				firePropertyChange(event.toString(), !value, value);
				break;
			default:
				firePropertyChange(event.toString());
				break;
					
		}
		
	}
}
