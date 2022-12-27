/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2015 Aaron Madlon-kay
 *                2022 Hiroshi Miura
 *                Home page: http://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.omegat.util;

import static org.omegat.util.PreferencesImpl.IPrefsPersistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.io.FileUtils;

public class PreferencesXML implements IPrefsPersistence {

    private final File loadFile;
    private final File saveFile;

    public PreferencesXML(File loadFile, File saveFile) {
        this.loadFile = loadFile;
        this.saveFile = saveFile;
    }

    @Override
    public void load(final List<String> keys, final List<String> values) {
        if (loadFile != null) {
            try (InputStream is = Files.newInputStream(loadFile.toPath())) {
                loadXml(is, keys, values);
            } catch (IOException e) {
                Log.logErrorRB(e, "PM_ERROR_READING_FILE");
                makeBackup(loadFile);
            }
        } else {
            // If no prefs file is present, look inside JAR for defaults.
            try (InputStream is = getClass().getResourceAsStream(Preferences.FILE_PREFERENCES)) {
                if (is != null) {
                    loadXml(is, keys, values);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadXml(InputStream is, List<String> keys, List<String> values) throws IOException {
        OmegaT rootComponent;
        XmlMapper mapper = new XmlMapper();
        rootComponent = mapper.readValue(is, OmegaT.class);
        Preference preference = rootComponent.getPreference();
        if (preference != null) {
            preference.getRecords().forEach((key, value) -> {
                keys.add(key);
                values.add(value);
            });
        }
    }

    @Override
    public void save(final List<String> keys, final List<String> values) throws Exception {
        OmegaT rootComponent = new OmegaT();
        Preference preference = new Preference();
        preference.version = "1.0";
        preference.setRecords(new TreeMap<>());
        for (int i = 0; i < keys.size(); i++) {
            preference.setRecords(keys.get(i), values.get(i));
        }
        XmlMapper mapper = new XmlMapper();
        String xmlString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootComponent);
        try (BufferedWriter writer = Files.newBufferedWriter(saveFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write(xmlString);
        }
    }

    private static void makeBackup(File file) {
        if (file == null || !file.isFile()) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        File bakFile = new File(file.getAbsolutePath() + "." + timestamp + ".bak");
        try {
            FileUtils.copyFile(file, bakFile);
            Log.logWarningRB("PM_BACKED_UP_PREFS_FILE", bakFile.getAbsolutePath());
        } catch (IOException ex) {
            Log.logErrorRB(ex, "PM_ERROR_BACKING_UP_PREFS_FILE");
        }
    }

    @JacksonXmlRootElement(localName = "omegat")
    static class OmegaT {
        @JacksonXmlProperty(localName = "preference")
        private Preference preference;

        public Preference getPreference() {
            return preference;
        }

        public void setPreference(final Preference preference) {
            this.preference = preference;
        }
    }

    static class Preference {
        @JacksonXmlProperty(localName = "version", isAttribute = true)
        public String version;
        private Map<String, String> records;

        @JsonAnyGetter
        public Map<String, String> getRecords() {
            return records;
        }

        public void setRecords(final Map<String, String> records) {
            this.records = records;
        }

        @JsonAnySetter
        public void setRecords(final String key, final String value) {
            this.records.put(key, value);
        }
    }

}
