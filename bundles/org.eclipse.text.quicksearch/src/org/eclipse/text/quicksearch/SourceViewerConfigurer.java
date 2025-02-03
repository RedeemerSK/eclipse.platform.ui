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
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineNumberChangeRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.text.quicksearch.SourceViewerHandle.FixedLineHighlighter;
import org.eclipse.text.quicksearch.internal.ui.QuickSearchDialog;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * @since 1.3
 */
public class SourceViewerConfigurer<T extends SourceViewer> {

	public static final int VIEWER_STYLES = SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.READ_ONLY;

	private final ISourceViewerConstructor<T> fViewerCreator;
	private final IPropertyChangeListener fPropertyChangeListener = this::handlePreferenceStoreChanged;
	private final LineNumberChangeRulerColumn fLineNumberRulerColumn = new LineNumberChangeRulerColumn(EditorsUI.getSharedTextColors());
	private final FixedLineHighlighter fMatchLineHighlighter = new FixedLineHighlighter();

	protected final CompositeRuler fVerticalRuler = new CompositeRuler();
	protected final IPreferenceStore fPreferenceStore;
	protected T fSourceViewer;
	private Font fFont;

	public SourceViewerConfigurer(ISourceViewerConstructor<T> viewerCreator) {
		this(EditorsUI.getPreferenceStore(), viewerCreator);
	}

	public SourceViewerConfigurer(IPreferenceStore store, ISourceViewerConstructor<T> viewerCreator) {
		Assert.isNotNull(store);
		Assert.isNotNull(viewerCreator);
		fViewerCreator = viewerCreator;
		fPreferenceStore = store;
	}

	protected T getSourceViewer(Composite parent) {
		fSourceViewer = fViewerCreator.createSourceViewer(parent, fVerticalRuler, VIEWER_STYLES);
		Assert.isNotNull(fSourceViewer);
		fSourceViewer.addVerticalRulerColumn(fLineNumberRulerColumn);
		initialize();
		return fSourceViewer;
	}

	protected IChangeRulerColumn getChangeRulerColumn() {
		return fLineNumberRulerColumn;
	}

	protected FixedLineHighlighter getMatchLineHighlighter() {
		return fMatchLineHighlighter;
	}

	protected void initialize() {
		fPreferenceStore.addPropertyChangeListener(fPropertyChangeListener);

		initializeColors();
		initializeFont();

		fSourceViewer.getTextWidget().addLineBackgroundListener(fMatchLineHighlighter);

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

			// ----------- line numbers color --------------------
			var lineNumbersColor = getColorFromStore(store, EDITOR_LINE_NUMBER_RULER_COLOR);
			if (lineNumbersColor == null) {
				lineNumbersColor = new RGB(0, 0, 0);
			}
			fLineNumberRulerColumn.setForeground(sharedColors.getColor(lineNumbersColor));

			// ----------- line highlight (background) color --------------------
			color = sharedColors.getColor(getColorFromStore(EditorsUI.getPreferenceStore(), EDITOR_CURRENT_LINE_COLOR));
			fLineNumberRulerColumn.setChangedColor(sharedColors.getColor(reverseInterpolateDiffPainterColor(textWidget.getBackground(), color)));
			if (fMatchLineHighlighter != null) {
				fMatchLineHighlighter.setHighlightColor(color);
			}

		}
	}

	private RGB reverseInterpolateDiffPainterColor(Color backgroundColor, Color finalColor) {
		RGB baseRGB= finalColor.getRGB();
		RGB background= backgroundColor.getRGB();

		boolean darkBase= isDark(baseRGB);
		boolean darkBackground= isDark(background);
		if (darkBase && darkBackground)
			background= new RGB(255, 255, 255);
		else if (!darkBase && !darkBackground)
			background= new RGB(0, 0, 0);

		// reverse interpolate
		double scale = 0.6;
		double scaleInv = 1.0 - scale;
		return new RGB((int) ((baseRGB.red - scale * background.red) / scaleInv), (int) ((baseRGB.green - scale * background.green) / scaleInv), (int) ((baseRGB.blue - scale * background.blue) / scaleInv));
	}


	/**
	 * Returns whether the given color is dark or light depending on the colors grey-scale level.
	 *
	 * @param rgb the color
	 * @return <code>true</code> if the color is dark, <code>false</code> if it is light
	 */
	private static boolean isDark(RGB rgb) {
		return greyLevel(rgb) > 128;
	}

	/**
	 * Returns the grey value in which the given color would be drawn in grey-scale.
	 *
	 * @param rgb the color
	 * @return the grey-scale value
	 */
	private static double greyLevel(RGB rgb) {
		if (rgb.red == rgb.green && rgb.green == rgb.blue)
			return rgb.red;
		return (0.299 * rgb.red + 0.587 * rgb.green + 0.114 * rgb.blue + 0.5);
	}

	protected RGB getColorFromStore(IPreferenceStore store, String key) {
		return QuickSearchDialog.getColorFromStore(store, key);
	}

	/**
	 * Initializes the given viewer's font.
	 *
	 * @param viewer the viewer
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

	public interface ISourceViewerConstructor<T extends SourceViewer> {
		T createSourceViewer(Composite parent, CompositeRuler verticalRuler, int styles);
	}

}
