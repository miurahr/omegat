/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
               2021 Hiroshi Miura
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.core.data;

import java.net.URL;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.omegat.filters2.master.PluginUtils;

public final class PluginInformationFactory {
    private static final String PLUGIN_NAME = "Plugin-Name";
    private static final String PLUGIN_VERSION = "Plugin-Version";
    private static final String PLUGIN_AUTHOR = "Plugin-Author";
    private static final String PLUGIN_DESCRIPTION = "Plugin-Description";
    private static final String PLUGIN_CATEGORY = "Plugin-Category";
    private static final String PLUGIN_LINK = "Plugin-Link";
    private static final String PLUGIN_TYPE = "OmegaT-Plugin";
    private static final String IMPLEMENTATION_VENDOR = "Implementation-Vendor";
    private static final String IMPLEMENTATION_TITLE = "Implementation-Title";
    private static final String IMPLEMENTATION_VERSION = "Implementation-Version";
    private static final String BUNDLE_VERSION = "Bundle-Version";
    private static final String BUNDLE_NAME = "Bundle-Name";
    private static final String BUILT_BY = "Built-By";

    private PluginInformationFactory() {
    }

    public static PluginInformation buildFromManifest(final String className, final Manifest manifest, final URL mu,
                                                      final PluginInformation.Status status) {
        Attributes mainAttrs = manifest.getMainAttributes();
        Attributes attrs = manifest.getEntries().get(className);
        if (attrs == null) {
              attrs = manifest.getMainAttributes();
        }
        String categoryKey1 = attrs.getValue(PLUGIN_CATEGORY);
        String categoryKey = categoryKey1 != null ? categoryKey1 : attrs.getValue(PLUGIN_TYPE);
        return new PluginInformation(className, findName(attrs, className), findVersion(attrs, mainAttrs),
                findAuthor(mainAttrs), attrs.getValue(PLUGIN_DESCRIPTION),
                PluginUtils.PluginType.getTypeByValue(categoryKey), attrs.getValue(PLUGIN_LINK), mu, status);
    }

    public static PluginInformation buildFromProperties(String className, Properties props, final String key,
                                                        final URL mu, final PluginInformation.Status status) {
        return new PluginInformation(className, className.substring(className.lastIndexOf(".") + 1),
                null, null, null, PluginUtils.PluginType.getTypeByValue(key),
                null, mu, status);
    }

    private static String findName(Attributes attrs, String className) {

        if (attrs.getValue(PLUGIN_NAME) != null) {
            return attrs.getValue(PLUGIN_NAME);
        } else if (attrs.getValue(BUNDLE_NAME) != null) {
            return attrs.getValue(BUNDLE_NAME);
        } else if (attrs.getValue(IMPLEMENTATION_TITLE) != null) {
            return attrs.getValue(IMPLEMENTATION_TITLE);
        }
        // fallback to className
        return className.substring(className.lastIndexOf(".") + 1);
    }

    private static String findVersion(Attributes attrs, Attributes mainAttrs) {
        if (attrs.getValue(PLUGIN_VERSION) != null) {
            return attrs.getValue(PLUGIN_VERSION);
        } else if (attrs.getValue(BUNDLE_VERSION) != null) {
            return attrs.getValue(BUNDLE_VERSION);
        } else if (attrs.getValue(IMPLEMENTATION_VERSION) != null) {
            return attrs.getValue(IMPLEMENTATION_VERSION);
        } else if (mainAttrs.getValue(PLUGIN_VERSION) != null) {
            return mainAttrs.getValue(PLUGIN_VERSION);
        } else if (mainAttrs.getValue(BUNDLE_VERSION) != null) {
            return mainAttrs.getValue(BUNDLE_VERSION);
        } else if (mainAttrs.getValue(IMPLEMENTATION_VERSION) != null) {
            return mainAttrs.getValue(IMPLEMENTATION_VERSION);
        }
        return "unknown";
    }

    private static String findAuthor(Attributes attrs) {
        if ("org.omegat.Main".equals(attrs.getValue("Main-Class"))) {
            return "OmegaT team";
        } else if (attrs.getValue(PLUGIN_AUTHOR) != null) {
            return attrs.getValue(PLUGIN_AUTHOR);
        } else if (attrs.getValue(IMPLEMENTATION_VENDOR) != null) {
            return attrs.getValue(IMPLEMENTATION_VENDOR);
        } else if (attrs.getValue(BUILT_BY) != null) {
            return attrs.getValue(BUILT_BY);
        }
        return null;
    }
}
