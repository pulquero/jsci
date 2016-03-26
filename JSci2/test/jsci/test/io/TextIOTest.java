package jsci.test.io;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import jsci.io.TextReader;
import jsci.io.TextWriter;
import jsci.util.array.ArrayUtilities;
import jsci.util.array.DenseDoubleArray1D;
import jsci.util.array.DenseDoubleArray2D;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;
import jsci.util.array.SparseDoubleArray1D;
import jsci.util.array.SparseDoubleArray2D;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static jsci.test.IsCloseTo.*;

/**
 *
 * @author Mark
 */
@RunWith(Parameterized.class)
public class TextIOTest {
    private final NumberFormat format;

    @Parameters
    public static Collection<NumberFormat[]> getTestParameters() {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);
        format.setMaximumIntegerDigits(Integer.MAX_VALUE);
        format.setMaximumFractionDigits(Integer.MAX_VALUE);
        return Arrays.asList(
            new NumberFormat[] {null},
            new NumberFormat[] {format}
        );
    }

    public TextIOTest(NumberFormat format) {
        this.format = format;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testArray() throws IOException, ParseException {
        double[][] data = {{56953412080761.125632}, {3.1000245}, {54.7762, 16.045}, {-23.6,2.45,-13.67}};

        StringWriter buf = new StringWriter();
        TextWriter writer = new TextWriter(buf);
        writer.setFormat(format);
        writer.write(data);
        writer.close();

        TextReader reader = new TextReader(new StringReader(buf.toString()));
        reader.setFormat(format);
        double[][] actual = reader.readArray();
        reader.close();

        assertThat(actual, is(data));
    }

    @Test
    public void testArray2D() throws IOException, ParseException {
        int rows = 4;
        int cols = 6;
        DoubleArray2D data = new DenseDoubleArray2D(ArrayUtilities.createRandomArray(rows, cols));

        StringWriter buf = new StringWriter();
        TextWriter writer = new TextWriter(buf);
        writer.setFormat(format);
        writer.write(data);
        writer.close();

        DoubleArray2D actual = new SparseDoubleArray2D(rows, cols);
        TextReader reader = new TextReader(new StringReader(buf.toString()));
        reader.setFormat(format);
        reader.read(actual);
        reader.close();

        assertThat(actual, closeTo(data, 0.0));
    }

    @Test
    public void testTable() throws IOException, ParseException {
        int rows = 4;
        int cols = 6;
        DoubleArray2D data = new DenseDoubleArray2D(ArrayUtilities.createRandomArray(rows, cols));

        StringWriter buf = new StringWriter();
        TextWriter writer = new TextWriter(buf);
        writer.setFormat(format);
        writer.writeTable(data);
        writer.close();

        DoubleArray2D actual = new SparseDoubleArray2D(rows, cols);
        TextReader reader = new TextReader(new StringReader(buf.toString()));
        reader.setFormat(format);
        reader.readTable(actual);
        reader.close();

        assertThat(actual, closeTo(data, 0.0));
    }

    @Test
    public void testColumn() throws IOException, ParseException {
        int rows = 1;
        int cols = 8;
        DoubleArray1D data = new DenseDoubleArray1D(ArrayUtilities.createRandomArray(rows, cols)[0]);

        StringWriter buf = new StringWriter();
        TextWriter writer = new TextWriter(buf);
        writer.setFormat(format);
        writer.writeColumn(data);
        writer.close();

        DoubleArray1D actual = new SparseDoubleArray1D(cols);
        TextReader reader = new TextReader(new StringReader(buf.toString()));
        reader.setFormat(format);
        reader.readColumn(actual);
        reader.close();

        assertThat(actual, closeTo(data, 0.0));
    }
}