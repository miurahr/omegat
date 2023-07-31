/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
 *                Home page: https://www.omegat.org/
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.util.logging;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLReporter2;
import org.codehaus.stax2.validation.XMLValidationProblem;

import org.omegat.util.Log;

public class OmegaTXMLReporter implements XMLReporter2 {

    private int errorsCount;
    private int warningsCount;

    public OmegaTXMLReporter() {
        errorsCount = 0;
        warningsCount = 0;
    }

    public boolean isSuccess() {
        return errorsCount == 0 && warningsCount == 0;
    }

    public int getErrorsCount() {
        return errorsCount;
    }

    public int getWarningsCount() {
        return warningsCount;
    }

    public void report(String key, Object... parameters) throws XMLStreamException {
        Log.logWarningRB(key, parameters);
        warningsCount++;
    }

    @Override
    public void report(final XMLValidationProblem problem) throws XMLStreamException {
        int severity = problem.getSeverity();
        String type = problem.getType();
        Log.logWarningRB("TMXR_WARNING_WHILE_PARSING", problem.getLocation().getLineNumber(),
                problem.getLocation().getColumnNumber());
        Log.log(problem.getMessage());
        warningsCount++;
    }

    @Override
    public void report(final String message, final String errorType, final Object relatedInformation, final Location location) throws XMLStreamException {
        Log.logWarningRB("TMXR_WARNING_WHILE_PARSING", location.getLineNumber(),
                location.getColumnNumber());
        Log.log(message);
        warningsCount++;
    }
}
