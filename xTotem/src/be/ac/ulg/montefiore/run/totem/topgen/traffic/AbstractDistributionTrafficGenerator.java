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
package be.ac.ulg.montefiore.run.totem.topgen.traffic;

import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.util.distribution.*;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;

import java.util.ArrayList;
import java.util.List;

/*
* Changes:
* --------
*
*/

/**
* TrafficGenerator that uses a distribution. Some specific parameters should be given.
*
* <p>Creation date: 29/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public abstract class AbstractDistributionTrafficGenerator extends AbstractTrafficGenerator {
    private final static ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
    static {
        try {
            params.add(new ParameterDescriptor("trafficDistribution", "Distribution of the traffic", String.class, "Constant", new String[] {"Bimodal", "Constant", "Normal", "Poisson", "Uniform (float)", "Uniform (integer)", "Inverse normal", "Logistic", "LogLogistic", "LogNormal"}));
            params.add(new ParameterDescriptor("bimodalMean1", "use if trafficDistribution is Bimodal.", Double.class, 0.0));
            params.add(new ParameterDescriptor("bimodalMean2", "use if trafficDistribution is Bimodal.", Double.class, 0.0));
            params.add(new ParameterDescriptor("bimodalStddev1", "use if trafficDistribution is Bimodal.", Double.class, 0.0));
            params.add(new ParameterDescriptor("bimodalStddev2", "use if trafficDistribution is Bimodal.", Double.class, 0.0));
            params.add(new ParameterDescriptor("bimodalCoinFlip", "use if trafficDistribution is Bimodal.", Double.class, 0.0));
            params.add(new ParameterDescriptor("constant", "use if trafficDistribution is Constant.", Double.class, 0.0));
            params.add(new ParameterDescriptor("normalMean", "use if trafficDistribution is Normal.", Double.class, 0.0));
            params.add(new ParameterDescriptor("normalStddev", "use if trafficDistribution is Normal.", Double.class, 0.0));
            params.add(new ParameterDescriptor("poissonMean", "use if trafficDistribution is Poisson.", Double.class, 0.0));
            params.add(new ParameterDescriptor("uniformFloatLower", "use if trafficDistribution is \"Uniform (float)\".", Float.class, 0.0f));
            params.add(new ParameterDescriptor("uniformFloatUpper", "use if trafficDistribution is \"Uniform (float)\".", Float.class, 0.0f));
            params.add(new ParameterDescriptor("uniformIntLower", "use if trafficDistribution is \"Uniform (integer)\".", Integer.class, 0));
            params.add(new ParameterDescriptor("uniformIntUpper", "use if trafficDistribution is \"Uniform (integer)\".", Integer.class, 0));
            params.add(new ParameterDescriptor("invNormalMu", "use if trafficDistribution is \"Inverse normal\".", Double.class, 0.0));
            params.add(new ParameterDescriptor("invNormalLambda", "use if trafficDistribution is \"Inverse normal\".", Double.class, 0.0));
            params.add(new ParameterDescriptor("logisticMu", "use if trafficDistribution is \"Logistic\".", Double.class, 0.0));
            params.add(new ParameterDescriptor("logisticSigma", "use if trafficDistribution is \"Logistic\".", Double.class, 0.0));
            params.add(new ParameterDescriptor("logLogisticMu", "use if trafficDistribution is \"LogLogistic\".", Double.class, 0.0));
            params.add(new ParameterDescriptor("logLogisticSigma", "use if trafficDistribution is \"LogLogistic\".", Double.class, 0.0));
            params.add(new ParameterDescriptor("logNormalMu", "use if trafficDistribution is \"LogNormal\".", Double.class, 0.0));
            params.add(new ParameterDescriptor("logNormalSigma", "use if trafficDistribution is \"LogNormal\".", Double.class, 0.0));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    protected Distribution dis;
    protected void initialize_generation() throws InvalidParameterException {
        dis = getDistribution();
    }


    /**
     * Returns a new distribution based on the given parameters
     * @return
     * @throws InvalidParameterException
     */
    private Distribution getDistribution() throws InvalidParameterException {
        Distribution dis;
        try {
            // creating a model
            if(parameters.get("trafficDistribution").equals("Bimodal"))
                dis = new BimodalDistribution(Double.parseDouble(parameters.get("bimodalMean1")),
                        Double.parseDouble(parameters.get("bimodalStddev1")),
                        Double.parseDouble(parameters.get("bimodalMean2")),
                        Double.parseDouble(parameters.get("bimodalStddev2")),
                        Double.parseDouble(parameters.get("bimodalCoinFlip")));

            else if(parameters.get("trafficDistribution").equals("Constant"))
                dis = new ConstantDistribution(Double.parseDouble(parameters.get("constant")));

            else if(parameters.get("trafficDistribution").equals("Normal"))
                dis = new NormalDistribution(Double.parseDouble(parameters.get("normalMean")),
                        Double.parseDouble(parameters.get("normalStddev")));

            else if(parameters.get("trafficDistribution").equals("Poisson"))
                dis = new PoissonDistribution(Double.parseDouble(parameters.get("poissonMean")));

            else if(parameters.get("trafficDistribution").equals("Uniform (float)"))
                dis = new UniformFloatDistribution(Float.parseFloat(parameters.get("uniformFloatLower")),
                        Float.parseFloat(parameters.get("uniformFloatUpper")));

            else if(parameters.get("trafficDistribution").equals("Uniform (integer)"))
                dis = new UniformIntDistribution(Integer.parseInt(parameters.get("uniformIntLower")),
                        Integer.parseInt(parameters.get("uniformIntUpper")));

            else if(parameters.get("trafficDistribution").equals("Inverse normal"))
                dis = new InverseNormalDistribution(Double.parseDouble(parameters.get("invNormalMu")),
                        Double.parseDouble(parameters.get("invNormalLambda")));

            else if(parameters.get("trafficDistribution").equals("Logistic"))
                dis = new LogisticDistribution(Double.parseDouble(parameters.get("logisticMu")),
                        Double.parseDouble(parameters.get("logisticSigma")));

            else if(parameters.get("trafficDistribution").equals("LogLogistic"))
                dis = new LogLogisticDistribution(Double.parseDouble(parameters.get("logLogisticMu")),
                        Double.parseDouble(parameters.get("logLogisticSigma")));

            else if(parameters.get("trafficDistribution").equals("LogNormal"))
                dis = new LogNormalDistribution(Double.parseDouble(parameters.get("logNormalMu")),
                        Double.parseDouble(parameters.get("logNormalSigma")));

            else {
                throw new IllegalArgumentException("Distribution "
                        + parameters.get("trafficDistribution")
                        + " not found !");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InvalidParameterException("Argument invalid:" + e.getMessage());
        }

        return dis;
    }

    public List<ParameterDescriptor> getAvailableParameters() {
        List<ParameterDescriptor> list = super.getAvailableParameters();
        list.addAll(params);
        return list;
    }
}
