package cis501.submission;

import cis501.*;

/** If you subclass cis501.Uop, update this factory to generate instances of your subclass */
public class UopFactory implements IUopFactory {

    @Override
    public Uop create(String line) {
        return new Uop(line);
    }

    @Override
    public Uop create(int sr1, int sr2, int dr, MemoryOp mop, int uid, long pc,
                      Flags f, Direction dir, long immed, long dataAddr, long fallthruPC,
                      long targetPC, String macro, String micro) {
        return new Uop(sr1, sr2, dr, mop, uid, pc,
        f, dir, immed, dataAddr,
        fallthruPC, targetPC, macro, micro);
    }

}
