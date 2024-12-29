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

import org.eclipse.compare.SharedDocumentAdapter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextViewerConfiguration;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;
import org.eclipse.ui.internal.genericeditor.compare.GenericEditorViewer.Storage;
import org.eclipse.ui.internal.genericeditor.compare.GenericEditorViewer.StorageEditorInput;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class GenericSourceViewer extends SourceViewer {

	private IEditorInput editorInput;

	public GenericSourceViewer(Composite parent, CompositeRuler verticalRuler, int styles) {
		super(parent, verticalRuler, null, false, styles);
		getTextWidget().addDisposeListener(e -> disconnect());

	}

//	@Override
//	public ISelection getSelection() {
//		return StructuredSelection.EMPTY;
//	}
//
//	@Override
//	public void setSelection(ISelection selection, boolean reveal) {
//		// empty implementation
//	}

	@Override
	public void refresh() {
		// empty implementation
	}

	@Override
	public void setInput(Object input) {
		disconnect();

		editorInput = new StorageEditorInput(new Storage<>((GenericSourceViewerCreator.Input) input));

		IDocumentProvider documentProvider = SharedDocumentAdapter.getDocumentProvider(editorInput);
		try {
			documentProvider.connect(editorInput);
		} catch (CoreException ex) {
			GenericEditorPlugin.getDefault().getLog()
					.log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, ex.getMessage(), ex));
		}

		unconfigure();

		setDocument(documentProvider.getDocument(editorInput));

		ExtensionBasedTextViewerConfiguration configuration = new ExtensionBasedTextViewerConfiguration(null,
				new ChainedPreferenceStore(new IPreferenceStore[] { EditorsUI.getPreferenceStore(),
						GenericEditorPlugin.getDefault().getPreferenceStore() }));

		configure(configuration);
	}

	private void disconnect() {
		if (editorInput != null) {
			setDocument(null);
			SharedDocumentAdapter.getDocumentProvider(editorInput).disconnect(editorInput);
			editorInput = null;
		}
	}

}
