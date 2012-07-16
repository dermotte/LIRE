package edu.uniklu.itec.mosaix.engine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;

/*
* This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
*
* Caliph & Emir is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* Caliph & Emir is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Caliph & Emir; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
* Copyright statement:
* --------------------
* (c) 2002-2007 by Mathias Lux (mathias@juggle.at), Lukas Esterle & Manuel Warum.
* http://www.juggle.at, http://www.SemanticMetadata.net
*/
public final class Logging {
    //private final static Calendar cal = Calendar.getInstance();
    private final static boolean enabled = true;
    private static PrintStream logStream;

    public final static void log(Object caller, String mesg) {
        if (!enabled)
            return;

        if (logStream == null) {
            try {
                logStream = new PrintStream(new FileOutputStream("log.txt", false));
            } catch (IOException iox) {
                logStream = System.out;
                Logging.log(null, "Failed to setup file based logging. Relying on console instead.");
            }
        }

        logStream.print('[');
        logStream.print(tstamp());
        logStream.print(']');
        if (caller != null) {
            logStream.print('[');
            logStream.print(caller.getClass().getSimpleName());
            logStream.print("] ");
        } else {
            logStream.print("[?] ");
        }
        logStream.println(mesg);
    }

    private final static String tstamp() {
        Calendar cal = Calendar.getInstance();
        int hr, min, sec;
        hr = cal.get(Calendar.HOUR_OF_DAY);
        min = cal.get(Calendar.MINUTE);
        sec = cal.get(Calendar.SECOND);

        StringBuilder bldr = new StringBuilder();
        if (hr < 10)
            bldr.append('0');
        bldr.append(hr);
        bldr.append(':');
        if (min < 10)
            bldr.append('0');
        bldr.append(min);
        bldr.append(':');
        if (sec < 10)
            bldr.append('0');
        bldr.append(sec);
        return bldr.toString();
    }
}
