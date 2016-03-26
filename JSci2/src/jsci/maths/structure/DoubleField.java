package jsci.maths.structure;

/**
 *
 * @author Mark
 */
public final class DoubleField implements Field<Double> {
    public static final DoubleField INSTANCE = new DoubleField();
    
    private final Double ZERO = Double.valueOf(0.0);
    private final Double ONE = Double.valueOf(1.0);

    public Double zero() {
        return ZERO;
    }
    public Double unit() {
        return ONE;
    }

    public Double add(Double a, Double b) {
        return Double.valueOf(a+b);
    }
    public Double subtract(Double a, Double b) {
        return Double.valueOf(a-b);
    }
    public Double multiply(Double a, Double b) {
        return Double.valueOf(a*b);
    }
}
