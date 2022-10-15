/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel, Martin Fleurke
               2010 Didier Briel
               2011 Didier Briel, Martin Fleurke
               2012 Didier Briel, Martin Fleurke
               2013 Didier Briel, Alex Buloichik
               2017 Aaron Madlon-Kay
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

package org.omegat.filters2.html2;

import org.omegat.util.HtmlUtils;

import java.util.Collection;

@Deprecated
public final class HTMLUtils {

    private HTMLUtils() {
    }

    @Deprecated
    public static String entitiesToChars(String str) {
        return HtmlUtils.entitiesToChars(str);
    }

    @Deprecated
    public static boolean isLatinLetter(int ch) {
        return HtmlUtils.isLatinLetter(ch);
    }

    @Deprecated
    public static boolean isDecimalDigit(int ch) {
        return HtmlUtils.isDecimalDigit(ch);
    }

    @Deprecated
    public static boolean isHexDigit(int ch) {
        return HtmlUtils.isHexDigit(ch);
    }

    @Deprecated
    public static String charsToEntities(String str, String encoding, Collection<String> shortcuts) {
        return HtmlUtils.charsToEntities(str, encoding, shortcuts);
    }

}
