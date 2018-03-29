package org.cytoscape.sana.sana_app.internal;

import org.cytoscape.work.TaskMonitor;


public class SanaTaskMonitor implements TaskMonitor
{

    @Override
    public void setTitle(String s)
    {

    }

    @Override
    public void setProgress(double v)
    {
        
    }

    @Override
    public void setStatusMessage(String s)
    {
        
    }

    @Override
    public void showMessage(Level level, String s)
    {
        switch(level)
        {
            case ERROR:
                	SanaUtil.errorbox(s);
                break;
		default:
			break;
        }
    }
}
