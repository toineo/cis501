package cis501.submission;

import cis501.*;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BranchPredSampleTest {

    // TODO: replace the path of trace file here
    private static final String TRACE_FILE = "path/to/go-10M.trace.gz";

    private static IUopFactory uopF = new UopFactory();

    private IBranchTargetBuffer btb;
    private IDirectionPredictor bimodal;
    private IDirectionPredictor gshare;
    private IDirectionPredictor tournament;
    private IInorderPipeline<Uop> pipe;

    @Before
    public void setUp() throws Exception {
        // Runs before each test...() method
        btb = new BranchTargetBuffer(3/*index bits*/);
        bimodal = new DirPredBimodal(3/*index bits*/);
        gshare = new DirPredGshare(3/*index bits*/, 1/*history bits*/);

        // create a tournament predictor that behaves like bimodal
        IDirectionPredictor always = new DirPredAlwaysTaken();
        IDirectionPredictor never = new DirPredNeverTaken();
        tournament = new DirPredTournament(3/*index bits*/, never, always);

        // pipeline uses never predictor
        pipe = new InorderPipeline<>(0, new BranchPredictor(never, btb));
    }

    // BTB tests

    @Test
    public void testBtbInitialState() {
        assertEquals(0, btb.predict(0));
    }

    @Test
    public void testBtbNewTarget() {
        btb.train(0, 42);
        assertEquals(42, btb.predict(0));
    }

    @Test
    public void testBtbAlias() {
        btb.train(0, 42);
        assertEquals(42, btb.predict(0));
        btb.train((long) Math.pow(2, 3), 100);
        assertEquals(100, btb.predict(0));
    }

    // Bimodal tests

    @Test
    public void testBimodalInitialState() {
        assertEquals(Direction.NotTaken, bimodal.predict(0));
    }

    @Test
    public void testBimodalTaken() {
        bimodal.train(0, Direction.Taken);
        assertEquals(Direction.NotTaken, bimodal.predict(0));
        bimodal.train(0, Direction.Taken);
        assertEquals(Direction.Taken, bimodal.predict(0));
    }

    @Test
    public void testBimodalTakenSaturation() {
        for (int i = 0; i < 10; i++) {
            bimodal.train(0, Direction.Taken);
        }
        bimodal.train(0, Direction.NotTaken);
        bimodal.train(0, Direction.NotTaken);
        assertEquals(Direction.NotTaken, bimodal.predict(0));
    }

    // Gshare tests

    @Test
    public void testGshareInitialState() {
        assertEquals(Direction.NotTaken, gshare.predict(0));
    }

    @Test
    public void testGshareTaken() {
        // initially, history is 0
        gshare.train(0, Direction.Taken); // 0 ^ 0 == 0
        // history is 1
        assertEquals(Direction.NotTaken, gshare.predict(1)); // 1 ^ 1 == 0
        gshare.train(1, Direction.Taken); // 1 ^ 1 == 0
        // history is 1
        assertEquals(Direction.Taken, gshare.predict(1)); // 1 ^ 1 == 0
    }

    // Tournament predictor tests

    @Test
    public void testTournInitialState() {
        assertEquals(Direction.NotTaken, tournament.predict(0));
    }

    @Test
    public void testTournTaken() {
        tournament.train(0, Direction.Taken);
        assertEquals(Direction.NotTaken, tournament.predict(0));
        tournament.train(0, Direction.Taken);
        assertEquals(Direction.Taken, tournament.predict(0));
    }

    @Test
    public void testTournTakenSaturation() {
        for (int i = 0; i < 10; i++) {
            tournament.train(0, Direction.Taken);
        }
        tournament.train(0, Direction.NotTaken);
        tournament.train(0, Direction.NotTaken);
        assertEquals(Direction.NotTaken, tournament.predict(0));
    }

    // Pipeline tests

    private static Uop makeBr(long pc, Direction dir, long fallthruPC, long targetPC) {
        return uopF.create(1, 2, 3, null, 1, pc,
                Flags.IgnoreFlags, dir, 0, 0,
                fallthruPC, targetPC, "", "");
    }

    @Test
    public void testPipeCorrectPred() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeBr(0, Direction.NotTaken, 1, 40));
        uops.add(makeBr(1, Direction.NotTaken, 2, 40));
        pipe.run(uops);

        assertEquals(2, pipe.getInsns());
        // 1234567890
        // fdxmw |
        //  fdxmw|
        assertEquals(7, pipe.getCycles());
    }

    @Test
    public void testPipeMispredicted() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeBr(0, Direction.Taken, 1, 40));  // mispredicted
        uops.add(makeBr(40, Direction.NotTaken, 41, 60));
        pipe.run(uops);

        assertEquals(2, pipe.getInsns());
        // 1234567890
        // fdxmw   |
        //  ..fdxmw|
        assertEquals(7 + 2, pipe.getCycles());
    }

    @Test
    public void testPipe2Mispredicted() {
        List<Uop> uops = new LinkedList<>();
        uops.add(makeBr(0, Direction.Taken, 1, 40));  // mispredicted
        uops.add(makeBr(40, Direction.Taken, 41, 60));  // mispredicted
        uops.add(makeBr(60, Direction.NotTaken, 61, 80));
        pipe.run(uops);

        assertEquals(3, pipe.getInsns());
        // 1234567890abcd
        // fdxmw      |
        //  ..fdxmw   |
        //     ..fdxmw|
        assertEquals(8 + (2 * 2), pipe.getCycles());
    }

    // Trace tests: actual IPCs for go-10M.trace.gz with the always/never-taken predictors
    // and zero additional memory latency.

    @Test
    public void testAlwaysTakenTrace() {
        final IDirectionPredictor always = new DirPredAlwaysTaken();
        final IBranchTargetBuffer bigBtb = new BranchTargetBuffer(10);
        UopIterator uiter = new UopIterator(TRACE_FILE, -1, new UopFactory());
        IInorderPipeline<Uop> pl = new InorderPipeline<>(0, new BranchPredictor(always, bigBtb));
        pl.run(uiter);
        assertEquals(0.276, pl.getInsns() / (double) pl.getCycles(), 0.01);
    }

    @Test
    public void testNeverTakenTrace() {
        final IDirectionPredictor never = new DirPredNeverTaken();
        final IBranchTargetBuffer bigBtb = new BranchTargetBuffer(10);
        UopIterator uiter = new UopIterator(TRACE_FILE, -1, new UopFactory());
        IInorderPipeline<Uop> pl = new InorderPipeline<>(0, new BranchPredictor(never, bigBtb));
        pl.run(uiter);
        assertEquals(0.338, pl.getInsns() / (double) pl.getCycles(), 0.01);
    }

    // add more tests here!
}
