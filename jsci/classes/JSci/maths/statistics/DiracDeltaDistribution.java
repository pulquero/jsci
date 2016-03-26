package JSci.maths.statistics;

import java.util.Random;

/**
 *
 * @author Mark
 */
public class DiracDeltaDistribution extends ProbabilityDistribution {
    private final double mean;

    public DiracDeltaDistribution(double mean)
    {
        this.mean = mean;
    }
    public double probability(double X) {
        return (X == mean ? 1.0 : 0.0);
    }

    public double cumulative(double X) {
        return (X < mean ? 0.0 : 1.0);
    }

    public double inverse(double probability) {
        return probability == 1.0 ? mean : Double.NaN;
    }

    /**
     * Generates a random variate from this distribution.
     */
    public double generate(Random rnd)
    {
        return mean;
    }
}
