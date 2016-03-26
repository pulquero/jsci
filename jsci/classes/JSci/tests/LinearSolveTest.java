package JSci.tests;

import JSci.GlobalSettings;
import JSci.maths.*;
import JSci.maths.matrices.*;
import JSci.maths.vectors.*;
import JSci.util.VectorToolkit;

/**
 * Testcase for linear solve methods.
 * @author Mark Hale
 */
public class LinearSolveTest extends junit.framework.TestCase {
	public static void main(String arg[]) {
			junit.textui.TestRunner.run(ExtraMathTest.class);
	}
	public LinearSolveTest(String name) {
			super(name);
	}

	protected void setUp() {
			GlobalSettings.ZERO_TOL=1.0e-9;
	}

	public void testSolveCG() throws MaximumIterationsExceededException
	{
		DoubleSquareMatrix A = new DoubleSquareMatrix(2);
		A.setElement(0, 0, 3.0);
		A.setElement(0, 1, 2.0);
		A.setElement(1, 0, 2.0);
		A.setElement(1, 1, 6.0);
		Double2Vector b = new Double2Vector(2.0, -8.0);
		AbstractDoubleVector x = VectorToolkit.randomVector(2);
		x = LinearMath.solveCG(A, b, x, 50, 100, GlobalSettings.ZERO_TOL);
		Double2Vector expected = new Double2Vector(2.0, -2.0);
		assertEquals(expected, x);
	}
}
