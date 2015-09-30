package cis501.submission;


import cis501.BranchPredictor;
import cis501.ICache;
import cis501.IInorderPipeline;
import cis501.Uop;

enum Stage {
    FETCH(0), DECODE(1), EXECUTE(2), MEMORY(3), WRITEBACK(4);

    private final int index;

    private Stage(int idx) {
        this.index = idx;
    }

    /** Returns the index of this stage within the pipeline */
    public int i() {
        return index;
    }
}

public class InorderPipeline<T extends Uop> implements IInorderPipeline<T> {

    @Override
    public String[] groupMembers() {
        return new String[]{"your", "names"};
    }

    /**
     * Create a new pipeline with the given additional memory latency.
     * @param additionalMemLatency The number of extra cycles mem uops require in the M stage. If 0,
     *                             mem uops require just 1 cycle in the M stage, like all other
     *                             uops. If x, mem uops require 1+x cycles in the M stage.
     */
    public InorderPipeline(int additionalMemLatency) {

    }

    /** ctor for HW4: Branch Prediction */
    public InorderPipeline(int additionalMemLatency, BranchPredictor bp) {
    }

    /** ctor for HW5: Caches */
    public InorderPipeline(BranchPredictor bp, ICache ic, ICache dc) {
    }

    @Override
    public void run(Iterable<T> ui) {

    }

    @Override
    public long getInsns() {
        return 0;
    }

    @Override
    public long getCycles() {
        return 0;
    }
}
