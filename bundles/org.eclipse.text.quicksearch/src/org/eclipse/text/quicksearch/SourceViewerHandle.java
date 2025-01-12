package org.eclipse.text.quicksearch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ISourceViewerCreator.ISourceViewerHandle;

/**
 * @since 1.3
 */
public class SourceViewerHandle<T extends SourceViewer> implements ISourceViewerHandle {

	protected final T fSourceViewer;
	protected StyleRange[] fMatchRangers = null;

	public SourceViewerHandle(SourceViewerConfigurer<T> sourceViewerConfigurer, Composite parent) {
		this(sourceViewerConfigurer, parent, false);
	}

	public SourceViewerHandle(SourceViewerConfigurer<T> sourceViewerConfigurer, Composite parent, boolean addStylesMergingPresentationListener) {
		Assert.isNotNull(sourceViewerConfigurer);
		fSourceViewer = sourceViewerConfigurer.getSourceViewer(parent);
		Assert.isNotNull(fSourceViewer);
		if (addStylesMergingPresentationListener) {
			fSourceViewer.addTextPresentationListener(p -> {
				if (fMatchRangers != null && fMatchRangers.length > 0) {
					// mergeStyleRanges() modifies passed ranges so we need to clone
					var ranges = new StyleRange[fMatchRangers.length];
					for (int i = 0; i < ranges.length; i++) {
						ranges[i] = (StyleRange) fMatchRangers[i].clone();
					}
					p.mergeStyleRanges(ranges);
				}
			});
		}
	}

	@Override
	public T getSourceViewer() {
		return fSourceViewer;
	}

	@Override
	public void setViewerInput(IDocument document, StyleRange[] matchRangers, IFile file) {
		setViewerInput(document, matchRangers, file, true);
	}

	protected void setViewerInput(IDocument document, StyleRange[] matchRangers, @SuppressWarnings("unused") IFile file, boolean applyMatchStyles) {
		this.fMatchRangers = matchRangers;
		fSourceViewer.setInput(document);
		if (applyMatchStyles) {
			applyMatchesStyles(matchRangers, fSourceViewer);
		}
	}

}
