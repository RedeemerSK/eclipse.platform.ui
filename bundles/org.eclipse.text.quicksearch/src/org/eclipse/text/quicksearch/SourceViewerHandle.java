package org.eclipse.text.quicksearch;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ISourceViewerCreator.ISourceViewerHandle;

/**
 * @since 1.3
 */
public class SourceViewerHandle implements ISourceViewerHandle {

	protected final SourceViewer fSourceViewer;
	protected StyleRange[] fMatchRangers = null;

	public SourceViewerHandle(SourceViewerConfigurer sourceViewerConfigurer, Composite parent) {
		this(sourceViewerConfigurer, parent, false);
	}

	public SourceViewerHandle(SourceViewerConfigurer sourceViewerConfigurer, Composite parent, boolean addStylesMergingPresentationListener) {
		Assert.isNotNull(sourceViewerConfigurer);
		fSourceViewer = sourceViewerConfigurer.getSourceViewer(parent);
		Assert.isNotNull(fSourceViewer);
		if (addStylesMergingPresentationListener) {
			fSourceViewer.addTextPresentationListener(p -> {
				if (fMatchRangers != null && fMatchRangers.length > 0) {
					p.mergeStyleRanges(fMatchRangers);
				}
			});
		}
	}

	@Override
	public ITextViewer getSourceViewer() {
		return fSourceViewer;
	}

	@Override
	public void setViewerInput(IDocument document, StyleRange[] matchRangers, IPath filePath) {
		this.fMatchRangers = matchRangers;
		fSourceViewer.setInput(document);
		applyMatchesStyles(matchRangers, fSourceViewer);
	}

}
