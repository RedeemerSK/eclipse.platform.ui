package org.eclipse.text.quicksearch;

import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.text.quicksearch.internal.ui.QuickSearchDialog;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * @since 1.3
 */
public class SourceViewerConfigurer {

	protected final SourceViewer fSourceViewer;
	protected final CompositeRuler fVerticalRuler;
	private final IPropertyChangeListener fPropertyChangeListener = this::handlePreferenceStoreChanged;
	private final LineNumberRulerColumn fLineNumberRulerColumn = new LineNumberRulerColumn();
	protected IPreferenceStore fPreferenceStore;
	private Font fFont;

//	protected SourceViewerConfiguration fConfiguration;

	protected SourceViewerConfigurer(SourceViewer viewer, CompositeRuler verticalRuler) {
		this(viewer, verticalRuler, EditorsUI.getPreferenceStore());
	}

	protected SourceViewerConfigurer(SourceViewer viewer, CompositeRuler verticalRuler, IPreferenceStore store) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(verticalRuler);
		Assert.isNotNull(store);
		fVerticalRuler = verticalRuler;
		fSourceViewer = viewer;
		fSourceViewer.addVerticalRulerColumn(fLineNumberRulerColumn);
		fPreferenceStore = store;
	}

	protected void initialize() {
		fPreferenceStore.addPropertyChangeListener(fPropertyChangeListener);

		initializeColors();
		initializeFont();

//		if (fConfiguration != null) {
//			fSourceViewer.configure(fConfiguration);
//		}

		var currentLineDecorations = new SourceViewerDecorationSupport(fSourceViewer, null, null, EditorsUI.getSharedTextColors());
		currentLineDecorations.setCursorLinePainterPreferenceKeys(EDITOR_CURRENT_LINE, EDITOR_CURRENT_LINE_COLOR);
		currentLineDecorations.install(fPreferenceStore);

		updateContributedRulerColumns((CompositeRuler) fVerticalRuler);

		fSourceViewer.getControl().addDisposeListener(e -> {
			currentLineDecorations.uninstall();
			fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
		});
	}

	/**
	 * Initializes the fore- and background colors of this viewer for both normal and selected text.
	 */
	protected void initializeColors() {

		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			ISharedTextColors sharedColors = EditorsUI.getSharedTextColors();
			var textWidget = fSourceViewer.getTextWidget();

			// ----------- foreground color --------------------
			Color color= store.getBoolean(PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: sharedColors.getColor(getColorFromStore(store, PREFERENCE_COLOR_FOREGROUND));
			textWidget.setForeground(color);

			// ---------- background color ----------------------
			color= store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: sharedColors.getColor(getColorFromStore(store, PREFERENCE_COLOR_BACKGROUND));
			textWidget.setBackground(color);

			// ----------- selection foreground color --------------------
			color= store.getBoolean(PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: sharedColors.getColor(getColorFromStore(store, PREFERENCE_COLOR_SELECTION_FOREGROUND));
			textWidget.setSelectionForeground(color);


			// ---------- selection background color ----------------------
			color= store.getBoolean(PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: sharedColors.getColor(getColorFromStore(store, PREFERENCE_COLOR_SELECTION_BACKGROUND));
			textWidget.setSelectionBackground(color);

			fLineNumberRulerColumn.setBackground(textWidget.getBackground());

			var lineNumbersColor = getColorFromStore(store, EDITOR_LINE_NUMBER_RULER_COLOR);
			if (lineNumbersColor == null) {
				lineNumbersColor = new RGB(0, 0, 0);
			}
			fLineNumberRulerColumn.setForeground(sharedColors.getColor(lineNumbersColor));
		}
	}

	protected RGB getColorFromStore(IPreferenceStore store, String key) {
		return QuickSearchDialog.getColorFromStore(store, key);
	}

	/**
	 * Initializes the given viewer's font.
	 *
	 * @param viewer the viewer
	 * @since 2.0
	 */
	private void initializeFont() {

		boolean isSharedFont= true;
		Font font= null;
		String symbolicFontName= getSymbolicFontName();

		if (symbolicFontName != null)
			font= JFaceResources.getFont(symbolicFontName);
		else if (fPreferenceStore != null) {
			// Backward compatibility
			if (fPreferenceStore.contains(JFaceResources.TEXT_FONT) && !fPreferenceStore.isDefault(JFaceResources.TEXT_FONT)) {
				FontData data= PreferenceConverter.getFontData(fPreferenceStore, JFaceResources.TEXT_FONT);

				if (data != null) {
					isSharedFont= false;
					font= new Font(fSourceViewer.getTextWidget().getDisplay(), data);
				}
			}
		}
		if (font == null)
			font= JFaceResources.getTextFont();

		if (!font.equals(fSourceViewer.getTextWidget().getFont())) {
			setFont(font);

			disposeFont();
			if (!isSharedFont)
				fFont= font;
		} else if (!isSharedFont) {
			font.dispose();
		}
	}

	/**
	 * Sets the font for the given viewer sustaining selection and scroll position.
	 *
	 * @param sourceViewer the source viewer
	 * @param font the font
	 * @since 2.0
	 */
	private void setFont(Font font) {
		if (fSourceViewer.getDocument() != null) {

			ISelectionProvider provider= fSourceViewer.getSelectionProvider();
			ISelection selection= provider.getSelection();
			int topIndex= fSourceViewer.getTopIndex();

			Control parent= fSourceViewer.getControl();
			parent.setRedraw(false);

			fSourceViewer.getTextWidget().setFont(font);

			fVerticalRuler.setFont(font);

			provider.setSelection(selection);
			fSourceViewer.setTopIndex(topIndex);

			if (parent instanceof Composite composite) {
				composite.layout(true);
			}

			parent.setRedraw(true);
		} else {
			fSourceViewer.getTextWidget().setFont(font);
			fVerticalRuler.setFont(font);
		}
	}

	/**
	 * Disposes of the non-shared font.
	 */
	private void disposeFont() {
		if (fFont != null) {
			fFont.dispose();
			fFont= null;
		}
	}

	/**
	 * Returns the property preference key for the editor font.
	 * <p>
	 * If the editor is defined with a <code>symbolicFontName </code> then this name is returned and
	 * the font is looked up in the JFace resource registry. Otherwise,
	 * {@link JFaceResources#TEXT_FONT} is returned and the font is looked up in this editor's
	 * preference store.
	 * </p>
	 *
	 * @return a String with the key
	 * @since 2.1
	 */
	protected final String getFontPropertyPreferenceKey() {
		String symbolicFontName= getSymbolicFontName();
		if (symbolicFontName != null)
			return symbolicFontName;
		return JFaceResources.TEXT_FONT;
	}

	protected String getSymbolicFontName() {
		return null;
	}

	/**
	 * Returns this viewers's preference store or <code>null</code> if none has
	 * been set yet. When {@link #initialize()} is called and no preference store has been set,
	 * preference store is set to {@link EditorsUI#getPreferenceStore()}.
	 *
	 * @return this viewers's preference store which may be <code>null</code>
	 */
	protected final IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}

//	/**
//	 * Sets source viewer configuration used to configure this source viewer.
//	 * This method must be called before call to {@link #initialize()}. If not, this viewer uses a
//	 * <code>SourceViewerConfiguration</code>.
//	 *
//	 * @param configuration the source viewer configuration object
//	 */
//	protected void setSourceViewerConfiguration(SourceViewerConfiguration configuration) {
//		Assert.isNotNull(configuration);
//		fConfiguration= configuration;
//	}
//
//	/**
//	 * Returns this source viewer's configuration. May return <code>null</code>
//	 * before call to {@link #initialize()} and after disposal.
//	 *
//	 * @return this source viewer's configuration which may be <code>null</code>
//	 */
//	protected final SourceViewerConfiguration getSourceViewerConfiguration() {
//		return fConfiguration;
//	}

	/**
	 * Adds additional ruler contributions to the vertical ruler.
	 * <p>
	 * Default implementation does nothing, clients may replace.</p>
	 *
	 * @param ruler the composite ruler to add contributions to
	 */
	protected void updateContributedRulerColumns(CompositeRuler ruler) {
		// no-op in default implementation
	}

	/**
	 * Handles a property change event describing a change of the editor's
	 * preference store and updates the preference related editor properties.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 *
	 * @param event the property change event
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String property= event.getProperty();

		if (getFontPropertyPreferenceKey().equals(property)) {
			initializeFont();
			return;
		}

		if (property != null) {
			switch (property) {
			case PREFERENCE_COLOR_FOREGROUND:
			case PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT:
			case PREFERENCE_COLOR_BACKGROUND:
			case PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT:
			case PREFERENCE_COLOR_SELECTION_FOREGROUND:
			case PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT:
			case PREFERENCE_COLOR_SELECTION_BACKGROUND:
			case PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT:
			case EDITOR_LINE_NUMBER_RULER_COLOR:
				initializeColors();
				return;
			default:
				break;
			}
		}

		if (affectsTextPresentation(event))
			fSourceViewer.invalidateTextPresentation();

//		getTextWidget().redraw();
//		lineNumberRulerColumn.redraw();
	}

	/**
	 * Determines whether the given preference change affects the editor's
	 * presentation. This implementation always returns <code>false</code>.
	 * May be reimplemented by subclasses.
	 *
	 * @param event the event which should be investigated
	 * @return <code>true</code> if the event describes a preference change affecting the viewers's presentation
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return false;
	}

	public interface IQuickSearchSourceViewerProvider {
		SourceViewer createSourceViewer(Composite parent, CompositeRuler verticalRuler, int styles);
	}

}
