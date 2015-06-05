/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.imageanalysis.mser.fourier;

import net.semanticmetadata.lire.imageanalysis.mser.fourier.utils.Complex;
import net.semanticmetadata.lire.imageanalysis.mser.fourier.utils.PolygonUtils;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: Shotty
 * Date: 10.01.11
 * Time: 05:00
 * To change this template use File | Settings | File Templates.
 */
public class Fourier {

    double[] c, d;
    Complex[] a, b;
    Complex[] Q;
    double[] t;
    Point2D.Double points[];
    int NPT;

    public Fourier(Point2D.Double[] points) {
        this.points = points;
        this.NPT = points.length - 1;
        this.t = calcParameter(points, 2);
    }

    /**
     * Description of the Method
     *
     * @param kmax Description of the Parameter
     */
    public void computeFourier(int kmax) {
        a = new Complex[kmax + 1];
        b = new Complex[kmax + 1];

        Complex AP, AM, BP, BM, E0P, E0M, E1P, E1M;
        Complex DX, DT;

        // Perimeter
        double PER = (t[NPT] - t[0]);

        // System.out.println("PER:" + PER);

        double factor;
        for (int k = 0; k <= kmax; k++) {
            // calculate Fourier coefficient 0
            if (k == 0) {
                double sumX = 0.0;
                double sumY = 0.0;
                for (int i = 0; i < NPT; i++) {
                    factor = (t[(i + 1)] - t[i]);
                    // sum (xi+1 + xi)(ti+1-ti)
                    sumX = sumX + ((points[(i + 1)].x + points[i].x) * factor);
                    sumY = sumY + ((points[(i + 1)].y + points[i].y) * factor);
                }
                // sumX / 2*(tn - t0)
                a[k] = new Complex(sumX / (2. * PER), sumY / (2. * PER));
                b[k] = new Complex(0, 0);
            } else {
                // double PI
                double ZP = 6.28318530718;
                double A0 = PER / (ZP * ZP);
                double B0 = 1. / (ZP);
                double P0 = ZP / PER;

                double XK = 1. * k;
                double PK = P0 * XK;
                double PH = PK * t[0];
                E1P = new Complex(Math.cos(PH), -Math.sin(PH));
                E1M = E1P.conjugate();
                AP = new Complex(0, 0);
                BP = new Complex(0, 0);
                AM = new Complex(0, 0);
                BM = new Complex(0, 0);

                for (int i = 0; i < NPT; i++) {
                    DX = new Complex(points[(i + 1) % NPT].x - points[i].x, points[(i + 1) % NPT].y - points[i].y);
                    // DT = t[(i+1)] - t[i];     // DT == ????
                    DT = new Complex(t[(i + 1)] - t[i], 0);     // DT == ????

                    if (DT.re() == 0) {
                        BP = BP.plus(DX.times(E1P));
                        BM = BM.plus(DX.times(E1M));
                    } else {
                        PH = PK * t[i + 1];
                        E0P = E1P;
                        E0M = E1M;
                        E1P = new Complex(Math.cos(PH), -Math.sin(PH));
                        E1M = E1P.conjugate();
                        DX = DX.divides(DT);
                        AP = AP.plus(DX.times(E1P.minus(E0P)));
                        AM = AM.plus(DX.times(E1M.minus(E0M)));
                    }

                    a[k] = AP.times(A0 / (XK * XK)).minus(new Complex(0, B0 / XK).times(BP));
                    b[k] = AM.times(A0 / (XK * XK)).plus(new Complex(0, B0 / XK).times(BM));
                }
            }
        }
    }

    /**
     * @param polygon
     * @param type    1: Bogenlaenge; 2: Abs. Flaeche; 3: Flaeche
     * @return
     */
    public static double[] calcParameter(Point2D.Double[] polygon, int type) {
        int N = polygon.length;
        double[] t = new double[N];
        t[0] = 0;

        switch (type) {
            case 1:
                Complex x = new Complex(polygon[0].x, polygon[0].y);
                Complex x1;
                Complex dx;
                double d;
                for (int i = 0; i < N - 1; i++) {
                    x1 = new Complex(polygon[i + 1].x, polygon[i + 1].y);
                    dx = x1.minus(x);
                    d = Math.sqrt(dx.times(dx.conjugate()).re());
                    t[i + 1] = t[i] + d;
                    x = x1;
                }
                break;
            case 2:
            case 3:
                int j;
                double factor = 0;
                for (int i = 1; i < N; i++) {
                    j = (i - 1);
                    factor = (polygon[j].x * polygon[i].y - polygon[i].x * polygon[j].y);
                    factor = factor > 0 ? factor : 0 - factor;
                    // absolut-wert (darf nicht negativ werden)
                    if (type == 2) {
                        t[i] = t[i - 1] + Math.abs((factor / 2));
                    } else {
                        t[i] = t[i - 1] + (factor / 2);
                    }
                }
        }

        return t;
    }

