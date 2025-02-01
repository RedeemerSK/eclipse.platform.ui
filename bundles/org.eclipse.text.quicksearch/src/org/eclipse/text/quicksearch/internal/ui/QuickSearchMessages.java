
package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.osgi.util.NLS;

public final class QuickSearchMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.text.quicksearch.internal.ui.QuickSearchMessages";//$NON-NLS-1$

	private QuickSearchMessages() {
		// Do not instantiate
	}

	public static String QuickSearchDialog_switchButtonTooltip;
	public static String QuickSearchDialog_defaultViewer;

	static {
		NLS.initializeMessages(BUNDLE_NAME, QuickSearchMessages.class);
	}
}
