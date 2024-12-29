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
package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.text.quicksearch.ISourceViewerCreator;

/**
 * Creates <code>ISourceViewerCreator</code>s from an <code>IConfigurationElement</code>.
 *
 * @see ISourceViewerCreator
 */
public class ViewerDescriptor implements IViewerDescriptor {
	private final static String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private final static String EXTENSIONS_ATTRIBUTE = "extensions"; //$NON-NLS-1$
	private final static String LINKED_EDITOR_ATTRIBUTE = "linkedEditor"; //$NON-NLS-1$
	private final static String LABEL_ATTRIBUTE = "label"; //$NON-NLS-1$

	private final IConfigurationElement fConfiguration;
	private ISourceViewerCreator fViewerCreator;

	public ViewerDescriptor(IConfigurationElement config) {
		fConfiguration = config;
	}

	@Override
	public ISourceViewerCreator getViewerCreator() {
		if (fViewerCreator == null) {
			try {
				fViewerCreator = (ISourceViewerCreator) fConfiguration.createExecutableExtension(CLASS_ATTRIBUTE);
			} catch (CoreException e) {
				QuickSearchActivator.log(e);
			}
		}
		return fViewerCreator;
	}

	public String getExtension() {
		return fConfiguration.getAttribute(EXTENSIONS_ATTRIBUTE);
	}

	String getLabel() {
		return fConfiguration.getAttribute(LABEL_ATTRIBUTE);
	}

	String getLinkedEditorId() {
		return fConfiguration.getAttribute(LINKED_EDITOR_ATTRIBUTE);
	}

	String getViewerClass() {
		return fConfiguration.getAttribute(CLASS_ATTRIBUTE);
	}

	@SuppressWarnings("nls") // TODO why this does not work ?
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ViewerDescriptor ["); //$NON-NLS-1$
		if (fViewerCreator != null) {
			sb.append("viewerCreator="); //$NON-NLS-1$
			sb.append(fViewerCreator);
			sb.append(", "); //$NON-NLS-1$
		}
		String viewerClass = getViewerClass();
		if (viewerClass != null) {
			sb.append("viewerClass="); //$NON-NLS-1$
			sb.append(viewerClass);
			sb.append(", "); //$NON-NLS-1$
		}
		if (fConfiguration != null) {
			sb.append("configuration="); //$NON-NLS-1$
			sb.append(fConfiguration);
		}
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

}