    public static void main(String[] args) {

/*
        Point2D.Double[] test =
        {
            new Point2D.Double(-1, -1),
            new Point2D.Double(-4, 2),
            new Point2D.Double(-1, 5),
            new Point2D.Double(2, 2),
            new Point2D.Double(-1, -1)  // X(N) == X(0))
        };

        testFourier(test, 5);

        Point2D.Double[] test2 =
        {
            new Point2D.Double(1, 2),
            new Point2D.Double(4, 2),
            new Point2D.Double(5, 3),
            new Point2D.Double(2, 3),
            new Point2D.Double(1, 2)  // X(N) == X(0))
        };

        testFourier(test2, 5);
*/
        // gerades F
        Point2D.Double[] EF =
                {
                        new Point2D.Double(0 / 2., 0 / 2.),
                        new Point2D.Double(2 / 2., 0 / 2.),
                        new Point2D.Double(2 / 2., 5 / 2.),
                        new Point2D.Double(5 / 2., 5 / 2.),
                        new Point2D.Double(5 / 2., 7 / 2.),
                        new Point2D.Double(2 / 2., 7 / 2.),
                        new Point2D.Double(2 / 2., 9 / 2.),
                        new Point2D.Double(7 / 2., 9 / 2.),
                        new Point2D.Double(7 / 2., 11 / 2.),
                        new Point2D.Double(0 / 2., 11 / 2.),
                        new Point2D.Double(0 / 2., 0 / 2.) // X(N) == X(0))
                };

        testFourier(EF, 5);

        // falsches F
        Point2D.Double[] EF2 =
                {
                        new Point2D.Double(0 / 2., 0 / 2.),
                        new Point2D.Double(2 / 2., 0 / 2.),
                        new Point2D.Double(2 / 2., 5 / 2.),
                        new Point2D.Double(5 / 2., 5 / 2.),
                        new Point2D.Double(5 / 2., 7 / 2.),
                        new Point2D.Double(2 / 2., 7 / 2.),
                        new Point2D.Double(2 / 2., 9 / 2.),
                        new Point2D.Double(7 / 2., 9 / 2.),
                        new Point2D.Double(5 / 2., 11 / 2.),
                        new Point2D.Double(0 / 2., 11 / 2.),
                        new Point2D.Double(0 / 2., 0 / 2.) // X(N) == X(0))
                };

        testFourier(EF2, 5);

        // schiefes F
        EF = new Point2D.Double[]
                {
                        new Point2D.Double(0 / 2., 0 / 2.),
                        new Point2D.Double(2 / 2., 0 / 2.),
                        new Point2D.Double(2 / 2., 5 / 2.),
                        new Point2D.Double(5 / 2., 5 / 2.),
                        new Point2D.Double(5 / 2., 7 / 2.),
                        new Point2D.Double(2 / 2., 7 / 2.),
                        new Point2D.Double(2 / 2., 9 / 2.),
                        new Point2D.Double(7 / 2., 9 / 2.),
                        new Point2D.Double(7 / 2., 11 / 2.),
                        new Point2D.Double(0 / 2., 11 / 2.),
                        new Point2D.Double(0 / 2., 0 / 2.) // X(N) == X(0))
                };

        double[][] A =
                {{3., 1.}, {2., 3.}
//                        new Point2D.Double(3.,1.),
//                        new Point2D.Double(2.,3.)
                };
        // create new Points with MatrixMultiplication

        Point2D.Double[] EF3 = matMult(A, EF);

        testFourier(EF3, 5);

        // schiefes F anderer Aufpunkt
        EF = new Point2D.Double[]
                {
                        new Point2D.Double(2 / 2., 5 / 2.),
                        new Point2D.Double(5 / 2., 5 / 2.),
                        new Point2D.Double(5 / 2., 7 / 2.),
                        new Point2D.Double(2 / 2., 7 / 2.),
                        new Point2D.Double(2 / 2., 9 / 2.),
                        new Point2D.Double(7 / 2., 9 / 2.),
                        new Point2D.Double(7 / 2., 11 / 2.),
                        new Point2D.Double(0 / 2., 11 / 2.),
                        new Point2D.Double(0 / 2., 0 / 2.),
                        new Point2D.Double(2 / 2., 0 / 2.),
                        new Point2D.Double(2 / 2., 5 / 2.) // X(N) == X(0))
                };

        EF3 = matMult(A, EF);

        testFourier(EF3, 5);


    }

