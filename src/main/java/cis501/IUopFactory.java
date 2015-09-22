package cis501;

public interface IUopFactory {

    /** Create a Uop from a line of the trace file. */
    public Uop create(String line);

    /** Create a Uop manually */
    public Uop create(int sr1, int sr2, int dr, MemoryOp mop, int uid, long pc,
                      Flags f, Direction dir, long immed, long dataAddr,
                      long fallthruPC, long targetPC, String macro, String micro);

}
