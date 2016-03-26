package jsci.test.linalg;

import jsci.maths.linalg.CholeskyDecomposition;
import jsci.maths.linalg.LUDecomposition;
import jsci.maths.linalg.PolarDecomposition;
import jsci.maths.linalg.QRDecomposition;
import jsci.maths.linalg.SingularValueDecomposition;
import jsci.maths.matrix.DoubleMatrix;
import jsci.maths.matrix.impl.AlgorithmsDenseDoubleArray2D;
import jsci.util.array.ArrayUtilities;
import jsci.util.array.DenseDoubleArray1D;
import jsci.util.array.DenseDoubleArray2D;
import jsci.util.array.DiagonalDoubleArray2D;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static jsci.test.IsCloseTo.*;

/**
 *
 * @author Mark
 */
public class DecompositionTest {
    private static final double TOL = 1.0e-12;
    private final int N = 10;
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> matrix;

    public DecompositionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        double[][] arr = ArrayUtilities.createRandomArray(N, N);
        AlgorithmsDenseDoubleArray2D array = new AlgorithmsDenseDoubleArray2D(arr);
        matrix = DoubleMatrix.create(array);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLU() {
        LUDecomposition luDecomp = new LUDecomposition(matrix);
        int[] pivot = luDecomp.getPivot();
        DoubleMatrix lu = luDecomp.getL().multiply(luDecomp.getU());
        double pArray[][]=new double[N][N];
        for(int i=0;i<N;i++) {
            for(int j=0;j<N;j++) {
                pArray[pivot[i]][j] = lu.getDouble(i, j);
            }
        }
        DoubleMatrix pMatrix = DoubleMatrix.create(new DenseDoubleArray2D(pArray));
        assertThat(pMatrix, closeTo(matrix, TOL));
    }
    @Test
    public void testLUInverse() {
        LUDecomposition luDecomp = new LUDecomposition(matrix);
        assertThat(luDecomp.det(), not(0.0));

        DoubleMatrix inv = luDecomp.inverse();
        DoubleMatrix leftInv = inv.multiply(matrix);
        DoubleMatrix rightInv = matrix.multiply(inv);
        assertThat(leftInv, closeTo(rightInv, TOL));

        DenseDoubleArray1D idArray = new DenseDoubleArray1D(ArrayUtilities.createArray(N, 1.0));
        DoubleMatrix id = DoubleMatrix.create(new DiagonalDoubleArray2D(idArray));
        assertThat(leftInv, closeTo(id, TOL));
    }
    @Test
    public void testCholesky() {
        CholeskyDecomposition decomp = new CholeskyDecomposition(matrix);
        DoubleMatrix lu = decomp.getL().multiply(decomp.getU());
        assertThat(lu, closeTo(matrix, TOL));
    }
    @Test
    public void testCholeskyInverse() {
        CholeskyDecomposition decomp = new CholeskyDecomposition(matrix);
        assertThat(decomp.det(), not(0.0));

        DoubleMatrix inv = decomp.inverse();
        DoubleMatrix leftInv = inv.multiply(matrix);
        DoubleMatrix rightInv = matrix.multiply(inv);
        assertThat(leftInv, closeTo(rightInv, TOL));

        DenseDoubleArray1D idArray = new DenseDoubleArray1D(ArrayUtilities.createArray(N, 1.0));
        DoubleMatrix id = DoubleMatrix.create(new DiagonalDoubleArray2D(idArray));
        assertThat(leftInv, closeTo(id, TOL));
    }
    @Test
    public void testQR() {
        QRDecomposition decomp = new QRDecomposition(matrix);
        DoubleMatrix qr = decomp.getQ().multiply(decomp.getR());
        assertThat(qr, closeTo(matrix, TOL));
    }
    @Test
    public void testSVD() {
        SingularValueDecomposition decomp = new SingularValueDecomposition(matrix);
        DoubleMatrix VT = decomp.getV().transpose();
        DoubleMatrix usv = decomp.getU().multiply(decomp.getS()).multiply(VT);
        assertThat(usv, closeTo(matrix, TOL));
    }
    @Test
    public void testPolar() {
        matrix = matrix.add(matrix.transpose());
        PolarDecomposition decomp = new PolarDecomposition(matrix);
        DoubleMatrix up = decomp.getU().multiply(decomp.getP());
        assertThat(up, closeTo(matrix, TOL));
    }
}
