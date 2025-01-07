package org.eclipse.text.quicksearch;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ISourceViewerCreator.ISourceViewerHandle;
import org.eclipse.text.quicksearch.ISourceViewerCreator.ISourceViewerInputSetter;

/**
 * @since 1.3
 */
public class SourceViewerHandleFactory {

	public static <T extends SourceViewer> ISourceViewerHandle createHandle(Composite parent, ISourceViewerCreator<T> viewerCreator, ISourceViewerInputSetter<T> inputSetter) {
		return createHandle(parent, viewerCreator, SourceViewerConfigurer::new, inputSetter);
	}

	public static <T extends SourceViewer> ISourceViewerHandle createHandle(Composite parent, ISourceViewerCreator<T> viewerCreator, ISourceViewerConfigurerProvider configurerProvider, ISourceViewerInputSetter<T> inputSetter) {
		Assert.isNotNull(parent);
		Assert.isNotNull(viewerCreator);
		Assert.isNotNull(configurerProvider);
		Assert.isNotNull(inputSetter);
		var ruler = new CompositeRuler();
		T viewer = viewerCreator.createSourceViewer(parent, ruler, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.READ_ONLY);
		var configurer = configurerProvider.provideSourceViewerConfigurer(viewer, ruler);
		configurer.initialize();
		return new ISourceViewerHandle() {

			@Override
			public void setViewerInput(IDocument document, StyleRange[] matchRangers, IPath filePath) {
				inputSetter.setViewerInput(viewer, document, matchRangers, filePath);
			}

			@Override
			public ITextViewer getSourceViewer() {
				return viewer;
			}
		};
	}

	public interface ISourceViewerCreator<T extends SourceViewer> {
		T createSourceViewer(Composite parent, CompositeRuler verticalRuler, int styles);
	}

	public interface ISourceViewerConfigurerProvider {
		SourceViewerConfigurer provideSourceViewerConfigurer(SourceViewer viewer, CompositeRuler vericalRuler);
	}

	public interface ISourceViewerInputSetter<T extends SourceViewer> {
		void setViewerInput(T viewer, IDocument document, StyleRange[] matchRangers, IPath filePath);
	}

}
