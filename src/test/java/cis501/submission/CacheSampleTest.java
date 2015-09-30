package cis501.submission;

import cis501.*;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CacheSampleTest {

    private static IUopFactory uopF = new cis501.submission.UopFactory();

    private static final int HIT_LAT = 0;
    private static final int CLEAN_MISS_LAT = 2;
    private static final int DIRTY_MISS_LAT = 3;

    private static final int INDEX_BITS = 3;
    private static final int WAYS = 1;
    private static final int BLOCK_BITS = 2;
    private static final int BLOCK_SIZE = 1 << BLOCK_BITS;

    private ICache cache;
    private IInorderPipeline<Uop> pipe;

    /** Runs before each test...() method */
    @Before
    public void setup() {
        cache = new Cache(INDEX_BITS, WAYS, BLOCK_BITS, HIT_LAT, CLEAN_MISS_LAT, DIRTY_MISS_LAT);

        IBranchTargetBuffer btb = new BranchTargetBuffer(3/*index bits*/);
        IDirectionPredictor never = new DirPredNeverTaken();

        // pipeline uses never predictor for simplicity
        pipe = new InorderPipeline<>(new BranchPredictor(never, btb),
                new Cache(INDEX_BITS, WAYS, BLOCK_BITS, HIT_LAT, CLEAN_MISS_LAT, DIRTY_MISS_LAT),
                new Cache(INDEX_BITS, WAYS, BLOCK_BITS, HIT_LAT, CLEAN_MISS_LAT, DIRTY_MISS_LAT));
    }

    // cache tests

    @Test
    public void testInitialState() {
        final long addr = 0xFF << (INDEX_BITS + BLOCK_BITS);
        int lat = cache.access(true, addr);
        assertEquals(CLEAN_MISS_LAT, lat);
    }

    @Test
    public void testRemainderIndexing() {
        final long addr = -1;
        int lat = cache.access(true, addr);
        assertEquals(CLEAN_MISS_LAT, lat);
    }

    @Test
    public void testBlockOffset() {
        final long firstByteInBlock = 0xFF << (INDEX_BITS + BLOCK_BITS);
        int lat = cache.access(true, firstByteInBlock);
        assertEquals(CLEAN_MISS_LAT, lat);
        final long lastByteInBlock = firstByteInBlock + (1 << BLOCK_BITS) - 1;
        lat = cache.access(true, lastByteInBlock);
        assertEquals(HIT_LAT, lat);
    }

    @Test
    public void testLRU() {
        final long a = 0xFF << (INDEX_BITS + BLOCK_BITS);
        final long waySize = (1 << INDEX_BITS) * (1 << BLOCK_BITS);

        int lat = cache.access(true, a);
        assertEquals(CLEAN_MISS_LAT, lat);

        for (int w = 1; w < WAYS * 2; w++) {
            // a hits
            lat = cache.access(true, a);
            assertEquals(HIT_LAT, lat);

            // conflicting access
            lat = cache.access(true, a + (w * waySize));
            assertEquals(CLEAN_MISS_LAT, lat);
        }
    }

    @Test
    public void testFullSetLoads() {
        final long a = 0xFF << (INDEX_BITS + BLOCK_BITS);
        final long waySize = (1 << INDEX_BITS) * (1 << BLOCK_BITS);
        for (int w = 0; w < WAYS; w++) {
            int lat = cache.access(true, a + (w * waySize));
            assertEquals(CLEAN_MISS_LAT, lat);
        }

        // a should still be in the cache
        int lat = cache.access(true, a);
        assertEquals(HIT_LAT, lat);
    }

    @Test
    public void testConflictMissLoads() {
        final long a = 0xFF << (INDEX_BITS + BLOCK_BITS);
        final long waySize = (1 << INDEX_BITS) * (1 << BLOCK_BITS);
        for (int w = 0; w < WAYS + 1; w++) {
            int lat = cache.access(true, a + (w * waySize));
            assertEquals(CLEAN_MISS_LAT, lat);
        }

        // a should have gotten evicted
        int lat = cache.access(true, a);
        assertEquals(CLEAN_MISS_LAT, lat);
    }

    // pipeline integration tests

    private static Uop makeInt(int src1, int src2, int dst, long pc, long fallthru) {
        return uopF.create(src1, src2, dst, null, 1, pc,
                Flags.IgnoreFlags, null, 0, 0,
                fallthru, 0, "", "");
    }

    private static Uop makeMem(int src1, int src2, int dst, long pc, long fallthru, MemoryOp mop, long dataAddr) {
        return uopF.create(src1, src2, dst, mop, 1, pc,
                Flags.IgnoreFlags, null, 0, dataAddr,
                fallthru, 0, "", "");
    }


    @Test
    public void testImiss() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeInt(1, 2, 3, 0xAB, 0xAC));
        pipe.run(uops);

        assertEquals(1, pipe.getInsns());
        // 123456789a
        // f..dxmw|
        assertEquals(6 + CLEAN_MISS_LAT, pipe.getCycles());
    }

    @Test
    public void test2Imiss() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeInt(1, 2, 3, 0, 1 << BLOCK_BITS)); // fallthru is a different cache line
        uops.add(makeInt(1, 2, 3, 1 << BLOCK_BITS, 1 << (BLOCK_BITS + 1)));
        pipe.run(uops);

        assertEquals(2, pipe.getInsns());
        // 123456789abcd
        // f..dxmw   |
        //    f..dxmw|
        assertEquals(7 + (2 * CLEAN_MISS_LAT), pipe.getCycles());
    }

    @Test
    public void testImissIhit() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeInt(1, 2, 3, 0, 1));
        uops.add(makeInt(1, 2, 3, 1, 2));
        pipe.run(uops);

        assertEquals(2, pipe.getInsns());
        // 123456789abcd
        // f..dxmw |
        //    fdxmw|
        assertEquals(7 + CLEAN_MISS_LAT, pipe.getCycles());
    }

    @Test
    public void testManyImiss() {
        List<Uop> uops = new LinkedList<>();
        final int numInsns = 10;
        for (int i = 0; i < numInsns; i++) {
            uops.add(makeInt(1, 2, 3, i * BLOCK_SIZE, (i + 1) * BLOCK_SIZE));
        }
        pipe.run(uops);

        assertEquals(numInsns, pipe.getInsns());
        // 123456789abcdef
        // f..dxmw      |
        //    f..dxmw   |
        //       f..dxmw|
        assertEquals(5 + (numInsns * (CLEAN_MISS_LAT + 1)), pipe.getCycles());
    }

    @Test
    public void testImissDmiss() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeMem(1, 2, 3, 0, 1, MemoryOp.Load, 0xB));
        pipe.run(uops);

        assertEquals(1, pipe.getInsns());
        // 123456789a
        // f..dxm..w|
        assertEquals(6 + (2 * CLEAN_MISS_LAT), pipe.getCycles());
    }

    @Test
    public void testImissDmissIhitDhit() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeMem(1, 2, 3, 0x0, 0x1, MemoryOp.Load, 0x42));
        uops.add(makeMem(1, 2, 3, 0x1, 0x2, MemoryOp.Load, 0x42));
        pipe.run(uops);

        assertEquals(2, pipe.getInsns());
        // 123456789abcd
        // f..dxm..w |
        //    fdx  mw|
        assertEquals(7 + (2 * CLEAN_MISS_LAT), pipe.getCycles());
    }

}
