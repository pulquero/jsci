package jsci.test;

import jsci.maths.matrix.Matrix;
import jsci.maths.vector.Vector;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 *
 * @author Mark
 */
public class IsCloseTo extends BaseMatcher {
    private final Object value;
    private final double error;

    public IsCloseTo(Object value, double error) {
        this.value = value;
        this.error = error;
    }

    @Override
    public boolean matches(Object item) {
        if(item instanceof Matrix) {
            return ((Matrix)item).equals((Matrix) value, error);
        } else if(item instanceof Vector) {
            return ((Vector)item).equals((Vector) value, error);
        } else if(item instanceof DoubleArray2D) {
            return ((DoubleArray2D)item).contentEquals((DoubleArray2D) value, error);
        } else if(item instanceof DoubleArray1D) {
            return ((DoubleArray1D)item).contentEquals((DoubleArray1D) value, error);
        } else if(item instanceof Double) {
            return Math.abs((Double)item-(Double)value) <= error;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(value).appendText(" within ").appendValue(error);
    }

    @Factory
    public static Matcher<Object> closeTo(Object value, double error) {
        return new IsCloseTo(value, error);
    }
}
