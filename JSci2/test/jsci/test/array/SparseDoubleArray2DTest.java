package jsci.test.array;

import java.util.Random;
import jsci.util.array.SparseDoubleArray2D;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Mark
 */
public class SparseDoubleArray2DTest {
    private final int rows = 5;
    private final int cols = 8;
    private final double TOL = 0.05;
    private final double tiny = 0.9*TOL;
    private final double nonZero = 1.1*TOL;
    private SparseDoubleArray2D array;

    public SparseDoubleArray2DTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        array = new SparseDoubleArray2D(rows, cols, TOL, 3);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSetNonZero() {
        for(int i=0; i<rows; i++) {
            for(int j=0; j<cols; j++) {
                array.setDouble(i,j, nonZero);
                assertThat(array.getDouble(i,j), is(nonZero));
            }
        }
        for(int i=0; i<rows; i++) {
            for(int j=0; j<cols; j++) {
                assertThat(array.getDouble(i,j), is(nonZero));
            }
        }
        assertThat(array.getElementCount(), is(rows*cols));
    }

    @Test
    public void testSetZero() {
        for(int i=0; i<rows; i++) {
            for(int j=0; j<cols; j++) {
                array.setDouble(i,j, tiny);
                assertThat(array.getDouble(i,j), is(0.0));
            }
        }
        assertThat(array.getElementCount(), is(0));
    }

    @Test
    public void testOverwriteWithZeros() {
        testSetNonZero();
        testSetZero();
    }

    @Test
    public void testRandomSet() {
        Random rnd = new Random();
        for(int n=0; n<rows*cols; n++) {
            int i = rnd.nextInt(rows);
            int j = rnd.nextInt(cols);
            double value = rnd.nextDouble();
            array.setDouble(i,j, value);
            if(value <= TOL)
                value = 0.0;
            assertThat(array.getDouble(i,j), is(value));
        }
    }

    @Test
    public void testSetSubRow() {
        double[] expected = {2.0, 0.0, 3.0, 0.0, 0.0, 4.0};
        double[] sub = new double[expected.length];
        for(int i=0; i<expected.length; i++) {
            if(expected[i] != 0.0)
                sub[i] = expected[i];
            else
                sub[i] = tiny;
        }

        int row = 2;
        int startCol = 2;
        array.setSubRow(row, startCol, sub);
        double[] actual = array.getSubRow(row, startCol, startCol+expected.length);
        assertThat(actual, is(expected));

        actual = array.getSubRow(row, 0, cols);
        for(int i=0; i<cols; i++) {
            assertThat(actual[i], is(array.getDouble(row, i)));
            double[] single = array.getSubRow(row, i, i+1);
            assertThat(actual[i], is(single[0]));
        }
    }
}
