package org.cytoscape.sana.sana_app.internal;

import java.util.Properties;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.sana.sana_app.internal.rest.AlignmentResource;
import org.cytoscape.sana.sana_app.internal.rest.endpoints.impl.AlignmentResourceImpl;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class CyActivator extends AbstractCyActivator {
	static CySwingAppAdapter swingAdapter;
	public static CxTaskFactoryManager tfManager;
	private SanaApp app;
	public final static String SANA_URL_PROPERTY = "sana.url";
	private final static String SANA_URL = "http://v1.sana.test.cytoscape.io";
	public static Properties cyProps;

	private ServiceTracker ciResponseFactoryTracker = null;
	// private ServiceTracker ciExceptionFactoryTracker = null;
	private ServiceTracker ciErrorFactoryTracker = null;

	@Override
	public void start(BundleContext context) throws Exception {
		tfManager = new CxTaskFactoryManager();
		registerServiceListener(context, tfManager, "addReaderFactory", "removeReaderFactory",
				InputStreamTaskFactory.class);
		registerServiceListener(context, tfManager, "addWriterFactory", "removeWriterFactory",
				CyNetworkViewWriterFactory.class);
		Properties properties = new Properties();

		swingAdapter = getService(context, CySwingAppAdapter.class);
		
		@SuppressWarnings("unchecked")
		CyProperty<Properties> props = getService(context, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		cyProps = props.getProperties();
		
		app = new SanaApp(swingAdapter);

		registerAllServices(context, app, properties);
		registerService(context, app, SetCurrentNetworkListener.class, new Properties());

		ciResponseFactoryTracker = new ServiceTracker(context,
				context.createFilter("(objectClass=org.cytoscape.ci.CIResponseFactory)"), null);
		ciResponseFactoryTracker.open();
		// this.getService(context, CIResponseFactory.class);
		// ciExceptionFactoryTracker = new ServiceTracker(context,
		// context.createFilter("(objectClass=org.cytoscape.ci.CIExceptionFactory)"),
		// null);
		// ciExceptionFactoryTracker.open();
		// this.getService(context, CIExceptionFactory.class);
		ciErrorFactoryTracker = new ServiceTracker(context,
				context.createFilter("(objectClass=org.cytoscape.ci.CIErrorFactory)"), null);
		ciErrorFactoryTracker.open();
		// this.getService(context, CIErrorFactory.class);

		AlignmentResourceImpl restResource = new AlignmentResourceImpl(ciResponseFactoryTracker, ciErrorFactoryTracker);
		registerService(context, restResource, AlignmentResource.class);

	}

	public static String getSanaURLProperty() {
		return cyProps.getProperty(SANA_URL_PROPERTY, SANA_URL);
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		app.teardown();

		if (ciResponseFactoryTracker != null) {
			ciResponseFactoryTracker.close();
		}
		// if (ciExceptionFactoryTracker != null) {
		// ciExceptionFactoryTracker.close();
		// }
		if (ciErrorFactoryTracker != null) {
			ciErrorFactoryTracker.close();
		}
		super.shutDown();
	}
}
