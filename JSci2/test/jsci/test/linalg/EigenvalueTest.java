package jsci.test.linalg;

import jsci.maths.linalg.EigenDecomposition;
import jsci.maths.linalg.LUDecomposition;
import jsci.maths.matrix.DoubleMatrix;
import jsci.maths.matrix.impl.*;
import jsci.maths.vector.DoubleVector;
import jsci.util.array.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static jsci.test.IsCloseTo.*;

/**
 *
 * @author Mark
 */
public class EigenvalueTest {
    private static final double TOL = 1.0e-10;
    private static final double SPARSE_TOL = 0.0;
    private final int N = 10;
    private DoubleMatrix matrix;

    public EigenvalueTest() {
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
        DoubleArray2D array = new AlgorithmsDenseDoubleArray2D(arr);
        matrix = DoubleMatrix.create(array);
        matrix = matrix.add(matrix.transpose());
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testEigenvectors() {
        EigenDecomposition eigen = new EigenDecomposition(matrix, true);
        double[] eval = eigen.getEigenvalues();
        DoubleVector[] evec = eigen.getEigenvectors();
        for (int i = 0; i < eval.length; i++) {
            DoubleVector matrixVec = matrix.act(evec[i]);
            DoubleVector valVec = evec[i].scalarMultiply(eval[i]);
            assertThat(matrixVec, closeTo(valVec, TOL));
        }
    }

    @Test
    public void testOrthogonalTransformation() {
        EigenDecomposition eigen = new EigenDecomposition(matrix, true);
        DoubleVector[] evec = eigen.getEigenvectors();
        double[][] array = new double[evec.length][evec.length];
        for(int i=0; i<array.length; i++) {
            for(int j=0; j<array[0].length; j++) {
                array[i][j] = evec[j].getDouble(i);
            }
        }
        DoubleMatrix transf = DoubleMatrix.create(new DenseDoubleArray2D(array));
        LUDecomposition transfLU = new LUDecomposition(transf);
        DoubleMatrix res = transfLU.inverse().multiply(matrix).multiply(transf);
        double[] eval = eigen.getEigenvalues();
        DoubleMatrix diag = DoubleMatrix.create(new DiagonalDoubleArray2D(new DenseDoubleArray1D(eval)));
        assertThat(res, closeTo(diag, TOL));
    }

    @Test
    public void testTrace() {
        EigenDecomposition eigen = new EigenDecomposition(matrix, false);
        double[] eval = eigen.getEigenvalues();
        double tr = 0.0;
        for (int i = 0; i < eval.length; i++) {
            tr += eval[i];
        }
        assertThat(tr, closeTo(matrix.trace(), TOL));
    }

    @Test
    public void testDeterminant() {
        EigenDecomposition eigen = new EigenDecomposition(matrix, false);
        double[] eval = eigen.getEigenvalues();
        double det = 1.0;
        for (int i = 0; i < eval.length; i++) {
            det *= eval[i];
        }
        LUDecomposition lu = new LUDecomposition(matrix);
        assertThat(det, closeTo(lu.det(), TOL));
    }

    @Test
    public void testDiagonalize() {
        EigenDecomposition eigen = new EigenDecomposition(matrix, false);
        double[] eval = eigen.getEigenvalues();

        DoubleArray2D array = matrix.getArray2D();
        DoubleArray1D offdiag = new DenseDoubleArray1D(matrix.rows());
        int n = offdiag.size();
        for(int i=n-1; i>0; i--)
            EigenDecomposition.reduceSymmetricToTridiagonal(array, offdiag, i, i-1);
        for(int i=0; i<n; i++)
            EigenDecomposition.diagonalizeSymmetricTridiagonal(array, offdiag, i, i+1, 250);

        for(int i=0; i<eval.length; i++) {
            assertThat(array.getDouble(i, i), closeTo(eval[i], TOL));
        }
    }

    @Test
    public void testDiagonalizeSparse() {
        EigenDecomposition eigen = new EigenDecomposition(matrix, false);
        double[] eval = eigen.getEigenvalues();

        AlgorithmsSparseDoubleArray2D array = new AlgorithmsSparseDoubleArray2D(matrix.rows(), matrix.columns(), SPARSE_TOL, matrix.rows());
        for(int i=0; i<matrix.rows(); i++) {
            for(int j=0; j<matrix.columns(); j++) {
                array.setDouble(i,j, matrix.getDouble(i, j));
            }
        }

        SparseDoubleArray1D offdiag = new SparseDoubleArray1D(matrix.rows());
        int n = offdiag.size();
        EigenDecomposition.reduceSymmetricToTridiagonal(array, offdiag, n-1, 0);
        EigenDecomposition.diagonalizeSymmetricTridiagonal(array, offdiag, 0, n, 250);

        for(int i=0; i<eval.length; i++) {
            assertThat(array.getDouble(i, i), closeTo(eval[i], TOL));
        }
    }

    @Test
    public void testDiagonalizeSparseGraph() {
        matrix = DoubleMatrix.create(new AlgorithmsSparseDoubleArray2D(new double[][] {
            {1.0, 0.0, 0.0, -0.4082482904638631, 0.0, -0.5},
            {0.0, 1.0, 0.0, -0.4082482904638631, -0.5, 0.0},
            {0.0, 0.0, 1.0, -0.3333333333333333, -0.4082482904638631, -0.4082482904638631},
            {-0.4082482904638631, -0.4082482904638631, -0.3333333333333333, 1.0, 0.0, 0.0},
            {0.0, -0.5, -0.4082482904638631, 0.0, 1.0, 0.0},
            {-0.5, 0.0, -0.4082482904638631, 0.0, 0.0, 1.0}
        }));
        EigenDecomposition eigen = new EigenDecomposition(matrix, false);
        double[] eval = eigen.getEigenvalues();

        DoubleArray2D array = matrix.getArray2D();
        SparseDoubleArray1D offdiag = new SparseDoubleArray1D(matrix.rows());
        int n = offdiag.size();
        EigenDecomposition.reduceSymmetricToTridiagonal(array, offdiag, n-1, 0);
        EigenDecomposition.diagonalizeSymmetricTridiagonal(array, offdiag, 0, n, 250);

        for(int i=0; i<eval.length; i++) {
            assertThat(array.getDouble(i, i), closeTo(eval[i], TOL));
        }
    }

    @Test
    public void testRosser() {
        matrix = DoubleMatrix.create(new DenseDoubleArray2D(rosserMatrix()));
        EigenDecomposition eigen = new EigenDecomposition(matrix, false);
        double[] eval = eigen.getEigenvalues();

        DoubleArray2D array = matrix.getArray2D();
        SparseDoubleArray1D offdiag = new SparseDoubleArray1D(matrix.rows());
        int n = offdiag.size();
        EigenDecomposition.reduceSymmetricToTridiagonal(array, offdiag, n-1, 0);
        EigenDecomposition.diagonalizeSymmetricTridiagonal(array, offdiag, 0, n, 250);

        double[] rosserEigenvalues = rosserEigenvalues();
        for(int i=0; i<eval.length; i++) {
            assertThat(array.getDouble(i, i), closeTo(eval[i], TOL));
            assertThat(eval[i], closeTo(rosserEigenvalues[i], TOL));
        }
    }

    @Test
    public void testWilkinson() {
        matrix = DoubleMatrix.create(new AlgorithmsSparseDoubleArray2D(wilkinsonMatrix(5)));
        EigenDecomposition eigen = new EigenDecomposition(matrix, false);
        double[] eval = eigen.getEigenvalues();

        DoubleArray2D array = matrix.getArray2D();
        SparseDoubleArray1D offdiag = new SparseDoubleArray1D(matrix.rows());
        int n = offdiag.size();
        EigenDecomposition.reduceSymmetricToTridiagonal(array, offdiag, n-1, 0);
        EigenDecomposition.diagonalizeSymmetricTridiagonal(array, offdiag, 0, n, 250);

        for(int i=0; i<eval.length; i++) {
            assertThat(array.getDouble(i, i), closeTo(eval[i], TOL));
        }
    }

    public static double[][] rosserMatrix() {
            return new double[][] {
                { 611,  196, -192,  407,   -8,  -52,  -49,   29},
                { 196,  899,  113, -192,  -71,  -43,   -8,  -44},
                {-192,  113,  899,  196,   61,   49,    8,   52},
                { 407, -192,  196,  611,    8,   44,   59,  -23},
                {  -8,  -71,   61,    8,  411, -599,  208,  208},
                { -52,  -43,   49,   44, -599,  411,  208,  208},
                { -49,   -8,    8,   59,  208,  208,   99, -911},
                {  29,  -44,   52,  -23,  208,  208, -911,   99}
            };
        }

    public static double[] rosserEigenvalues() {
        return new double[] {
                1000,
                1000,
                10*Math.sqrt(10405),
                1020,
                510+100*Math.sqrt(26),
                510-100*Math.sqrt(26),
                0,
               -10*Math.sqrt(10405)
            };
    }

    public static double[][] wilkinsonMatrix(int n) {
            double[][] w = new double[n][n];
            for(int i=0; i<n; i++)
                w[i] = new double[n];
            int half = (n-1)/2;
            for(int i=0; i<half; i++) {
                w[i][i+1] = 1.0;
                w[i+1][i] = 1.0;
                w[i][i] = half-i;
                w[n-1-i][n-1-i] = half-i;
                w[n-1-i][n-2-i] = 1.0;
                w[n-2-i][n-1-i] = 1.0;
            }
            return w;
        }
}
