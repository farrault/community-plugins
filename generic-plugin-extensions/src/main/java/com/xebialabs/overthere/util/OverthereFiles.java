/*
 * @(#)OverthereFiles.java     14 Sep 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.overthere.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;
import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

public class OverthereFiles {
    public static final Logger LOGGER = LoggerFactory.getLogger(OverthereFiles.class);
    
    public static void setFilePermissions(OverthereFile file, String permissions) {
        checkArgument(file.getConnection().getHostOperatingSystem().equals(UNIX), 
                "File permissions can only be set on UNIX files, but host OS is '%s'",
                file.getConnection().getHostOperatingSystem());
        LOGGER.debug("Setting file permission on {} to {}", file, permissions);
        
        CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
        int errno = file.getConnection().execute(multiHandler(loggingHandler(LOGGER), capturedOutput), 
                CmdLine.build("chmod", permissions, file.getPath()));
        if (errno != 0) {
                throw new RuntimeIOException(format("Cannot set execute permission on '%s' to %s: %s (errno=%s)",
                        file, permissions, capturedOutput.getError(), errno));
        }       
    }
}
