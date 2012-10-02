package xplogger.views;

import org.eclipse.swt.widgets.Composite;

import gov.usgs.nwrc.internal.view.classes.AbstractViewPart;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import xplogger.events.XPLoggerEvents;
import org.eclipse.swt.widgets.Table;

public class XPLoggerView extends AbstractViewPart implements IXPLoggerView, SelectionListener
{
	public static String ID = "xplogger.views.xploggerview";
	
	private Text m_TextDir;
	private Button	m_ButtonDirBrowse;
	private Button	m_ButtonStart;
	private Table m_Table;

	private Composite	m_TableComposite;
	private Composite composite;
	private Button m_ButtonClear;

	private Button	m_ButtonStop;
	private Button m_ButtonNewRun;
	private Button m_ButtonMakeCopy;
	

	@Override
	public void createPartControl(Composite parent)
	{
		parent.setLayout(new GridLayout(3, false));
		
		Label m_LabelDir = new Label(parent, SWT.NONE);
		m_LabelDir.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		m_LabelDir.setText("Screenshot Directory");
		
		m_TextDir = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		m_TextDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		m_ButtonDirBrowse = new Button(parent, SWT.NONE);
		GridData gd_m_ButtonDirBrowse = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_m_ButtonDirBrowse.widthHint = 80;
		m_ButtonDirBrowse.setLayoutData(gd_m_ButtonDirBrowse);
		m_ButtonDirBrowse.setText("Browse...");
		m_ButtonDirBrowse.setData(XPLoggerEvents.INPUT_BROWSE);
		m_ButtonDirBrowse.addSelectionListener(this);
		new Label(parent, SWT.NONE);
		
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite.setLayout(new GridLayout(4, false));
		
		m_ButtonStart = new Button(composite, SWT.NONE);
		GridData gd_m_ButtonStartService = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_m_ButtonStartService.widthHint = 80;
		m_ButtonStart.setLayoutData(gd_m_ButtonStartService);
		m_ButtonStart.setText("Start");	
		m_ButtonStart.setEnabled(false);
		m_ButtonStart.setData(XPLoggerEvents.START);
		m_ButtonStart.addSelectionListener(this);
		
		m_ButtonStop = new Button(composite, SWT.NONE);
		m_ButtonStop.setEnabled(false);
		GridData gd_m_ButtonStop = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_m_ButtonStop.widthHint = 80;
		m_ButtonStop.setLayoutData(gd_m_ButtonStop);
		m_ButtonStop.setText("Stop");
		m_ButtonStop.setData(XPLoggerEvents.STOP);
		m_ButtonStop.addSelectionListener(this);
		
		m_ButtonNewRun = new Button(composite, SWT.NONE);
		GridData gd_m_ButtonNewRun = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_m_ButtonNewRun.widthHint = 80;
		m_ButtonNewRun.setLayoutData(gd_m_ButtonNewRun);
		m_ButtonNewRun.setText("New Run");
		m_ButtonNewRun.addSelectionListener(this);
		m_ButtonNewRun.setData(XPLoggerEvents.NEW_RUN);
		m_ButtonNewRun.setEnabled(false);
		
		m_ButtonClear = new Button(composite, SWT.NONE);
		GridData gd_m_ButtonClear = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_m_ButtonClear.widthHint = 80;
		m_ButtonClear.setLayoutData(gd_m_ButtonClear);
		m_ButtonClear.setText("Clear");
		m_ButtonClear.setData(XPLoggerEvents.CLEAR);
		m_ButtonClear.addSelectionListener(this);

		m_ButtonMakeCopy = new Button(parent, SWT.NONE);
		GridData gd_m_ButtonMakeCopy = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_m_ButtonMakeCopy.widthHint = 80;
		m_ButtonMakeCopy.setLayoutData(gd_m_ButtonMakeCopy);
		m_ButtonMakeCopy.setText("Make Copy");
		m_ButtonMakeCopy.addSelectionListener(this);
		m_ButtonMakeCopy.setData(XPLoggerEvents.MAKE_COPY);
		m_ButtonMakeCopy.setVisible(false);
		m_ButtonMakeCopy.setEnabled(false);
		
		Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd_label.widthHint = 317;
		label.setLayoutData(gd_label);
		
		m_TableComposite = new Composite(parent, SWT.NONE);
		m_TableComposite.setLayout(new GridLayout(1, false));
		m_TableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		m_Table = new Table(m_TableComposite, SWT.HIDE_SELECTION | SWT.VIRTUAL);
		m_Table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		m_Table.setHeaderVisible(true);
		m_Table.setLinesVisible(true);
	}

	@Override
	public void widgetSelected(final SelectionEvent p_Event)
	{
		XPLoggerEvents event = XPLoggerEvents.valueOf(p_Event.widget.getData().toString());
		firePropertyChange(event.toString());
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent p_Event)
	{
		//this is not the method you are looking for	
	}
	
	@Override
	public void setWidgetEnabled(final XPLoggerEvents p_Widget, final boolean p_Enabled){
		
		switch(p_Widget){
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
			case MAKE_COPY:
				m_ButtonMakeCopy.setEnabled(p_Enabled);
				break;
		}
	}
	
	@Override
	public void setPath(final XPLoggerEvents p_Widget, final String p_Path){
		m_TextDir.setText(p_Path);
	}
	
	@Override
	public void addTableColumn(final String p_ColumnName){	
		TableColumn column = new TableColumn(m_Table, SWT.CENTER, m_Table.getColumnCount());
		column.setText(p_ColumnName);
		column.setAlignment(SWT.CENTER);
		resizeColumns();
	}
	
	@Override
	public void addTableColumn(final String p_ColumnName, final int p_Width){
		TableColumn column = new TableColumn(m_Table, SWT.CENTER, m_Table.getColumnCount());
		column.setText(p_ColumnName);
		column.setAlignment(SWT.CENTER);
		column.setWidth(p_Width);
		resizeColumns();
	}
	
	@Override 
	public void insertTableItem(final String[] p_Values, final int p_Index){
		TableItem item = new TableItem(m_Table, SWT.CENTER, p_Index);
		item.setText(p_Values);
		resizeColumns();
	}
	
	@Override
	public void replaceTableItem(final String[] p_Values, final int p_Index){
		m_Table.remove(p_Index);
		TableItem item = new TableItem(m_Table, SWT.CENTER, p_Index);
		resizeColumns();
	}
	
	@Override
	public void addTableItem(final String[] p_Values){
		TableItem item = new TableItem(m_Table, SWT.CENTER);
		item.setText(p_Values);
		resizeColumns();
	}
	
	@Override
	public void clearTable(){
		//m_Table.clearAll();
		m_Table.removeAll();
		m_Table.setRedraw(false);
		for(TableColumn column : m_Table.getColumns()){
			column.dispose();
		}
		m_Table.setRedraw(true);
	}
	
	protected void resizeColumns(){
		for(TableColumn column : m_Table.getColumns()){
			column.pack();
		}
	}
}
