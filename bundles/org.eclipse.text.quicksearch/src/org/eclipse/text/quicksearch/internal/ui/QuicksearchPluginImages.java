package org.eclipse.text.quicksearch.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

/**
 * The images provided by the quicksearch plugin.
 */
public class QuicksearchPluginImages {

	/**
	 * The image registry containing <code>Image</code>s and the <code>ImageDescriptor</code>s.
	 */
	private static ImageRegistry imageRegistry;

	/* Declare Common paths */
	private static URL ICON_BASE_URL= null;

	static {
		String pathSuffix = "icons/full/"; //$NON-NLS-1$
		ICON_BASE_URL= QuickSearchActivator.getDefault().getBundle().getEntry(pathSuffix);
	}

	// Use IPath and toOSString to build the names to ensure they have the slashes correct
	private final static String LOCALTOOL= "clcl16/"; //basic colors - size 16x16 //$NON-NLS-1$

	/**
	 * Declare all images
	 */
	private static void declareImages() {
		declareRegistryImage(IInternalQuicksearchConstants.IMG_LCL_VIEWER, LOCALTOOL + "viewer.png"); //$NON-NLS-1$

	}

	/**
	 * Declare an Image in the registry table.
	 * @param key 	The key to use when registering the image
	 * @param path	The path where the image can be found. This path is relative to where
	 *				this plugin class is found (i.e. typically the packages directory)
	 */
	private final static void declareRegistryImage(String key, String path) {
		ImageDescriptor desc= ImageDescriptor.getMissingImageDescriptor();
		try {
			desc= ImageDescriptor.createFromURL(makeIconFileURL(path));
		} catch (MalformedURLException me) {
			QuickSearchActivator.log(me);
		}
		imageRegistry.put(key, desc);
	}

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

	/**
	 * Initialize the image registry by declaring all of the required graphics. This
	 * involves creating JFace image descriptors describing how to create/find the
	 * image should it be needed. The image is not actually allocated until
	 * requested.
	 *
	 * <pre>
	 * 	Prefix conventions
	 *		Wizard Banners			WIZBAN_
	 *		Preference Banners		PREF_BAN_
	 *		Property Page Banners	PROPBAN_
	 *		Color toolbar			CTOOL_
	 *		Enable toolbar			ETOOL_
	 *		Disable toolbar			DTOOL_
	 *		Local enabled toolbar	ELCL_
	 *		Local Disable toolbar	DLCL_
	 *		Object large			OBJL_
	 *		Object small			OBJS_
	 *		View 					VIEW_
	 *		Product images			PROD_
	 *		Misc images				MISC_
	 *
	 *	Where are the images?
	 *		The images (typically pngs) are found in the same location as this plugin class.
	 *		This may mean the same package directory as the package holding this class.
	 *		The images are declared using this.getClass() to ensure they are looked up via
	 *		this plugin class.
	 * </pre>
	 *
	 * @return the initialized ImageRegistry
	 * @see org.eclipse.jface.resource.ImageRegistry
	 */
	public static ImageRegistry initializeImageRegistry() {
		imageRegistry = new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
		declareImages();
		return imageRegistry;
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

	private static URL makeIconFileURL(String iconPath) throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}

		return new URL(ICON_BASE_URL, iconPath);
	}
}