    /*!
    * Multiply a point by a transformation matrix.
    *
    * Applies the given transformation matrix to the given point.  With some
    * transformation matrices, a vector may also be transformed.
    *
    * \param c Result. (just a float[3])
    * \param m Transformation matrix. (just a float[4][4])
    * \param a Input point. (just a float[3])
    */
    protected static Point2D.Double[] matMult(double[][] A, Point2D.Double[] points) {
        double a11 = A[0][0];
        double a12 = A[1][0];
        double a21 = A[0][1];
        double a22 = A[1][1];

        Point2D.Double[] result = new Point2D.Double[points.length];

        for (int i = 0; i < result.length; i++) {
            result[i] =
                    new Point2D.Double(
                            a11 * points[i].x + a12 * points[i].y,
                            a21 * points[i].x + a22 * points[i].y
                    );


        }
        return result;
    }

    protected static void testFourier(Point2D.Double[] poly, int k) {
        // System.out.println();
        // System.out.println("NEW");
        // System.out.println();
        printPoints(poly);
        Point2D.Double cog = PolygonUtils.polygonCenterOfMass(poly);
        // System.out.println("COG before:" + cog.getX() + "/" + cog.getY());
        PolygonUtils.applyCoG(poly, cog);
        printPoints(poly);
        cog = PolygonUtils.polygonCenterOfMass(poly);
        // System.out.println("COG after:" + cog.getX() + "/" + cog.getY());
        // calc T
        double[] t = calcParameter(poly, 2);
        printParametrisierung(t);
        Fourier f = new Fourier(poly);
        f.computeFourier(k);
        f.printCoefficients();
        f.createInvariants2(1);
        f.printInvariants();
    }

    public static void printPoints(Point2D.Double[] points) {
        // System.out.print("(");

        for (int i = 0; i < points.length; i++) {
            // System.out.print(" " + points[i].x);
        }

        // System.out.println(")");
        // System.out.print("(");
        for (int i = 0; i < points.length; i++) {
            // System.out.print(" " + points[i].y);
        }

        // System.out.println(")");


    }

    public static void printParametrisierung(double[] t) {
        // System.out.print("Parametrisierung: ");
        for (int i = 0; i < t.length; i++) {
            // System.out.print(" " + t[i]);
        }
        // System.out.println(" ");
    }

    public void printCoefficients() {
        // System.out.println("COEFFICIENTS: ");

        for (int i = b.length - 1; i > 0; i--) {
            // System.out.println("-" + i + ":" + b[i].toString());
        }

        for (int i = 0; i < a.length; i++) {
            // System.out.println(" " + i + ":" + a[i].toString());
        }
    }

    /**
     * @param s rotation symmetry (1 if not symmetrical at all)
     *          <p/>
     *          Formel auf Seite 20 ME-08-01.pdf , xTilde
     */
    public void createInvariants2(int s) {
        int q = 1;
        int l = a.length + b.length - 1;

        a[0] = new Complex(0, 0);
        b[0] = new Complex(0, 0);

        Complex[] fd = new Complex[a.length + b.length - 1];

        createInvariants(s);

        c = new double[a.length];
        d = new double[a.length];

        for (int i = 0; i < a.length; i++) {
            c[i] = a[i].abs() / a[q].abs();
            d[i] = b[i].abs() / a[q].abs();
        }

        int n = a.length - 1;

        int[] N = new int[l];
        for (int i = 0; i < N.length; i++) {
            N[i] = i - n;
            // System.out.println("N[" + i + "]=" + N[i]);
        }

        double[] phi = new double[l];
        int counter = 0;
        for (int i = b.length - 1; i > 0; i--) {
            phi[counter++] = getArg(b[i]);
        }
        phi[counter++] = 0;
        for (int i = 1; i < a.length; i++) {
            phi[counter++] = getArg(a[i]);
        }
        // System.out.println("PHI");
        for (int i = 0; i < phi.length; i++) {
            // System.out.println(phi[i]);
        }

        for (int i = 0; i < phi.length; i++) {
            phi[i] = phi[i] + ((q - N[i]) / s) * getArg(a[q + s]);
        }

        // System.out.println("PHI NEU");
        for (int i = 0; i < phi.length; i++) {
            // System.out.println(phi[i]);
        }

        for (int i = 0; i < phi.length; i++) {
            phi[i] = phi[i] - ((q + s - N[i]) / s) * getArg(a[q]);
        }

        // System.out.println("PHI NEU2");
        for (int i = 0; i < phi.length; i++) {
            // System.out.println(phi[i]);
        }

        for (int i = 0; i < c.length; i++) {
            fd[i] = new Complex(d[d.length - i - 1], 0).times(new Complex(0., phi[i]).exp());
            fd[n + i] = new Complex(c[i], 0).times(new Complex(0., phi[n + i]).exp());
        }

        // System.out.println("INVARIANTS");
        for (int i = 0; i < fd.length; i++) {
            // System.out.println(fd[i].toString());
        }

        for (int i = 0; i < c.length; i++) {
            d[d.length - i - 1] = fd[i].abs();
            c[i] = fd[n + i].abs();
        }

    }

