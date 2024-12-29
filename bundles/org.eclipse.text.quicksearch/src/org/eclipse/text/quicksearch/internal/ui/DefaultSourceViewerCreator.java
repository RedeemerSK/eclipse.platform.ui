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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ISourceViewerCreator;

/**
 * Creates DefaultSourceViewer used as fallback by Quick Search plugin-in to display file content.
 *
 * @see ISourceViewerCreator
 */
public class DefaultSourceViewerCreator implements ISourceViewerCreator {

	@Override
	public ISourceViewerHandle createSourceViewer(Composite parent) {
		return new DefaultSourceViewer(parent);
	}
}
