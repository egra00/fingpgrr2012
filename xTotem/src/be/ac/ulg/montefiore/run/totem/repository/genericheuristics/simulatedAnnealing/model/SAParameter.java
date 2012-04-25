/* TOTEM-v3.2 June 18 2008*/

/*
 * ===========================================================
 * TOTEM : A TOolbox for Traffic Engineering Methods
 * ===========================================================
 *
 * (C) Copyright 2004-2006, by Research Unit in Networking RUN, University of Liege. All Rights Reserved.
 *
 * Project Info:  http://totem.run.montefiore.ulg.ac.be
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License version 2.0 as published by the Free Software Foundation;
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
*/
package be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model;

/*
 * Changes:
 * --------
 *
 */

/**
 * Contains the parameters of an instance of Simulated Annealing : T0, L, alpha, epsilon2, K2 and specify if the problem
 * is a minisation or maximisation problem.
 *
 * <p>Creation date: 18 nov. 2004 12:01:21
 *
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class SAParameter {

    float T0;
    int L;
    float alpha;
    float epsilon2;
    int K2;
    boolean minimise;

    /**
     * Contains the parameters of an GAinstance of Simulated Annealing
     *
     * @param T0 : Initial temperature (must be > 0)
     * @param L : Size of the plateau (must be >= 1)
     * @param alpha : Cooling factor (must be > 0 and < 1)
     * @param epsilon2 : Terminaison value, has to be exprimed in percent (the float value 5.2 corresponds to 5.2%)
     *                   (must be > 0 and <= 100)
     * @param K2 : Terminaison value (must be >=1)
     * @param minimise : true if it is a minimisation problem and false if it is a maximisation problem
     */
    public SAParameter(float T0, int L, float alpha, float epsilon2, int K2, boolean minimise) {
        if (T0 <= 0) {
            throw new IllegalArgumentException("T0 must be > 0.\n");
        }
        if (L < 1) {
            throw new IllegalArgumentException("L must be >= 1\n");
        }
        if ((alpha >= 1) | (alpha <= 0)){
            throw new IllegalArgumentException("alpha must be in the interval ]0,1[\n");
        }
        if ((epsilon2 > 100) | (epsilon2 <= 0)){
            throw new IllegalArgumentException("epsilon2 must be in the interval ]0,100]\n");
        }
        if (K2 < 1) {
            throw new IllegalArgumentException("K2 must be >= 1\n");
        }

        this.T0 = T0;
        this.L = L;
        this.alpha = alpha;
        this.epsilon2 = epsilon2;
        this.K2 = K2;
        this.minimise = minimise;
    }

    public float getT0() {
        return T0;
    }

    public int getL() {
        return L;
    }

    public float getAlpha() {
        return alpha;
    }

    public float getEpsilon2() {
        return epsilon2;
    }

    public int getK2() {
        return K2;
    }

    public boolean getMinimise() {
        return minimise;
    }

    public void setT0(float t0) {
        T0 = t0;
    }

    public void setL(int l) {
        L = l;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setEpsilon2(float epsilon2) {
        this.epsilon2 = epsilon2;
    }

    public void setK2(int k2) {
        K2 = k2;
    }

    public void setMinimise(boolean minimise) {
        this.minimise = minimise;
    }

    public void display() {
        System.out.println("SA parameter : " + toString());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("(TO:");
        sb.append(T0);
        sb.append(", L:");
        sb.append(L);
        sb.append(", alpha:");
                sb.append(alpha);
                                sb.append(", e:");
        sb.append(epsilon2);
                                sb.append(", K:");
        sb.append(K2);
        sb.append(")");
        return sb.toString();
    }
}
