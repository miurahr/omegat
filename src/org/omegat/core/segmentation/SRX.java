/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2017-2018 Thomas Cordonnier
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

package org.omegat.core.segmentation;

import java.beans.ExceptionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.omegat.util.Language;
import org.omegat.util.Log;

/**
 * The class with all the segmentation data possible -- rules, languages, etc.
 * It loads and saves its data from/to SRX file.
 *
 * @author Maxym Mykhalchuk
 */
public class SRX implements Serializable {

    private static final long serialVersionUID = 2182125877925944613L;
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    public static final String CONF_SENTSEG = "segmentation.conf";

    /**
     * Initializes SRX rules to defaults.
     */
    private void init() {
        this.mappingRules = new ArrayList<>();
        this.includeEndingTags = true;
        this.segmentSubflows = true;
        initDefaults();
    }

    /**
     * Creates an empty SRX, without any rules.
     * <p>
     * Please do not call directly unless you know what you are doing.
     */
    public SRX() {
    }

    public SRX copy() {
        SRX result = new SRX();
        result.mappingRules = new ArrayList<>(mappingRules.size());
        for (MapRule rule : mappingRules) {
            result.mappingRules.add(rule.copy());
        }
        return result;
    }

    /**
     * Saves segmentation rules into specified file.
     */
    public static void saveTo(SRX srx, File outFile) throws IOException {
        if (srx == null) {
            boolean ignored = outFile.delete();
            return;
        }
        saveToSrx(srx, outFile);
    }

    private static void saveToSrx(SRX srx, File outFile) throws IOException {
        try (OutputStreamWriter fos = new OutputStreamWriter(Files.newOutputStream(outFile.toPath()),
                StandardCharsets.UTF_8)) {
            XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(fos);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("srx");
            writer.writeDefaultNamespace("http://www.lisa.org/srx20");
            writer.writeAttribute("version", "2.0");

            writer.writeCharacters("\n    ");
            writer.writeEmptyElement("header");
            writer.writeAttribute("segmentsubflows", srx.segmentSubflows ? "yes" : "no");

            writer.writeCharacters("\n    ");
            writer.writeStartElement("body");
            writer.writeCharacters("\n        ");
            writer.writeStartElement("languagerules");
            for (MapRule mr : srx.getMappingRules()) {
                writer.writeCharacters("\n            ");
                writer.writeStartElement("languagerule");
                writer.writeAttribute("languagerulename", mr.getLanguage());
                for (Rule rule : mr.getRules()) {
                    writer.writeCharacters("\n                ");
                    writer.writeStartElement("rule");
                    writer.writeAttribute("break", rule.isBreakRule() ? "yes" : "no");
                    writer.writeCharacters("\n                    ");
                    writer.writeStartElement("beforebreak");
                    writer.writeCharacters(rule.getBeforebreak());
                    writer.writeEndElement();
                    writer.writeCharacters("\n                    ");
                    writer.writeStartElement("afterbreak");
                    writer.writeCharacters(rule.getAfterbreak());
                    writer.writeEndElement();
                    writer.writeCharacters("\n                ");
                    writer.writeEndElement(/* "rule" */);
                }
                writer.writeCharacters("\n            ");
                writer.writeEndElement(/* "languagerule" */);
            }
            writer.writeCharacters("\n        ");
            writer.writeEndElement(/* "languagerules" */);
            writer.writeCharacters("\n        ");
            writer.writeStartElement("maprules");
            for (MapRule mr : srx.getMappingRules()) {
                writer.writeCharacters("\n            ");
                writer.writeEmptyElement("languagemap");
                writer.writeAttribute("languagerulename", mr.getLanguage());
                writer.writeAttribute("languagepattern", mr.getPattern());
            }
            writer.writeCharacters("\n        ");
            writer.writeEndElement(/* "maprules" */);
            writer.writeCharacters("\n    ");
            writer.writeEndElement(/* "body" */);
            writer.writeCharacters("\n");
            writer.writeEndElement(/* "srx" */);
            writer.close();

        } catch (Exception e) {
            Log.logErrorRB("CORE_SRX_ERROR_SAVING_SEGMENTATION_CONFIG");
            throw new IOException(e);
        }
    }

