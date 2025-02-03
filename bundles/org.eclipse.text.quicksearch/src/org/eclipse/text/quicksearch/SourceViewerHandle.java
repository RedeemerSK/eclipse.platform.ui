package org.eclipse.text.quicksearch;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ISourceViewerCreator.ISourceViewerHandle;

/**
 * @since 1.3
 */
public class SourceViewerHandle<T extends SourceViewer> implements ISourceViewerHandle {

	protected final T fSourceViewer;
	protected final IChangeRulerColumn fChangeRulerColumn;
	protected final FixedLineChangedAnnotationModel fFixedLineChangeModel;
	protected StyleRange[] fMatchRangers = null;

	public SourceViewerHandle(SourceViewerConfigurer<T> sourceViewerConfigurer, Composite parent) {
		this(sourceViewerConfigurer, parent, false);
	}

	public SourceViewerHandle(SourceViewerConfigurer<T> sourceViewerConfigurer, Composite parent, boolean addStylesMergingPresentationListener) {
		Assert.isNotNull(sourceViewerConfigurer);
		fSourceViewer = sourceViewerConfigurer.getSourceViewer(parent);
		Assert.isNotNull(fSourceViewer);
		fChangeRulerColumn = sourceViewerConfigurer.getChangeRulerColumn();
		if (fChangeRulerColumn != null) {
			fFixedLineChangeModel = new FixedLineChangedAnnotationModel();
			fChangeRulerColumn.setModel(fFixedLineChangeModel);
		} else {
			fFixedLineChangeModel = null;
		}
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
	public int getVisibleLines() {
		StyledText details = fSourceViewer.getTextWidget();
		if (details != null && !details.isDisposed()) {
			int lineHeight = details.getLineHeight();
			int areaHeight = details.getClientArea().height;
			return (areaHeight + lineHeight - 1) / lineHeight;
		}
		return 0;
	}

	@Override
	public void focusMatch(IRegion visibleRange, IRegion revealedRange, int matchLine, IRegion matchRegion) {
		// limit content of the document that we can scroll to
		fSourceViewer.setVisibleRegion(visibleRange.getOffset(), visibleRange.getLength());
		// scroll to range to be presented
		fSourceViewer.revealRange(revealedRange.getOffset(), revealedRange.getLength());
		// sets caret position
		fSourceViewer.setSelectedRange(matchRegion.getOffset(), 0);
		// does horizontal scrolling if necessary to reveal 1st occurrence in target line
		fSourceViewer.revealRange(matchRegion.getOffset(), matchRegion.getLength());

		if (fFixedLineChangeModel != null) {
			fFixedLineChangeModel.selectedMatchLine = matchLine;
			fChangeRulerColumn.redraw();
		}
	}

	@Override
	public void matchLineSelected(int line) {
		if (fFixedLineChangeModel != null) {
			fFixedLineChangeModel.selectedMatchLine = line;
			fChangeRulerColumn.redraw();
		}
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

	private static class FixedLineChangedAnnotationModel implements IAnnotationModel, ILineDiffer {

		int selectedMatchLine;

		@Override
		public void addAnnotationModelListener(IAnnotationModelListener listener) {
			// no-op

		}

		@Override
		public void removeAnnotationModelListener(IAnnotationModelListener listener) {
			// no-op
		}

		@Override
		public void connect(IDocument document) {
			// no-op
		}

		@Override
		public void disconnect(IDocument document) {
			// no-op
		}

		@Override
		public void addAnnotation(Annotation annotation, Position position) {
			// no-op
		}

		@Override
		public void removeAnnotation(Annotation annotation) {
			// no-op
		}

		@Override
		public Iterator<Annotation> getAnnotationIterator() {
			return Collections.emptyIterator();
		}

		@Override
		public Position getPosition(Annotation annotation) {
			return null;
		}

		@Override
		public ILineDiffInfo getLineInfo(int line) {
			return line == selectedMatchLine ? FixedLineChangedDiffInfo.INSTANCE : null;
		}

		@Override
		public void revertLine(int line) throws BadLocationException {
			// no-op
		}

		@Override
		public void revertBlock(int line) throws BadLocationException {
			// no-op
		}

		@Override
		public void revertSelection(int line, int nLines) throws BadLocationException {
			// no-op
		}

		@Override
		public int restoreAfterLine(int line) throws BadLocationException {
			// no-op
			return 0;
		}

	}

	private static class FixedLineChangedDiffInfo implements ILineDiffInfo {

		static final FixedLineChangedDiffInfo INSTANCE = new FixedLineChangedDiffInfo();

		@Override
		public int getRemovedLinesBelow() {
			return 0;
		}

		@Override
		public int getRemovedLinesAbove() {
			return 0;
		}

		@Override
		public int getChangeType() {
			return CHANGED;
		}

		@Override
		public boolean hasChanges() {
			return true;
		}

		@Override
		public String[] getOriginalText() {
			return new String[0];
		}

	}

}
