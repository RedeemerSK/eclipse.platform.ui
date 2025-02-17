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

import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Text viewer used as fallback by Quick Search plugin-in to display files content
 */
class DefaultSourceViewer extends SourceViewer {

	private static final String DISABLE_CSS = "org.eclipse.e4.ui.css.disabled"; //$NON-NLS-1$

	public DefaultSourceViewer(Composite parent, CompositeRuler verticalRules, int styles) {
		super(parent, verticalRules, null, false, styles);
		getTextWidget().setData(DISABLE_CSS, Boolean.TRUE);
	}

}