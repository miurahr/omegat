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

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import java.net.URL;
import java.util.Comparator;

public class PluginInformation implements Comparable<PluginInformation> {

    public enum Status {
        INSTALLED,
        BUNDLED,
        UPGRADABLE,
    }

    private final String className;
    private final String name;
    private final String version;
    private final String author;
    private final String description;
    private final String category;
    private final String link;
    private final URL url;
    private Status status;

    public PluginInformation(String className, String name, String version, String author, String description,
                             String category, String link, URL url, Status status) {
        this.className = className;
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
        this.category = category;
        this.link = link;
        this.url = url;
        this.status = status;
    }

    /**
     * @return className of plugin entry point
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return name of plugin
     */
    public String getName() {
        return name;
    }

    /**
     * @return version of plugin
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return description of plugin features
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return author(s) of plugin
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return category or type of plugin
     */
    public final String getCategory() {
        return category;
    }

    /**
     * @return link URL of plugin homepage
     */
    public final String getLink() {
        return link;
    }

    /**
     * @return manifest URL of plugin jar
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @return true if plugin is bundled with OmegaT distribution, otherwise false when 3rd party plugin
     */
    public final boolean isBundled() {
        return status == Status.BUNDLED;
    }

    /**
     * It always returns true at this time. It will be extended when handles remote 3rd party plugins in future.
     * @return true if plugin is installed and enabled, otherwise, knows but not installed, false
     */
    public final boolean isInstalled() {
        return status == Status.INSTALLED || status == Status.BUNDLED || status == Status.UPGRADABLE;
    }

    /**
     * It returns plugin status, whether installed, bundled or upgradable.
     * @return plugin status
     */
    public final Status getStatus() {
        return status;
    }

    /**
     * @param s status to set
     */
    public final void setStatus(Status s) {
        status = s;
    }

    /**
     * @return string expression of PluginInformation class.
     */
    @Override
    public String toString() {
        return "PluginInformation [className=" + className + ", name=" + name
                + ", version=" + version + ", author=" + author + ", description="
                + description + "]";
    }

    /**
     * It is identical if status is differed.
     * @return hashCode of plugin
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PluginInformation other = (PluginInformation) obj;
        if (author == null) {
            if (other.author != null) {
                return false;
            }
        } else if (!author.equals(other.author)) {
            return false;
        }
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (version == null) {
            return other.version == null;
        } else {
            return version.equals(other.version);
        }
    }

    @Override
    public final int compareTo(PluginInformation pluginInformation) {
        return Comparator.comparing(PluginInformation::getCategory, nullsLast(naturalOrder()))
                .thenComparing(PluginInformation::getAuthor, nullsLast(naturalOrder()))
                .thenComparing(PluginInformation::getClassName)
                .thenComparing(PluginInformation::getVersion, nullsLast(naturalOrder()))
                .compare(this, pluginInformation);
    }
}
