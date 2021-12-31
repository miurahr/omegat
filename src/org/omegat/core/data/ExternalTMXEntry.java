/*
 * *************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2021 Hiroshi Miura.
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
 *  *************************************************************************
 *
 */

package org.omegat.core.data;

import java.util.Collections;
import java.util.List;

import org.omegat.util.TMXProp;

public class ExternalTMXEntry extends TMXEntry {

    private final List<TMXProp> otherProperties;

    ExternalTMXEntry(PrepareTMXEntry from, boolean defaultTranslation, ExternalLinked linked,
                     final List<TMXProp> props) {
        super(from, defaultTranslation, linked);
        this.otherProperties = props;
    }

    /**
     * Property existence.
     * @return true when entry has property, otherwise false.
     */
    public boolean hasPorperties() {
        return otherProperties != null && otherProperties.size() > 0;
    }

    /**
     * Return propertySet..
     * @return list of TMXProp.
     */
    public List<TMXProp> propertySet() {
        return Collections.unmodifiableList(otherProperties);
    }

    /**
     * Check entry have specified type of property, and that is specified value.
     * @param propType property type to check.
     * @param propValue value to check equality.
     * @return true when entry have specified type of propety that is as same as specified value.
     */
    public boolean hasPropValue(String propType, String propValue) {
        if (otherProperties == null) {
            return false;
        }
        for (int i = 0; i < otherProperties.size(); i++) {
            TMXProp kv = otherProperties.get(i);
            if (propType.equals(kv.getType())) {
                if (propValue == null) {
                    return true;
                }
                if (propValue.equals(kv.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

}
