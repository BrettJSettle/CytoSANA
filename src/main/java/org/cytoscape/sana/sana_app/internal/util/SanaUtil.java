package org.cytoscape.sana.sana_app.internal.util;

import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class SanaUtil
{

    public static void msgbox(String s)
    {
        JOptionPane.showMessageDialog(null, s, "Message", JOptionPane.WARNING_MESSAGE);
    }

    public static void errorbox(String s)
    {
        JOptionPane.showMessageDialog(null, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean ask(String s, String title)
    {
        int answer = JOptionPane.showConfirmDialog(null, s, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return answer == JOptionPane.OK_OPTION;
    }

    public static void msgboxNonBlock(String message)
    {
        final JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION);
        final JDialog dialog = new JDialog((JFrame)null, "Message", false);

        pane.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                if (e.getSource() == pane && dialog.isVisible() && JOptionPane.VALUE_PROPERTY.equals(e.getPropertyName()))
                    dialog.dispose();
            }
        });

        dialog.setContentPane(pane);
        dialog.pack();
        dialog.setVisible(true);
        dialog.toFront();
    }

    public static void msgboxAsync(final String s)
    {
        (new Thread()
        {
            @Override
            public void run()
            {
                msgboxNonBlock(s);
            }
        }).start();
    }

    public static String getStackTrace(Throwable t)
    {
        Writer w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }
    
    


    /*double[] arrayMinMax(double[] a)
    {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < a.length; ++i)
        {
            min = Math.min(min, a[i]);
            max = Math.max(max, a[i]);
        }
        return new double[] { min, max };
    }*/
}

