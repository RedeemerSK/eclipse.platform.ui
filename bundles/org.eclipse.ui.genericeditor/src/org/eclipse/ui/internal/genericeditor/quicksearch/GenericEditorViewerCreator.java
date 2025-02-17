/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jozef Tomek - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.quicksearch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ITextViewerCreator;
import org.eclipse.text.quicksearch.SourceViewerConfigurer;
import org.eclipse.text.quicksearch.SourceViewerHandle;

/**
 * Creates quicksearch text viewer handles that use
 * {@link GenericEditorViewer2}.
 */
public class GenericEditorViewerCreator implements ITextViewerCreator {

	@Override
	public ITextViewerHandle createTextViewer(Composite parent) {
		return new GenericEditorSourceViewerHandle(parent);
	}

	private class GenericEditorSourceViewerHandle extends SourceViewerHandle<GenericEditorViewer>
			implements ITextPresentationListener {
		private boolean fNewDocumentReconciliation;

		// after focusing on other match in the same document, not all reconciliations
		// are performed again (e.g. LSP reconciliation is done only after setting new
		// input document), so we collect all applied styles to be able to set them
		// after other match focus manually
		private boolean fDoCollectStyles;
		private TextPresentation fMergedStylesPresentation;

		private boolean fScheduleMatchRangesPresentation = true;

		public GenericEditorSourceViewerHandle(Composite parent) {
			super(new SourceViewerConfigurer<>(GenericEditorViewer::new), parent);
			fSourceViewer.addTextPresentationListener(this);
		}

		/*
		 * triggered variable number of times by a) tm4e code (possibly after setInput()
		 * and/or focusMatch() -> fSourceViewer.setVisibleRegion() ), b) lsp4e code
		 * (zero or more times after setInput() only)
		 */
		@Override
		public void applyTextPresentation(TextPresentation textPresentation) {
			if (fDoCollectStyles) {
				StyleRange[] ranges = new StyleRange[textPresentation.getDenumerableRanges()];
				int i = 0;
				for (Iterator<StyleRange> iter = textPresentation.getAllStyleRangeIterator(); iter.hasNext();) {
					ranges[i++] = iter.next();
				}
				mergeStylesToTextPresentation(fMergedStylesPresentation, ranges);
			}
			if (fScheduleMatchRangesPresentation) {
				fScheduleMatchRangesPresentation = false;
				fSourceViewer.getTextWidget().getDisplay().asyncExec(() -> applyMatchRangesTextPresentation());
			}
		}

		private void mergeStylesToTextPresentation(TextPresentation textPresentation, StyleRange[] styleRanges) {
			if (styleRanges != null && styleRanges.length > 0) {
				// mergeStyleRanges() modifies passed ranges so we need to clone
				var ranges = new StyleRange[styleRanges.length];
				for (int i = 0; i < ranges.length; i++) {
					ranges[i] = (StyleRange) styleRanges[i].clone();
				}
				textPresentation.mergeStyleRanges(ranges);
			}
		}

		private void applyMatchRangesTextPresentation() {
			applyMatchesStyles();
			fScheduleMatchRangesPresentation = true;

		}

		@Override
		public void setViewerInput(IDocument document, StyleRange[] matchRangers, IFile file) {
			fNewDocumentReconciliation = true;
			fMergedStylesPresentation = new TextPresentation(1024);
			super.setViewerInput(document, matchRangers, file);
		}

		@Override
		public void focusMatch(IRegion visibleRegion, IRegion revealedRange, int matchLine, IRegion matchRange) {
			if (fNewDocumentReconciliation) {
				fNewDocumentReconciliation = false;
				fDoCollectStyles = true;
				super.focusMatch(visibleRegion, revealedRange, matchLine, matchRange);
			} else {
				fDoCollectStyles = false;
				fScheduleMatchRangesPresentation = false; // temporary disable scheduling match ranges presentation
				super.focusMatch(visibleRegion, revealedRange, matchLine, matchRange);
				// now apply collected styles
				fSourceViewer.changeTextPresentation(fMergedStylesPresentation, false);
				applyMatchRangesTextPresentation(); // also enables scheduling match ranges presentation
				fDoCollectStyles = true;
			}
		}
	}

	static class Input implements ITypedElement, IEncodedStreamContentAccessor {

		private final IPath filePath;
		private final byte[] content;

		public Input(IDocument document, IPath filePath) {
			this.filePath = filePath;
			this.content = document.get().getBytes(StandardCharsets.UTF_8);
		}

		@Override
		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(content);
		}

		@Override
		public String getCharset() throws CoreException {
			return StandardCharsets.UTF_8.name();
		}

		@Override
		public String getName() {
			return filePath.lastSegment();
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public String getType() {
			return filePath.getFileExtension();
		}

	}

}
