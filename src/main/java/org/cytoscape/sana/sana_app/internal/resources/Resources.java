package org.cytoscape.sana.sana_app.internal.resources;

import javax.swing.*;
import java.net.URL;

public class Resources
{
    public static ImageIcon getTabIcon()
    {
        return icon("tabicon.png");
    }
    public static ImageIcon getLogos()
    {
        return icon("logos.png");
    }

    private static URL getResource(String res)
    {
        return Resources.class.getResource(res);
    }

    private static ImageIcon icon(String name)
    {
        URL url = getResource(name);
        return url == null ? null : new ImageIcon(url);
    }
}
