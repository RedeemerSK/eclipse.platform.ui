/*******************************************************************************
* Copyright (c) 2024-2025 Ole Osterhagen and others.
* 
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Ole Osterhagen - initial API and implementation
*******************************************************************************/
package org.eclipse.ui.internal.genericeditor.quicksearch;

import org.eclipse.compare.SharedDocumentAdapter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.AbstractQuicksearchSourceViewerBAK;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextViewerConfiguration;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;
import org.eclipse.ui.internal.genericeditor.compare.GenericEditorViewer.Storage;
import org.eclipse.ui.internal.genericeditor.compare.GenericEditorViewer.StorageEditorInput;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class GenericSourceViewerBAK extends AbstractQuicksearchSourceViewerBAK {

	private IEditorInput editorInput;

	public GenericSourceViewerBAK(Composite parent) {
		super(parent);
		getTextWidget().addDisposeListener(e -> disconnect());
		initialize();

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

		setDocument(documentProvider.getDocument(editorInput));

		ExtensionBasedTextViewerConfiguration configuration = new ExtensionBasedTextViewerConfiguration(null,
				new ChainedPreferenceStore(new IPreferenceStore[] { EditorsUI.getPreferenceStore(),
						GenericEditorPlugin.getDefault().getPreferenceStore() }));
		setSourceViewerConfiguration(configuration);

		unconfigure();
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
