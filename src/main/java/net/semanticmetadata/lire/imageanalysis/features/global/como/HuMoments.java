/*
 * This file is part of the LIRE project: http://lire-project.net
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2017 by Savvas Chatzichristofis (savvash@gmail.com) & Nektarios Anagnostopoulos (nek.anag@gmail.com)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.imageanalysis.features.global.como;

/**
 * Part of the COMO global feature
 *
 * @author Savvas Chatzichristofis, savvash@gmail.com
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class HuMoments {

    public static double[][] HuMomentsTable = {
            {0.0012801877159444332, 1.2811997974150548E-8, 2.931517692794886E-11, 2.3361585476863737E-11, 1.0229383520567805E-21, -4.741043401863473E-16, 3.489450463009515E-23},
            {0.030098519274221386, 9.553200793114547E-6, 2.7312098265223947E-7, 1.5846418079328335E-6, -2.6894242032236695E-12, 5.431901588135399E-9, -1.6868751727422065E-12},
            {0.0018139530164278453, 5.941872634495257E-8, 1.4200444288697612E-10, 1.5023611260922216E-10, 1.3373151439243982E-20, 1.1760743231129731E-14, 1.3487898261349881E-20},
            {0.004440612110292505, 6.320931310602498E-7, 5.4004430112564E-9, 1.0423679617819742E-8, -2.7085204326667915E-16, 1.7090726156377653E-11, 9.51706439192856E-16},
            {9.639630464555355E-4, 7.859161865855281E-9, 1.1230404627214495E-11, 8.291087328844257E-12, 1.372616300158933E-22, -4.3513261260527616E-16, -3.862637133906026E-24},
            {0.10135058748498203, 4.290771163627242E-5, 6.178005148446455E-6, 1.9548008476574724E-5, 4.4473620282147234E-11, 1.6752667650020017E-7, 2.758163163459048E-11}
    };


    public static double[][] getHuMomentsTable() {
        return HuMomentsTable;
    }

    public static void setHuMomentsTable(double[][] huMomentsTable) {
        HuMoments.HuMomentsTable = huMomentsTable;
    }
}
