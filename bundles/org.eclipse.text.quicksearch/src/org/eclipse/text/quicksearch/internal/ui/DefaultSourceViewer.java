package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

class DefaultSourceViewer extends SourceViewer {

	private static final String DISABLE_CSS = "org.eclipse.e4.ui.css.disabled"; //$NON-NLS-1$

	public DefaultSourceViewer(Composite parent, CompositeRuler verticalRules, int styles) {
		super(parent, verticalRules, null, false, styles);
		getTextWidget().setData(DISABLE_CSS, Boolean.TRUE);
	}

}