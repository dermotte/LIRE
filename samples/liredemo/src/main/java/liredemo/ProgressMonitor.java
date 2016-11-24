package liredemo;

import javax.swing.*;

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
 * (c) 2002-2007 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ProgressMonitor {
    JProgressBar pbar;

    public ProgressMonitor(JProgressBar progressBar) {
        pbar = progressBar;
        pbar.setMinimum(0);
        pbar.setMaximum(100);
    }

    public int getProgress() {
        return pbar.getValue();
    }

    public void setProgress(int progress) {
        progress = Math.max(0, progress);
        progress = Math.min(100, progress);
        pbar.setString(Math.round(progress) + "% finished");
        pbar.setValue(progress);
    }
}
