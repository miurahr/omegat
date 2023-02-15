/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2021,2023 Hiroshi Miura
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

package org.omegat.gui.preferences.view;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.omegat.core.Core;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.main.MainWindowUI;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class AppearanceController extends BasePreferencesController {

    private AppearancePreferencesPanel panel;
    private Map<String, String> themes;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_APPEARANCE");
    }

    private void initGui() {
        panel = new AppearancePreferencesPanel();
        themes = Arrays.asList(UIManager.getInstalledLookAndFeels()).stream()
                .collect(Collectors.toMap(LookAndFeelInfo::getName, LookAndFeelInfo::getClassName));
        panel.themeList.setListData(themes.keySet().toArray(new String[0]));
        panel.themeList.setVisibleRowCount(10);
        panel.themeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.themeList.addListSelectionListener(e -> {
            String selected = themes.get(panel.themeList.getSelectedValue());
            String current = UIManager.getLookAndFeel().getName();
            setRestartRequired(!selected.equals(current));
        });

        // TODO: Properly abstract the restore function
        panel.restoreWindowButton
                .addActionListener(e -> MainWindowUI.resetDesktopLayout((MainWindow) Core.getMainWindow()));
    }

    @Override
    protected void initFromPrefs() {
        String current = UIManager.getLookAndFeel().getName();
        panel.themeList.setSelectedValue(current, true);
    }

    @Override
    public void restoreDefaults() {
        String defaultClassName = Preferences.THEME_CLASS_NAME_DEFAULT;
        for (Map.Entry<String, String> entry : themes.entrySet()) {
            if (entry.getValue().equals(defaultClassName)) {
                panel.themeList.setSelectedValue(entry.getKey(), true);
            }
        }
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.THEME_CLASS_NAME,
                themes.get(panel.themeList.getSelectedValue()));
    }
}
