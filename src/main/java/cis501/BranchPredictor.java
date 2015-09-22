package cis501;

/** A branch predictor, instantiated with a direction predictor and a branch target buffer. */
public class BranchPredictor {

    private final IDirectionPredictor dp;
    private final IBranchTargetBuffer btb;

    public BranchPredictor(IDirectionPredictor dirp, IBranchTargetBuffer btbuf) {
        this.dp = dirp;
        this.btb = btbuf;
    }

    /**
     * @param pc            the PC of a uop (which may or may not be a branch) on which we make our
     *                      prediction
     * @param fallthroughPC the fallthrough PC of the uop, taken from the trace file
     * @return the predicted next PC
     */
    public long predict(long pc, long fallthroughPC) {
        long btbTarget = btb.predict(pc);
        // if it's not a (taken) branch, just predict fallthroughPC
        if (0 == btbTarget) return fallthroughPC;

        Direction d = dp.predict(pc);
        switch (d) {
            case Taken:
                return btbTarget;
            case NotTaken:
                return fallthroughPC;
            default:
                assert false;
        }
        throw new IllegalStateException("Should never reach here!");
    }

    /**
     * Update the predictor state due to a branch misprediction (either the wrong direction or the
     * wrong target or both).
     *
     * @param pc           the program counter of the branch to train
     * @param actualNextPC the true next PC following the branch
     * @param actualDir    the actual direction of the branch
     */
    public void train(long pc, long actualNextPC, Direction actualDir) {
        // only record taken branches in the BTB, performs better than always updating BTB with actualNextPC
        btb.train(pc, (Direction.Taken == actualDir) ? actualNextPC : 0);
        dp.train(pc, actualDir);
    }

    @Override
    public String toString() {
        return dp.toString() + " " + btb.toString();
    }

}