    /**
     * Loads segmentation rules from an XML file. If there's an error loading a
     * file, it calls <code>initDefaults</code>.
     * <p>
     * Since 1.6.0 RC8 it also checks if the version of segmentation rules saved
     * is older than that of the current OmegaT, and tries to merge the two sets
     * of rules.
     */
    public static SRX loadSRX(File configFile) {
        if (!configFile.exists()) {
            return null;
        }
        try {
            SRX res = new SRX();
            loadSrx(res, configFile.toURI().toURL());
            return res;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static void loadSrx(SRX res, URL rulesUrl) {
        List<MapRule> newMap = new ArrayList<>();
        res.setMappingRules(newMap);
        try (InputStream io = rulesUrl.openStream()) {
            Log.log("using segmentation rules from " + rulesUrl);

            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(io);
            List<Rule> rulesList = null;
            HashMap<String, List<Rule>> mapping = new HashMap<>();
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamReader.START_ELEMENT) {
                    if (reader.getName().getLocalPart().equals("header")) {
                        res.setSegmentSubflows(
                                !"no".equals(reader.getAttributeValue(null, "segmentsubflows")));
                    } else if (reader.getName().getLocalPart().equals("languagerule")) {
                        rulesList = new ArrayList<Rule>();
                        mapping.put(reader.getAttributeValue(null, "languagerulename"), rulesList);
                    } else if (reader.getName().getLocalPart().equals("rule")) {
                        boolean isBreak = !("no".equals(reader.getAttributeValue(null, "break")));
                        StringBuilder before = new StringBuilder(), after = new StringBuilder();
                        byte pos = 0;
                        while (reader.hasNext()) {
                            int next = reader.next();
                            if (next == XMLStreamReader.START_ELEMENT) {
                                if (reader.getName().getLocalPart().equals("beforebreak")) {
                                    pos = -1;
                                } else if (reader.getName().getLocalPart().equals("afterbreak")) {
                                    pos = +1;
                                } else {
                                    pos = 0;
                                }
                            } else if (next == XMLStreamReader.END_ELEMENT) {
                                if (reader.getName().getLocalPart().equals("beforebreak")) {
                                    pos = 0;
                                } else if (reader.getName().getLocalPart().equals("afterbreak")) {
                                    pos = 0;
                                } else if (reader.getName().getLocalPart().equals("rule")) {
                                    break;
                                }
                            } else if (reader.hasText()) {
                                if (pos == -1) {
                                    before.append(reader.getText());
                                }
                                if (pos == +1) {
                                    after.append(reader.getText());
                                }
                            }
                        }
                        rulesList.add(new Rule(isBreak, before.toString(), after.toString()));
                    } else if (reader.getName().getLocalPart().equals("languagemap")) {
                        newMap.add(new MapRule(reader.getAttributeValue(null, "languagerulename"),
                                reader.getAttributeValue(null, "languagepattern"),
                                mapping.get(reader.getAttributeValue(null, "languagerulename"))));
                    }
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    /**
     * Does a config file already exists for the project at the given location?
     * 
     * @param configDir
     *            the project directory for storage of settings file
     */
    public static boolean projectConfigFileExists(String configDir) {
        File configFile = new File(configDir + CONF_SENTSEG);
        return configFile.exists();
    }

    /** Merges two sets of segmentation rules together. */
    private static SRX merge(SRX current, SRX defaults) {
        current = upgrade(current, defaults);

        int defaultMapRulesN = defaults.getMappingRules().size();
        for (int i = 0; i < defaultMapRulesN; i++) {
            MapRule dmaprule = defaults.getMappingRules().get(i);
            String dcode = dmaprule.getLanguageCode();
            // trying to find
            boolean found = false;
            int currentMapRulesN = current.getMappingRules().size();
            MapRule cmaprule = null;
            for (int j = 0; j < currentMapRulesN; j++) {
                cmaprule = current.getMappingRules().get(j);
                String ccode = cmaprule.getLanguageCode();
                if (dcode.equals(ccode)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                // merging -- adding those rules not there in current list
                List<Rule> crules = cmaprule.getRules();
                List<Rule> drules = dmaprule.getRules();
                for (Rule drule : drules) {
                    if (!crules.contains(drule)) {
                        if (drule.isBreakRule()) {
                            // breaks go to the end
                            crules.add(drule);
                        } else {
                            // exceptions go before the first break rule
                            int currentRulesN = crules.size();
                            int firstBreakRuleN = currentRulesN;
                            for (int k = 0; k < currentRulesN; k++) {
                                Rule crule = crules.get(k);
                                if (crule.isBreakRule()) {
                                    firstBreakRuleN = k;
                                    break;
                                }
                            }
                            crules.add(firstBreakRuleN, drule);
                        }
                    }
                }
            } else {
                // just adding before the default rules
                int englishN = currentMapRulesN;
                for (int j = 0; j < currentMapRulesN; j++) {
                    cmaprule = current.getMappingRules().get(j);
                    String cpattern = cmaprule.getPattern();
                    if (DEFAULT_RULES_PATTERN.equals(cpattern)) {
                        englishN = j;
                        break;
                    }
                }
                current.getMappingRules().add(englishN, dmaprule);
            }
        }
        return current;
    }

    /** Implements some upgrade heuristics. */
    private static SRX upgrade(SRX current, SRX defaults) {
        // renaming "Default (English)" to "Default"
        // and removing English/Text/HTML-specific rules from there
        if (OT160RC9_VERSION.equals(CURRENT_VERSION)) {
            String def = "Default (English)";
            for (int i = 0; i < current.getMappingRules().size(); i++) {
                MapRule maprule = current.getMappingRules().get(i);
                if (def.equals(maprule.getLanguageCode())) {
                    maprule.setLanguage(LanguageCodes.DEFAULT_CODE);
                    maprule.getRules().removeAll(Objects
                            .requireNonNull(getRulesForLanguage(defaults, LanguageCodes.ENGLISH_CODE)));
                    maprule.getRules().removeAll(
                            Objects.requireNonNull(getRulesForLanguage(defaults, LanguageCodes.F_TEXT_CODE)));
                    maprule.getRules().removeAll(
                            Objects.requireNonNull(getRulesForLanguage(defaults, LanguageCodes.F_HTML_CODE)));
                }
            }
        }
        return current;
    }

    /**
     * Find rules for specific language.
     *
     * @param source
     *            rules list
     * @param langName
     *            language name
     * @return list of rules
     */
    private static List<Rule> getRulesForLanguage(final SRX source, String langName) {
        for (MapRule mr : source.getMappingRules()) {
            if (langName.equals(mr.getLanguageCode())) {
                return mr.getRules();
            }
        }
        return null;
    }

    /**
     * My Own Class to listen to exceptions, occured while loading filters
     * configuration.
     */
    static class MyExceptionListener implements ExceptionListener {
        private final List<Exception> exceptionsList = new ArrayList<>();
        private boolean exceptionOccurred = false;

        public void exceptionThrown(Exception e) {
            exceptionOccurred = true;
            exceptionsList.add(e);
        }

        /**
         * Returns whether any exceptions occurred.
         */
        public boolean isExceptionOccurred() {
            return exceptionOccurred;
        }

        /**
         * Returns the list of occurred exceptions.
         */
        public List<Exception> getExceptionsList() {
            return exceptionsList;
        }
    }

    // Patterns
    private static final String DEFAULT_RULES_PATTERN = ".*";

    /**
     * Initializes default rules.
     */
    private void initDefaults() {
        try {
            URL rulesUrl = this.getClass().getResource("defaultRules.srx");
            loadSrx(this, rulesUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static SRX getDefault() {
        SRX srx = new SRX();
        srx.init();
        return srx;
    }

    /**
     * Finds the rules for a certain language.
     * <p>
     * Usually (if the user didn't screw up the setup) there're a default
     * segmentation rules, so it's a good idea to rely on this method always
     * returning at least some rules.
     * <p>
     * Or in case of a completely screwed setup -- an empty list without any
     * rules.
     */
    public List<Rule> lookupRulesForLanguage(Language srclang) {
        List<Rule> rules = new ArrayList<>();
        for (int i = 0; i < getMappingRules().size(); i++) {
            MapRule maprule = getMappingRules().get(i);
            if (maprule.getCompiledPattern().matcher(srclang.getLanguage()).matches()) {
                rules.addAll(maprule.getRules());
            }
        }
        return rules;
    }

    /**
     * Holds value of property segmentSubflows.
     */
    private boolean segmentSubflows = true;

    /**
     * Getter for property segmentSubflows.
     *
     * @return Value of property segmentSubflows.
     */
    public boolean isSegmentSubflows() {

        return this.segmentSubflows;
    }

    /**
     * Setter for property segmentSubflows.
     *
     * @param segmentSubflows
     *            New value of property segmentSubflows.
     */
    public void setSegmentSubflows(boolean segmentSubflows) {

        this.segmentSubflows = segmentSubflows;
    }

    /**
     * Holds value of property includeStartingTags.
     */
    private boolean includeStartingTags;

    /**
     * Getter for property includeStartingTags.
     *
     * @return Value of property includeStartingTags.
     */
    public boolean isIncludeStartingTags() {

        return this.includeStartingTags;
    }

    /**
     * Setter for property includeStartingTags.
     *
     * @param includeStartingTags
     *            New value of property includeStartingTags.
     */
    public void setIncludeStartingTags(boolean includeStartingTags) {
        this.includeStartingTags = includeStartingTags;
    }

    /**
     * Holds value of property includeEndingTags.
     */
    private boolean includeEndingTags = true;

    /**
     * Getter for property includeEndingTags.
     *
     * @return Value of property includeEndingTags.
     */
    public boolean isIncludeEndingTags() {
        return this.includeEndingTags;
    }

    /**
     * Setter for property includeEndingTags.
     *
     * @param includeEndingTags
     *            New value of property includeEndingTags.
     */
    public void setIncludeEndingTags(boolean includeEndingTags) {
        this.includeEndingTags = includeEndingTags;
    }

    /**
     * Holds value of property includeIsolatedTags.
     */
    private boolean includeIsolatedTags;

    /**
     * Getter for property includeIsolatedTags.
     *
     * @return Value of property includeIsolatedTags.
     */
    public boolean isIncludeIsolatedTags() {

        return this.includeIsolatedTags;
    }

    /**
     * Setter for property includeIsolatedTags.
     *
     * @param includeIsolatedTags
     *            New value of property includeIsolatedTags.
     */
    public void setIncludeIsolatedTags(boolean includeIsolatedTags) {

        this.includeIsolatedTags = includeIsolatedTags;
    }

    /**
     * Correspondences between languages and their segmentation rules. Each
     * element is of class {@link MapRule}.
     */
    private List<MapRule> mappingRules = new ArrayList<>();

    /**
     * Returns all mapping rules (of class {@link MapRule}) at once:
     * correspondences between languages and their segmentation rules.
     */
    public List<MapRule> getMappingRules() {
        return mappingRules;
    }

    /**
     * Sets all mapping rules (of class {@link MapRule}) at once:
     * correspondences between languages and their segmentation rules.
     */
    public void setMappingRules(List<MapRule> rules) {
        mappingRules = rules;
    }

    // ////////////////////////////////////////////////////////////////
    // Versioning properties to detect version upgrades
    // and possibly do something if required

    /** Initial version of segmentation support (1.4.6 beta 4 -- 1.6.0 RC7). */
    public static final String INITIAL_VERSION = "0.2";
    /** Segmentation support of 1.6.0 RC8 (a bit more rules added). */
    public static final String OT160RC8_VERSION = "0.2.1";
    /** Segmentation support of 1.6.0 RC9 (rules separated). */
    public static final String OT160RC9_VERSION = "0.2.2";
    /** Currently supported segmentation support version. */
    public static final String CURRENT_VERSION = OT160RC9_VERSION;

    /** Version of OmegaT segmentation support. */
    private String version;

    /** Returns segmentation support version. */
    public String getVersion() {
        return version;
    }

    /** Sets segmentation support version. */
    public void setVersion(String value) {
        version = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (includeEndingTags ? 1231 : 1237);
        result = prime * result + (includeIsolatedTags ? 1231 : 1237);
        result = prime * result + (includeStartingTags ? 1231 : 1237);
        result = prime * result + ((mappingRules == null) ? 0 : mappingRules.hashCode());
        result = prime * result + (segmentSubflows ? 1231 : 1237);
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
        SRX other = (SRX) obj;
        if (includeEndingTags != other.includeEndingTags) {
            return false;
        }
        if (includeIsolatedTags != other.includeIsolatedTags) {
            return false;
        }
        if (includeStartingTags != other.includeStartingTags) {
            return false;
        }
        if (mappingRules == null) {
            if (other.mappingRules != null) {
                return false;
            }
        } else if (!mappingRules.equals(other.mappingRules)) {
            return false;
        }
        if (segmentSubflows != other.segmentSubflows) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }
}
