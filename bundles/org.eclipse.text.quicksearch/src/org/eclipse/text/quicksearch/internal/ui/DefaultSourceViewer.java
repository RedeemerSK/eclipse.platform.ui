package org.eclipse.text.quicksearch.internal.ui;

import static org.eclipse.jface.resource.JFaceResources.TEXT_FONT;
import static org.eclipse.text.quicksearch.internal.ui.QuickSearchDialog.getColorFromStore;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ISourceViewerCreator.ISourceViewerHandle;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

class DefaultSourceViewer extends SourceViewer implements ISourceViewerHandle, IPropertyChangeListener {

	private static final String DISABLE_CSS = "org.eclipse.e4.ui.css.disabled"; //$NON-NLS-1$

	private LineNumberRulerColumn lineNumberRulerColumn;

	public DefaultSourceViewer(Composite parent) {
		super(parent, new CompositeRuler(), SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		getTextWidget().setFont(JFaceResources.getFont(TEXT_FONT));
		getTextWidget().setData(DISABLE_CSS, Boolean.TRUE);
		addVerticalRulerColumn(lineNumberRulerColumn = new LineNumberRulerColumn());
		setColors();

		var currentLineDecorations = new SourceViewerDecorationSupport(this, null, null, EditorsUI.getSharedTextColors());
		currentLineDecorations.setCursorLinePainterPreferenceKeys(EDITOR_CURRENT_LINE, EDITOR_CURRENT_LINE_COLOR);
		currentLineDecorations.install(EditorsUI.getPreferenceStore());

		EditorsUI.getPreferenceStore().addPropertyChangeListener(this);
		getControl().addDisposeListener(e -> {
			currentLineDecorations.uninstall();
			EditorsUI.getPreferenceStore().removePropertyChangeListener(this);
		});
	}

	@Override
	public ITextViewer getSourceViewer() {
		return this;
	}

	@Override
	public void setViewerInput(IDocument document, StyleRange[] matchRangers, IPath filePath) {
		setInput(document);
		applyMatchesStyles(matchRangers);
	};

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (getTextWidget().isDisposed()) {
			return;
		}
		var prop = event.getProperty();
		if (prop.equals(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR)
				|| prop.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				|| prop.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)
				|| prop.equals(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
				|| prop.equals(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND)) {
			setColors();
			getTextWidget().redraw();
			lineNumberRulerColumn.redraw();
		}
	}

	private void setColors() {
		var textWidget = getTextWidget();
		RGB background = null;
		RGB foreground = null;
		ISharedTextColors sharedColors = EditorsUI.getSharedTextColors();

		var isUsingSystemBackground = EditorsUI.getPreferenceStore()
				.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		if (!isUsingSystemBackground) {
			background = getColorFromStore(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
		}
		if (background != null) {
			var color = sharedColors.getColor(background);
			textWidget.setBackground(color);
			lineNumberRulerColumn.setBackground(color);
		} else {
			textWidget.setBackground(null);
			lineNumberRulerColumn.setBackground(null);
		}

		var isUsingSystemForeground = EditorsUI.getPreferenceStore()
				.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT);
		if (!isUsingSystemForeground) {
			foreground = getColorFromStore(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND);
		}
		if (foreground != null) {
			textWidget.setForeground(sharedColors.getColor(foreground));
		} else {
			textWidget.setForeground(null);
		}

		var lineNumbersColor =  getColorFromStore(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR);
		if (lineNumbersColor == null) {
			lineNumbersColor = new RGB(0, 0, 0);
		}
		lineNumberRulerColumn.setForeground(sharedColors.getColor(lineNumbersColor));
	}
}