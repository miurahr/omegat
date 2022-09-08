/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Didier Briel, Guido Leenders
               2019-2020 Thomas Cordonnier
               2022 Hiroshi Miura
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

package org.omegat.gui.glossary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.omegat.core.Core;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

/**
 * Reader for TBX glossaries.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Didier Briel
 * @author Guido Leenders
 */
public final class GlossaryReaderTBX {
    private static final XMLInputFactory factory;

    static {
        factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    private GlossaryReaderTBX() {
    }

    public static List<GlossaryEntry> read(final File file, boolean priorityGlossary) throws Exception {
        try (InputStream is = Files.newInputStream(file.toPath())) {
            return readMartif(is, priorityGlossary, file.getName());
        }
    }

    public static List<GlossaryEntry> read(final String data, boolean priorityGlossary, String origin) {
        try (InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))) {
            return readMartif(is, priorityGlossary, origin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<GlossaryEntry> readMartif(final InputStream is, boolean priorityGlosary,
            String origin) throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(is);
        List<GlossaryEntry> entries = new ArrayList<>();
        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals("termEntry")
                        || reader.getLocalName().equals("conceptEntry")) {
                    readTermEntry(entries, reader, priorityGlosary, origin);
                }
            }
        }
        return entries;
    }

    private static void readTermEntry(List<GlossaryEntry> result, final XMLStreamReader reader,
            boolean priorityGlossary, String origin) throws Exception {
        String sLang = Core.getProject().getProjectProperties().getSourceLanguage().getLanguageCode();
        String tLang = Core.getProject().getProjectProperties().getTargetLanguage().getLanguageCode();

        StringBuilder note = new StringBuilder();
        StringBuilder descTerm = new StringBuilder();
        List<String> sTerms = new ArrayList<>();
        List<String> tTerms = new ArrayList<>();

        while (reader.hasNext()) {
            int next = reader.next();
            if ((next == XMLStreamConstants.END_ELEMENT) && (reader.getLocalName().equals("termEntry")
                    || reader.getLocalName().equals("conceptEntry"))) {
                for (String s : sTerms) {
                    boolean addedForLang = false;
                    StringBuilder comment = new StringBuilder();
                    appendLine(comment, descTerm.toString());
                    appendLine(comment, note.toString());
                    for (String t : tTerms) {
                        result.add(new GlossaryEntry(s, t, comment.toString(), priorityGlossary, origin));
                        addedForLang = true;
                    }
                    if (!addedForLang) { // An entry is created just to get the
                                         // definition
                        result.add(new GlossaryEntry(s, "", comment.toString(), priorityGlossary, origin));
                    }
                }
                return;
            }

            if (next == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals("note")) {
                    appendLine(note, readContent(reader, "note"));
                } else if (reader.getLocalName().equals("descripGrp")) {
                    appendProperty(descTerm, "", reader);
                } else if (reader.getLocalName().equals("descrip")) {
                    appendProperty(descTerm, "", reader);
                } else if (reader.getLocalName().equals("langSet")
                        || reader.getLocalName().equals("langSec")) {
                    String lang = reader.getAttributeValue(null, "lang");
                    lang = new Language(lang).getLanguageCode();
                    while (reader.hasNext()) {
                        next = reader.next();
                        if ((next == XMLStreamConstants.END_ELEMENT)
                                && ((reader.getLocalName().equals("langSet")
                                        || reader.getLocalName().equals("langSec")))) {
                            break;
                        }

                        if (next == XMLStreamConstants.START_ELEMENT) {
                            if (reader.getLocalName().equals("tig") || reader.getLocalName().equals("ntig")
                                    || reader.getLocalName().equals("termSec")) {
                                if (sLang.equalsIgnoreCase(lang)) {
                                    sTerms.add(readContent(reader, "term"));
                                } else if (tLang.equalsIgnoreCase(lang)) {
                                    tTerms.add(readContent(reader, "term"));
                                }
                            } else if (reader.getLocalName().equals("termNote")) {
                                if (sLang.equalsIgnoreCase(lang)) {
                                    appendProperty(descTerm, "", reader);
                                } else if (tLang.equalsIgnoreCase(lang)) {
                                    appendProperty(descTerm, "", reader);
                                }
                            } else if (reader.getLocalName().equals("descripGrp")) {
                                if (sLang.equalsIgnoreCase(lang)) {
                                    appendProperty(descTerm, "", reader);
                                } else if (tLang.equalsIgnoreCase(lang)) {
                                    appendProperty(descTerm, "", reader);
                                }
                            } else if (reader.getLocalName().equals("descrip")) {
                                if (sLang.equalsIgnoreCase(lang)) {
                                    appendProperty(descTerm, "", reader);
                                } else if (tLang.equalsIgnoreCase(lang)) {
                                    appendProperty(descTerm, "", reader);
                                }
                            } else if (reader.getLocalName().equals("admin")) {
                                if (sLang.equalsIgnoreCase(lang)) {
                                    appendProperty(descTerm, "", reader);
                                } else if (tLang.equalsIgnoreCase(lang)) {
                                    appendProperty(descTerm, "", reader);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void appendProperty(StringBuilder str, final String prefix, final XMLStreamReader reader)
            throws XMLStreamException {
        if (reader.getLocalName().equals("descrip")) {
            if ("context".equals(reader.getAttributeValue(null, "type"))) {
                if (Preferences.isPreferenceDefault(Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT, true)) {
                    appendLine(str, prefix + "context: " + readContent(reader, "descrip"));
                }
            } else {
                appendLine(str, prefix + reader.getAttributeValue(null, "type") + ": "
                        + readContent(reader, "descrip"));
            }
        }
        if (reader.getLocalName().equals("descripGrp")) {
            String prefix2 = "";
            if (reader.getAttributeValue(null, "id") != null) {
                prefix2 = reader.getAttributeValue(null, "id") + ".";
            }
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamConstants.END_ELEMENT) {
                    if (reader.getLocalName().equals("descripGrp")) {
                        break;
                    }
                }
                if (next == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals("descrip")) {
                        appendProperty(str, prefix2 + prefix, reader);
                    }
                }
            }
        }
        if (reader.getLocalName().equals("termNote")) {
            appendLine(str,
                    prefix + reader.getAttributeValue(null, "type") + ": " + readContent(reader, "termNote"));
        }
        if (reader.getLocalName().equals("admin")) {
            appendLine(str,
                    prefix + reader.getAttributeValue(null, "type") + ": " + readContent(reader, "admin"));
        }
    }

    private static void appendLine(final StringBuilder str, String line) {
        if (line.isEmpty()) { // No need to append empty lines
            return;
        }
        if (str.length() > 0) {
            str.append('\n');
        }
        str.append(line);
    }

    private static String readContent(final XMLStreamReader reader, String mark) throws XMLStreamException {
        StringBuilder res = new StringBuilder();
        boolean in = reader.getLocalName().equals(mark);
        int next;
        while (reader.hasNext()) {
            next = reader.next();
            if (next == XMLStreamConstants.END_ELEMENT) {
                if (reader.getLocalName().equals(mark)) {
                    break;
                } else if (reader.getLocalName().equals("hi")) {
                    if (in) {
                        res.append("* ");
                    }
                }
            }
            if (next == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals(mark)) {
                    in = true;
                } else if (reader.getLocalName().equals("hi")) {
                    if (in) {
                        res.append(" *");
                    }
                }
            }
            if (next == XMLStreamConstants.CHARACTERS) {
                if (in) {
                    res.append(reader.getText());
                }
            }
        }
        return res.toString();
    }
}
