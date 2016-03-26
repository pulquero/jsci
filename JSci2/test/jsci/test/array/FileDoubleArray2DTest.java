package jsci.test.array;

import java.io.IOException;
import java.util.logging.Logger;
import jsci.maths.linalg.EigenDecomposition;
import jsci.util.array.ArrayUtilities;
import jsci.util.array.FileDoubleArray2D;
import jsci.util.array.SparseDoubleArray1D;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Mark
 */
public class FileDoubleArray2DTest {
    private final int N = 100;
    private final int rows = N;
    private final int cols = N;
    private FileDoubleArray2D array;

    @Before
    public void setUp() throws IOException {
        double[][] arr = ArrayUtilities.createRandomArray(rows, cols);
        array = new FileDoubleArray2D(arr);
    }

    @After
    public void tearDown() throws IOException {
        array.close();
    }

    @Test
    public void testPerformanceDiagonalize() throws IOException {
        Logger.getAnonymousLogger().info("Diagonalize");

        // matrix not symmetric
        SparseDoubleArray1D offdiag = new SparseDoubleArray1D(N);
        EigenDecomposition.reduceSymmetricToTridiagonal(array, offdiag, N-1, 0);
        EigenDecomposition.diagonalizeSymmetricTridiagonal(array, offdiag, 0, N, 250);

        int hits = array.getBufferHits();
        int misses = array.getBufferMisses();
        Logger.getAnonymousLogger().info("Hits/Misses: "+hits+"/"+misses);
        Logger.getAnonymousLogger().info("Hits: "+(hits*100/(hits+misses))+"%");
        Logger.getAnonymousLogger().info("Offdiag: "+offdiag.getElementCount()+"/"+N);
    }
}
