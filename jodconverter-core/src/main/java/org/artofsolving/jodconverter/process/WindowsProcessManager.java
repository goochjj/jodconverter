/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.artofsolving.jodconverter.process;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Windows specific office process manager that is capable of detecting and killing existing processes.
 * 
 * @author Sergiy Shyrkov
 */
public class WindowsProcessManager implements ProcessManager {

    /**
     * Output stream implementation that writes to a string buffer.
     * 
     * @author Sergiy Shyrkov
     */
    private static class StringOutputStream extends OutputStream {

        private StringWriter stringWriter = new StringWriter(256);

        public int getLength() {
            return getStringBuffer().length();
        }

        public StringBuffer getStringBuffer() {
            return stringWriter.getBuffer();
        }

        @Override
        public String toString() {
            return stringWriter.toString();
        }

        @Override
        public void write(int b) throws IOException {
            this.stringWriter.write(b);
        }
    }

    private static final Pattern LINE_PATTERN = Pattern.compile("^(.*?)\\s+(\\d+)\\s*$");

    private static final Logger logger = Logger.getLogger(WindowsProcessManager.class.getName());

    private static String execute(CommandLine cmd) throws ExecuteException, IOException {
        StringOutputStream out = new StringOutputStream();
        StringOutputStream err = new StringOutputStream();
        try {
            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(out, err));
            executor.execute(cmd, System.getenv());
        } finally {
            if (err.getLength() > 0) {
                logger.severe("Conversion process finished with error. Cause: " + err.toString());
            }
            if (logger.isLoggable(Level.FINE) && out.getLength() > 0) {
                logger.fine(out.toString());
            }
        }

        return out.toString();
    }

    public static boolean isSupported() {
        boolean supported = false;
        try {
            execute(new CommandLine("wmic").addArgument("quit"));
            execute(new CommandLine("taskkill").addArgument("/?"));
            supported = true;
        } catch (Exception e) {
            // commands not available
        }

        return supported;
    }

    public long findPid(ProcessQuery query) throws IOException {
        long pid = PID_UNKNOWN;
        Pattern commandPattern = Pattern.compile(".*" + query.getCommand() + ".*"
                + query.getArgument() + ".*");
        StringReader input = new StringReader(
                execute(new CommandLine("wmic").addArguments("process get CommandLine,ProcessId")));
        @SuppressWarnings("unchecked")
        List<String> lines = IOUtils.readLines(input);
        String targetLine = null;
        for (String line : lines) {
            if (commandPattern.matcher(line).matches()) {
                targetLine = line;
                break;
            }
        }
        if (targetLine != null) {
            Matcher matcher = LINE_PATTERN.matcher(targetLine);
            if (matcher.matches()) {
                pid = Long.parseLong(matcher.group(2));
                logger.info("Found existing office process with PID: " + pid);
            }
        }

        return pid;
    }

    public void kill(Process process, long pid) throws IOException {
        if (pid <= 0) {
            return;
        }
        logger.info("Killing existing office process with PID: " + pid);
        execute(new CommandLine("taskkill").addArguments("/t /f /pid " + pid));
    }
}
