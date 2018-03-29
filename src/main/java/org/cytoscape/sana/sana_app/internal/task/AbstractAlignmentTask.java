package org.cytoscape.sana.sana_app.internal.task;

import org.cytoscape.sana.sana_app.internal.rest.parameters.AlignmentParameters;
import org.cytoscape.work.AbstractTask;

public abstract class AbstractAlignmentTask extends AbstractTask
{
    protected final AlignmentParameters common;

    public AbstractAlignmentTask(AlignmentParameters common)
    {
        this.common = common;
    }
}
