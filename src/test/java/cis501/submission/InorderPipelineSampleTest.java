package cis501.submission;

import cis501.*;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InorderPipelineSampleTest {

    private static IUopFactory uopF = new UopFactory();
    private static IInorderPipeline<Uop> sim;

    private static Uop makeUop(int sr1, int sr2, int dr, MemoryOp mop) {
        return uopF.create(sr1, sr2, dr, mop, 1, 0,
                Flags.IgnoreFlags, null, 0, 0,
                0, 0, "", "");
    }

    @Before
    public void setup() {
        sim = new InorderPipeline<>(0/*no add'l memory latency*/);
    }

    @Test
    public void test1Uop() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeUop(1, 2, 3, null));
        sim.run(uops);
        assertEquals(1, sim.getInsns());
        // 123456
        // fdxmw|
        assertEquals(6, sim.getCycles());
    }

    @Test
    public void testManyUops() {
        List<Uop> uops = new LinkedList<>();
        final int COUNT = 10;
        for (int i = 0; i < COUNT; i++) {
            uops.add(makeUop(1, 2, 3, null));
        }
        sim.run(uops);
        assertEquals(COUNT, sim.getInsns());
        assertEquals(5 + COUNT, sim.getCycles());
    }

    @Test
    public void test1MemUop() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeUop(1, 2, 3, MemoryOp.Load));
        sim.run(uops);
        assertEquals(1, sim.getInsns());
        // 123456789abcdef
        // fdxmw|
        assertEquals(6, sim.getCycles());
    }

    @Test
    public void testManyMemUops() {
        List<Uop> uops = new LinkedList<>();
        final int COUNT = 10;
        for (int i = 0; i < COUNT; i++) {
            uops.add(makeUop(1, 2, 3, MemoryOp.Store)); // no load-use dependencies
        }
        sim.run(uops);
        assertEquals(COUNT, sim.getInsns());
        // 123456789abcdefghi
        // fdxmw  |
        //  fdxmw |
        //   fdxmw|
        assertEquals(5 + COUNT, sim.getCycles());
    }

    @Test
    public void testLoadUse1() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeUop(1, 2, 3, MemoryOp.Load));
        uops.add(makeUop(3, 4, 5, null)); // load to src reg 1
        sim.run(uops);
        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw  |
        //  fd xmw|
        assertEquals(6 + 2, sim.getCycles());
    }

}
