package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

/**
 * The images provided by the quicksearch plugin.
 */
public class QuickSearchPluginImages {

	/**
	 * The image registry containing <code>Image</code>s and the <code>ImageDescriptor</code>s.
	 */
	private static ImageRegistry imageRegistry;

	/**
	 * Returns the ImageRegistry.
	 *
	 * @return the ImageRegistry
	 */
	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			initializeImageRegistry();
		}
		return imageRegistry;
	}

	static ImageRegistry initializeImageRegistry() {
		return imageRegistry = new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
	}

	/**
	 * Returns the <code>Image</code> identified by the given key, or
	 * <code>null</code> if it does not exist.
	 *
	 * @param key the image's key
	 * @return the <code>Image</code> identified by the given key, or
	 *         <code>null</code> if it does not exist
	 */
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}

	/**
	 * Returns the <code>ImageDescriptor</code> identified by the given key, or
	 * <code>null</code> if it does not exist.
	 *
	 * @param key the image's key
	 * @return the <code>ImageDescriptor</code> identified by the given key, or
	 *         <code>null</code> if it does not exist
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}

}
