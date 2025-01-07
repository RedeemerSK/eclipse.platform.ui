package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ISourceViewerCreator.ISourceViewerInputSetter;

class DefaultSourceViewer extends SourceViewer implements ISourceViewerInputSetter {

	private static final String DISABLE_CSS = "org.eclipse.e4.ui.css.disabled"; //$NON-NLS-1$

	public DefaultSourceViewer(Composite parent, CompositeRuler verticalRules, int styles) {
		super(parent, verticalRules, null, false, styles);
		getTextWidget().setData(DISABLE_CSS, Boolean.TRUE);
	}

	@Override
	public void setViewerInput(IDocument document, StyleRange[] matchRangers, IPath filePath) {
		setInput(document);
		ISourceViewerInputSetter.applyMatchesStyles(matchRangers, this);
	};

}