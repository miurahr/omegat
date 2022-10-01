/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
 2015 Didier Briel
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
package org.omegat.util;

import org.junit.Test;
import org.omegat.util.HtmlUtils;

import static org.junit.Assert.*;

public class HtmlUtilsTest {

	@Test
	public void getSpacePrefix() {
		//no whitespace prefix
		assertEquals("", HtmlUtils.getSpacePrefix("a", true));
		assertEquals("", HtmlUtils.getSpacePrefix("\u0301a", true));
		assertEquals("", HtmlUtils.getSpacePrefix("\u00A0a", true));
		assertEquals("", HtmlUtils.getSpacePrefix("\u2007a", true));
		assertEquals("", HtmlUtils.getSpacePrefix("\u202Fa", true));

		//all sorts of whitespace characters
		assertEquals("\n", HtmlUtils.getSpacePrefix("\na", true));
		assertEquals("\r", HtmlUtils.getSpacePrefix("\ra", true));
		assertEquals("\u2028", HtmlUtils.getSpacePrefix("\u2028a", true)); //line separator
		assertEquals("\u2029", HtmlUtils.getSpacePrefix("\u2029a", true)); //paragraph separator
		assertEquals("\u0009", HtmlUtils.getSpacePrefix("\u0009a", true)); //h tab
		assertEquals("\u000B", HtmlUtils.getSpacePrefix("\u000Ba", true)); //v tab
		assertEquals("\f", HtmlUtils.getSpacePrefix("\fa", true)); //form feed
		assertEquals("\u001C", HtmlUtils.getSpacePrefix("\u001Ca", true)); //file separator
		assertEquals("\u001D", HtmlUtils.getSpacePrefix("\u001Da", true)); //group separator
		assertEquals("\u001E", HtmlUtils.getSpacePrefix("\u001Ea", true)); //record separator
		assertEquals("\u001F", HtmlUtils.getSpacePrefix("\u001Fa", true)); //unit separator

		//\u0301 is an accent, in a multi-code point character.
		assertEquals("one space is one space", " ", HtmlUtils.getSpacePrefix(" \u0301a", true));
		assertEquals("multiple spaces is compressed to one", " ", HtmlUtils.getSpacePrefix("  \u0301a", true));
		assertEquals("multiple spaces stay multiple spaces uncompressed", "    ", HtmlUtils.getSpacePrefix("    \u0301ap", false));
		String allWhite = "\n\r\u2028\u2029\t\n\u000B\f\n\u001C\u001D\u001E\u001F ";
		assertEquals("multiple different space types compress to the first whitespace character", "\n", HtmlUtils.getSpacePrefix(allWhite+"a", true));
		assertEquals("multiple different whtiespace characters stay that uncompressed" , allWhite, HtmlUtils.getSpacePrefix(allWhite+"a", false));

	}

	@Test
	public void getSpacePostfix() {
		//no whitespace prefix
		assertEquals("", HtmlUtils.getSpacePostfix("a", true));
		assertEquals("", HtmlUtils.getSpacePostfix("a\u0301", true));
		assertEquals("", HtmlUtils.getSpacePostfix("a\u00A0", true));
		assertEquals("", HtmlUtils.getSpacePostfix("a\u2007", true));
		assertEquals("", HtmlUtils.getSpacePostfix("a\u202F", true));

		//all sorts of whitespace characters
		assertEquals("\n", HtmlUtils.getSpacePostfix("a\n", true));
		assertEquals("\r", HtmlUtils.getSpacePostfix("a\r", true));
		assertEquals("\u2028", HtmlUtils.getSpacePostfix("a\u2028", true)); //line separator
		assertEquals("\u2029", HtmlUtils.getSpacePostfix("a\u2029", true)); //paragraph separator
		assertEquals("\u0009", HtmlUtils.getSpacePostfix("a\u0009", true)); //h tab
		assertEquals("\u000B", HtmlUtils.getSpacePostfix("a\u000B", true)); //v tab
		assertEquals("\f", HtmlUtils.getSpacePostfix("a\f", true)); //form feed
		assertEquals("\u001C", HtmlUtils.getSpacePostfix("a\u001C", true)); //file separator
		assertEquals("\u001D", HtmlUtils.getSpacePostfix("a\u001D", true)); //group separator
		assertEquals("\u001E", HtmlUtils.getSpacePostfix("a\u001E", true)); //record separator
		assertEquals("\u001F", HtmlUtils.getSpacePostfix("a\u001F", true)); //unit separator

		//\u0301 is an accent, in a multi-code point character.
		assertEquals("one space is one space", " ", HtmlUtils.getSpacePostfix("a\u0065\u0301 ", true));
		assertEquals("multiple spaces is compressed to one", " ", HtmlUtils.getSpacePostfix("a\u0065\u0301  ", true));
		assertEquals("multiple spaces stay multiple spaces uncompressed", "    ", HtmlUtils.getSpacePostfix("a\u0065\u0301    ", false));
		String allWhite = "\n\r\u2028\u2029\t\n\u000B\f\n\u001C\u001D\u001E\u001F ";
		assertEquals("multiple different space types compress to the first whitespace character", "\n", HtmlUtils.getSpacePostfix("a\u0065\u0301"+allWhite, true));
		assertEquals("multiple different whtiespace characters stay that uncompressed" , allWhite, HtmlUtils.getSpacePostfix("a\u0065\u0301"+allWhite, false));

	}

}