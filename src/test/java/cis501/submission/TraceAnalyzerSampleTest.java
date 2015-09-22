package cis501.submission;

import cis501.ITraceAnalyzer;
import cis501.Uop;
import cis501.UopIterator;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TraceAnalyzerSampleTest {

    private static ITraceAnalyzer<Uop> subm = new TraceAnalyzer<>();

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Run the trace before any tests are run. Then, test the results of the run.

        // TODO: replace the name of trace file here
        UopIterator uiter = new UopIterator("path/to/trace-file", -1, new UopFactory());
        subm.run(uiter);
    }

    /** Simple do-nothing test to verify that the test suite is being run. */
    @Test
    public void testNop() {
        assertTrue(true);
    }

    /** The trace's actual average insn size, so you can check your implementation. */
    @Test
    public void testAvgInsnSize() {
        assertEquals(4.72, subm.avgInsnSize(), 0.01);
    }

    // add more tests here!

}
