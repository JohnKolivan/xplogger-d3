package xplogger.util;

public enum ColumnNames
{
	RunTime("Run Time"),
	EarnedExp("Earned Exp"),
	ExpPerHour("Exp / Hour"),
	ParagonLevel("Paragon Level");
	
	private String m_DisplayName;
	
	ColumnNames(final String p_Text){
		m_DisplayName = p_Text;
	}
	
	
	@Override
	public String toString()
	{
		return m_DisplayName;
	}
}

