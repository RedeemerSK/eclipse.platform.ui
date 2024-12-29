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
package org.eclipse.text.quicksearch;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;

/**
 * A factory object for <code>SourceViewer</code> provided via <code>ISourceViewerHandle</code>.
 * <p>
 * This interface is only required when creating a <code>SourceViewer</code> from a plugin.xml file.
 * Since <code>SourceViewer</code>s have no default constructor they cannot be
 * instantiated directly with <code>Class.forName</code>.
 * @see ISourceViewerHandle
 * @since 1.3
 */
public interface ISourceViewerCreator {

	/**
	 * Creates a new source viewer under the given SWT parent control and returns handle for it.
	 *
	 * @param parent the SWT parent control under which to create the viewer's SWT control
	 * @return a new source viewer handle
	 */
	ISourceViewerHandle createSourceViewer(Composite parent);

	/**
	 * Source viewer handle is a wrapper for source viewer. Plugins providing <code>ISourceViewerCreator</code>
	 * extension are free to return their custom implementation that suits their need for book-keeping created
	 * source viewer.
	 *
	 * @see ISourceViewerCreator
	 */
	public interface ISourceViewerHandle {

		ITextViewer getSourceViewer();

		/**
		 * Sets input to the source viewer represented by the handle.
		 * @param document document to show in viewer
		 * @param matchRangers styles to apply to found matches in presented document
		 * @param filePath where the search match is found
		 */
		void setViewerInput(IDocument document, StyleRange[] matchRangers, IPath filePath);

		default void applyMatchesStyles(StyleRange[] ranges) {
			if (ranges == null || ranges.length == 0) {
				return;
			}
			StyleRange last = ranges[ranges.length - 1];
			getSourceViewer().getTextWidget().replaceStyleRanges(ranges[0].start, last.start + last.length - ranges[0].start, ranges);
		}
	}

}
