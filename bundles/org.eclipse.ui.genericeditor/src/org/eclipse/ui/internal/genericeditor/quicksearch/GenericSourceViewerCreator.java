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

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ITextViewerCreator;
import org.eclipse.text.quicksearch.SourceViewerConfigurer;
import org.eclipse.text.quicksearch.SourceViewerHandle;

/**
 * Creates quicksearch text viewer handles that use {@link GenericSourceViewer}.
 */
public class GenericSourceViewerCreator implements ITextViewerCreator {

	@Override
	public ITextViewerHandle createTextViewer(Composite parent) {
		return new SourceViewerHandle<>(new SourceViewerConfigurer<>(GenericSourceViewer::new), parent, true) {
			@Override
			public void setViewerInput(IDocument document, StyleRange[] matchRangers, IFile file) {
				this.fMatchRanges = matchRangers;
				fSourceViewer.setInput(new Input(document, file.getFullPath())); // we have to change input type
				applyMatchesStyles();
			}
		};
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
