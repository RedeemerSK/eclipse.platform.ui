/*******************************************************************************
 * Copyright (c) 2013-2025 Pivotal Software, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *    Jozef Tomek - source viewers extension
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.text.quicksearch.internal.core.preferences.QuickSearchPreferences;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class QuickSearchActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.text.quicksearch"; //$NON-NLS-1$


	private static final String SOURCE_VIEWERS_EXTENSION_POINT= "sourceViewers"; //$NON-NLS-1$
	private static final String CONTENT_TYPE_BINDING = "contentTypeBinding"; //$NON-NLS-1$
	private static final String VIEWER_TAG = "viewer"; //$NON-NLS-1$
	private static final String FILE_VIEWER_ID_ATTRIBUTE = "sourceViewerId"; //$NON-NLS-1$
	private static final String DEFAULT_CREATOR_CLASS = DefaultSourceViewerCreator.class.getName();

	private static final IContentTypeManager fgContentTypeManager = Platform.getContentTypeManager();

	// The shared instance
	private static QuickSearchActivator plugin;

	private final ExtensionsRegistry<ViewerDescriptor> fFileViewers = new ExtensionsRegistry<>();

	// Lazy initialized
	private QuickSearchPreferences prefs = null;
	private ResourceBundle fResourceBundle;
	private boolean fRegistryInitialized;
	private IViewerDescriptor fDefaultViewerDescriptor;

	/**
	 * The constructor
	 */
	public QuickSearchActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static QuickSearchActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void log(Throwable exception) {
		log(createErrorStatus(exception));
	}

	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 0, message, null));
	}

	public static void log(IStatus status) {
//		if (logger == null) {
			getDefault().getLog().log(status);
//		}
//		else {
//			logger.logEntry(status);
//		}
	}

	public static IStatus createErrorStatus(Throwable exception) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, exception.getMessage(), exception);
	}

	public QuickSearchPreferences getPreferences() {
		if (prefs==null) {
			prefs = new QuickSearchPreferences(QuickSearchActivator.getDefault().getPreferenceStore());
		}
		return prefs;
	}

	private ResourceBundle getResourceBundle() {
		if (fResourceBundle == null)
			fResourceBundle = Platform.getResourceBundle(getBundle());
		return fResourceBundle;
	}

	private static String getFormattedString(String key, String arg) {
		try {
			return MessageFormat.format(getDefault().getResourceBundle().getString(key), arg);
		} catch (MissingResourceException e) {
			return "!" + key + "!";	//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	private static String getFormattedString(String key, String arg0, String arg1) {
		try {
			return MessageFormat.format(getDefault().getResourceBundle().getString(key), arg0, arg1);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	static class ExtensionsRegistry<T> {
		private final static String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
		private final static String EXTENSIONS_ATTRIBUTE = "extensions"; //$NON-NLS-1$
		private final static String CONTENT_TYPE_ID_ATTRIBUTE = "contentTypeId"; //$NON-NLS-1$

		private HashMap<String, T> fIdMap;	// maps ids to data
		private HashMap<String, List<T>> fExtensionMap;	// multimap: maps extensions to list of data
		private HashMap<IContentType, List<T>> fContentTypeBindings; // multimap: maps content type bindings to list of data

		void register(IConfigurationElement element, T data) {
			String id = element.getAttribute(ID_ATTRIBUTE);
			if (id != null) {
				if (fIdMap == null)
					fIdMap = new HashMap<>();
				fIdMap.put(id, data);
			}

			String types = element.getAttribute(EXTENSIONS_ATTRIBUTE);
			if (types != null) {
				if (fExtensionMap == null)
					fExtensionMap = new HashMap<>();
				StringTokenizer tokenizer = new StringTokenizer(types, ","); //$NON-NLS-1$
				while (tokenizer.hasMoreElements()) {
					String extension = tokenizer.nextToken().trim();
					List<T> l = fExtensionMap.get(normalizeCase(extension));
					if (l == null)
						fExtensionMap.put(normalizeCase(extension),	l = new ArrayList<>());
					l.add(data);
				}
			}
		}

		void createBinding(IConfigurationElement element, String idAttributeName) {
			String type = element.getAttribute(CONTENT_TYPE_ID_ATTRIBUTE);
			String id = element.getAttribute(idAttributeName);
			if (id == null)
				log(getFormattedString("QuickSearchActivator.targetIdAttributeMissing", idAttributeName)); //$NON-NLS-1$
			if (type != null && id != null && fIdMap != null) {
				T o = fIdMap.get(id);
				if (o != null) {
					IContentType ct = fgContentTypeManager.getContentType(type);
					if (ct != null) {
						if (fContentTypeBindings == null)
							fContentTypeBindings = new HashMap<>();
						List<T> l = fContentTypeBindings.get(ct);
						if (l == null)
							fContentTypeBindings.put(ct, l = new ArrayList<>());
						l.add(o);
					} else {
						log(getFormattedString("QuickSearchActivator.contentTypeNotFound", type)); //$NON-NLS-1$
					}
				} else {
					log(getFormattedString("QuickSearchActivator.targetNotFound", id)); //$NON-NLS-1$
				}
			}
		}

		T search(IContentType type) {
			List<T> list = searchAll(type);
			return list != null ? list.get(0) : null;
		}

		List<T> searchAll(IContentType type) {
			if (fContentTypeBindings != null) {
				for (; type != null; type = type.getBaseType()) {
					List<T> data = fContentTypeBindings.get(type);
					if (data != null)
						return data;
				}
			}
			return null;
		}

		T search(String extension) {
			List<T> list = searchAll(extension);
			return list != null ? list.get(0) : null;
		}

		List<T> searchAll(String extension) {
			if (fExtensionMap != null)
				return fExtensionMap.get(normalizeCase(extension));
			return null;
		}

		Collection<T> getAll() {
			return fIdMap == null ? Collections.emptySet() : fIdMap.values();
		}

		private static String normalizeCase(String s) {
			return s == null ? null : s.toUpperCase();
		}
	}

	private void initializeExtensionsRegistry() {
		if (!fRegistryInitialized) {
			registerExtensions();
			Assert.isNotNull(fDefaultViewerDescriptor);
			fRegistryInitialized = true;
		}
	}

	private void registerExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// collect all descriptors which define the source viewer extension point
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(PLUGIN_ID, SOURCE_VIEWERS_EXTENSION_POINT);
		for (IConfigurationElement element : elements) {
			String name = element.getName();
			if (!CONTENT_TYPE_BINDING.equals(name)) {
				if (!VIEWER_TAG.equals(name))
					log(getFormattedString("QuickSearchActivator.unexpectedTag", name, VIEWER_TAG)); //$NON-NLS-1$
				var viewerDescriptor = new ViewerDescriptor(element);
				fFileViewers.register(element, viewerDescriptor);
				if (DEFAULT_CREATOR_CLASS.equals(viewerDescriptor.getViewerClass())) {
					fDefaultViewerDescriptor = viewerDescriptor;
				}
			}
		}
		for (IConfigurationElement element : elements) {
			if (CONTENT_TYPE_BINDING.equals(element.getName()))
				fFileViewers.createBinding(element, FILE_VIEWER_ID_ATTRIBUTE);
		}
	}

	public IViewerDescriptor getDefaultViewer() {
		initializeExtensionsRegistry();
		return fDefaultViewerDescriptor;
	}

	public List<IViewerDescriptor> getViewers(IFile file) {
		initializeExtensionsRegistry();
		if (file == null) {
			return List.of(getDefaultViewer());
		}
		return findContentViewerDescriptor(file);
	}

	private List<IViewerDescriptor> findContentViewerDescriptor(IFile input) {
		LinkedHashSet<IViewerDescriptor> result = new LinkedHashSet<>();

		String name = input.getName();

		IContentDescription cDescr = null;
		try {
			cDescr = input.getContentDescription();
		} catch (CoreException e) {
			log(e);
		}
		IContentType ctype = cDescr == null ? null : cDescr.getContentType();
		if (ctype == null) {
			ctype = fgContentTypeManager.findContentTypeFor(name);
		}
		if (ctype != null) {
			List<ViewerDescriptor> list = fFileViewers.searchAll(ctype);
			if (list != null) {
				result.addAll(list);
			}
		}

		String type = input.getFileExtension();
		if (type != null) {
			List<ViewerDescriptor> list = fFileViewers.searchAll(type);
			if (list != null)
				result.addAll(list);
		}

		Set<ViewerDescriptor> editorLinkedDescriptors = findEditorLinkedDescriptors(name, ctype, false);
		result.addAll(editorLinkedDescriptors);

		if (result.isEmpty() || result.size() == 1) {
			// single candidate should always be the default viewer, but in case it's not, add default viewer as well
			result.add(fDefaultViewerDescriptor);
		} else {
			// more than 1 candidate, make sure default viewer is the last one
			result.remove(fDefaultViewerDescriptor);
			result.add(fDefaultViewerDescriptor);
		}
		return new ArrayList<>(result);
	}

	/**
	 * @param fileName      file name for content in search match preview panel
	 * @param contentType   possible content type for content in search match preview panel, may be null
	 * @param firstIsEnough stop searching once first match is found
	 * @return set of descriptors which could be found for given content type via "linked" editor
	 */
	Set<ViewerDescriptor> findEditorLinkedDescriptors(String fileName, IContentType contentType,
			boolean firstIsEnough) {
		if (fileName == null && contentType == null) {
			return Collections.emptySet();
		}
		if (contentType == null) {
			contentType = fgContentTypeManager.findContentTypeFor(fileName);
		}

		LinkedHashSet<ViewerDescriptor> viewers = fFileViewers.getAll().stream()
				.filter(vd -> vd.getLinkedEditorId() != null).collect(Collectors.toCollection(LinkedHashSet::new));
		if (viewers.isEmpty()) {
			return Collections.emptySet();
		}

		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		LinkedHashSet<ViewerDescriptor> result = new LinkedHashSet<>();
		IEditorDescriptor[] editors = editorReg.getEditors(fileName, contentType);
		for (IEditorDescriptor ed : editors) {
			addLinkedEditorContentTypes(viewers, firstIsEnough, ed.getId(), result);
			if (firstIsEnough && !result.isEmpty()) {
				return result;
			}
		}
		return result;
	}

	private void addLinkedEditorContentTypes(LinkedHashSet<ViewerDescriptor> viewers, boolean firstIsEnough,
			String editorId, Set<ViewerDescriptor> result) {
		Stream<ViewerDescriptor> stream = viewers.stream().filter(vd -> editorId.equals(vd.getLinkedEditorId()));
		if (firstIsEnough) {
			Optional<ViewerDescriptor> first = stream.findFirst();
			if (first.isPresent()) {
				result.add(first.get());
			}
		} else {
			stream.collect(Collectors.toCollection(() -> result));
		}
	}

}
