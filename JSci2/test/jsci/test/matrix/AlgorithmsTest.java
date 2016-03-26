package jsci.test.matrix;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import jsci.maths.matrix.DoubleMatrix;
import jsci.maths.matrix.impl.*;
import jsci.util.array.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertThat;
import static jsci.test.IsCloseTo.*;

/**
 *
 * @author Mark
 */
@RunWith(Parameterized.class)
public class AlgorithmsTest {
    private static final double TOL = 0.0;
    private static final int rows = 5;
    private static final int cols = 5;
    private final Class arrayClass1;
    private final Class arrayClass2;
    private DoubleMatrix matrix1;
    private DoubleMatrix matrix2;
    private DoubleMatrix expected1;
    private DoubleMatrix expected2;

    private static final Class[] ARRAY2D_CLASSES = {
        DenseDoubleArray2D.class, SparseDoubleArray2D.class, TridiagonalDoubleArray2D.class, DiagonalDoubleArray2D.class, LowerTriangularDoubleArray2D.class,
        AlgorithmsDenseDoubleArray2D.class, AlgorithmsSparseDoubleArray2D.class
    };

    @Parameters
    public static Collection<Class[]> getTestParameters() {
        ArrayList params = new ArrayList();
        for(int i=0; i<ARRAY2D_CLASSES.length; i++) {
            for(int j=0; j<ARRAY2D_CLASSES.length; j++) {
                params.add(new Class[] {ARRAY2D_CLASSES[i], ARRAY2D_CLASSES[j]});
            }
        }
        return params;
    }

    public AlgorithmsTest(Class arrayCls1, Class arrayCls2) {
        //System.out.println(arrayCls1+", "+arrayCls2);
        arrayClass1 = arrayCls1;
        arrayClass2 = arrayCls2;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        double[][] arr1 = ArrayUtilities.createRandomArray(rows, cols);
        DoubleArray2D array1 = createArray2D(arrayClass1, arr1);
        matrix1 = DoubleMatrix.create(array1);
        expected1 = DoubleMatrix.create(new DenseDoubleArray2D(toArray(array1)));

        double[][] arr2 = ArrayUtilities.createRandomArray(rows, cols);
        DoubleArray2D array2 = createArray2D(arrayClass2, arr2);
        matrix2 = DoubleMatrix.create(array2);
        expected2 = DoubleMatrix.create(new DenseDoubleArray2D(toArray(array2)));
    }

    private static DoubleArray2D createArray2D(Class cls, Object arr) throws Exception {
        Constructor cnstr = cls.getConstructor(double[][].class);
        return (DoubleArray2D) cnstr.newInstance(arr);
    }
    private static double[][] toArray(DoubleArray2D array) {
        double[][] arr = new double[array.rows()][array.columns()];
        for(int i=0; i<arr.length; i++) {
            for(int j=0; j<arr[0].length; j++)
                arr[i][j] = array.getDouble(i,j);
        }
        return arr;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void add() {
        DoubleMatrix expected = expected1.add(expected2);
        assertThat(matrix1.add(matrix2), closeTo(expected, TOL));
        assertThat(matrix2.add(matrix1), closeTo(expected, TOL));
    }

    @Test
    public void subtract() {
        DoubleMatrix expected = expected1.subtract(expected2);
        assertThat(matrix1.subtract(matrix2), closeTo(expected, TOL));
        DoubleMatrix zero = DoubleMatrix.create(new SparseDoubleArray2D(rows, cols));
        DoubleMatrix mNeg21 = zero.subtract(matrix2.subtract(matrix1));
        assertThat(mNeg21, closeTo(expected, TOL));
    }

    @Test
    public void multiply() {
        DoubleMatrix expected = expected1.multiply(expected2);
        assertThat(matrix1.multiply(matrix2), closeTo(expected, TOL));
        assertThat(matrix2.transpose().multiply(matrix1.transpose()).transpose(), closeTo(expected, TOL));
    }
}