    /**
     * @param s rotation symmetry (1 if not symmetrical at all)
     */
    public void createInvariants(int s) {
        Complex Det = new Complex(0, 0);
        Complex UpStar = new Complex(0, 0);
        Complex VpStar = new Complex(0, 0);
        int p = 0;
        // look for index of the coefficient to norm onto
        for (int i = 1; i < a.length; i++) {
            Complex curUp = a[i].plus(b[i].conjugate()).divides(new Complex(2, 0));
            Complex curUpStar = curUp.conjugate();
            Complex curVp = a[i].minus(b[i].conjugate()).divides(new Complex(0, 2));
            Complex curVpStar = curVp.conjugate();

            // (U(p)V(p)*-V(p)U(p)*)
            Complex curDet = curUp.times(curVpStar).minus(curVp.times(curUpStar));
            // System.out.println(i + ": det=" + curDet.abs());

            // (U(p)V(p)*-V(p)U(p)*) must be biggest value
            if (curDet.abs() > Det.abs()) {
                p = i;
                UpStar = curUpStar;
                VpStar = curVpStar;
                Det = curDet;
            }
        }

        // This actually should always be 1 ...
        // since the first coefficient is the biggest ellipse
        // System.out.println("p=" + p);
//        Assert.assertTrue("p is 0 !!! Major BUG!", p != 0);

        Q = new Complex[a.length];

        Complex U;
        Complex V;
        for (int i = 1; i < a.length; i++) {
            U = a[i].plus(b[i].conjugate()).divides(new Complex(2, 0));
            V = a[i].minus(b[i].conjugate()).divides(new Complex(0, 2));

            // ((U(k)V(p)* - V(k)U(p)*)/(U(p)V(p)*-V(p)U(p)*)
            Q[i] = U.times(VpStar).minus(V.times(UpStar))
                    .divides(Det);

            a[i] = Q[i];

            // ((U(k)V(p)* - V(k)U(p)*)/(U(p)V(p)*-V(p)U(p)*)
            Q[i] = U.conjugate().times(VpStar).minus(V.conjugate().times(UpStar))
                    .divides(Det);

            b[i] = Q[i];
        }

        a[0] = new Complex(0, 0);
        b[0] = new Complex(0, 0);

    }

    /**
     * Calc wer of a complex number
     *
     * @param number
     * @param factor
     * @return
     */
    protected Complex calcPower(Complex number, int factor) {
        Complex powered;
        if (factor == 0) {
            powered = new Complex(1, 0);
        } else if (factor > 0) {
            powered = number;
            for (int i = 1; i < factor; i++) {
                powered = powered.times(number);
            }
            return powered;
        } else {
            powered = calcPower(number, 0 - factor);
            powered = new Complex(1, 0).divides(powered);
        }
        return powered;
    }

    public void printInvariants() {
        // System.out.println("INVARIANTS: ");

        for (int i = d.length - 1; i > 0; i--) {
            // System.out.println("-" + i + ":" + d[i]);
        }

        for (int i = 0; i < c.length; i++) {
            // System.out.println(" " + i + ":" + c[i]);
        }

    }

    public double getArg(Complex c) {
        return Math.atan2(c.im(), c.re());
    }

    /**
     * return a float array with the invariants.
     * The invariants go from negative to positive
     *
     * @return a float array holding the invariants
     */
    public float[] getInvariants() {
        float[] result = new float[d.length * 2 - 1];
        int counter = 0;

        for (int i = d.length - 1; i > 0; i--) {
            result[counter++] = (float) d[i];
        }

        for (int i = 0; i < c.length; i++) {
            result[counter++] = (float) c[i];
        }

        return result;
    }


}


