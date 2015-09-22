package cis501;

import java.util.StringTokenizer;

/** Class representing a single micro-op. */
public class Uop {

    /** Index of the uop within the macro-op */
    public final int uopId;

    /** Input 1 for ALU ops. Unused for loads, address for stores. */
    public final short srcReg1;

    /** Input 2 for ALU ops. Address for loads, value for stores. */
    public final short srcReg2;

    public final short dstReg;
    public final MemoryOp mem;

    public final long instructionAddress;
    public final Direction branch;
    public final long fallthroughPC;
    public final long targetAddressTakenBranch;

    public final long dataAddress;

    public final long immediate;
    public final Flags flags;
    public final String microOperation;
    public final String macroOperation;


    protected Flags FlagsOfChar(char c) {
        switch (c) {
            case 'R':
                return Flags.ReadFlags;
            case 'W':
                return Flags.WriteFlags;
            case '-':
                return Flags.IgnoreFlags;
            default:
                throw new IllegalArgumentException("Invalid flag type: " + c);
        }
    }

    protected Direction BranchOfChar(char c) {
        switch (c) {
            case 'T':
                return Direction.Taken;
            case 'N':
                return Direction.NotTaken;
            case '-':
                return null;
            default:
                throw new IllegalArgumentException("Invalid branch type: " + c);
        }
    }

    protected MemoryOp MemOfChar(char c) {
        switch (c) {
            case 'L':
                return MemoryOp.Load;
            case 'S':
                return MemoryOp.Store;
            case '-':
                return null;
            default:
                throw new IllegalArgumentException("Invalid mem op: " + c);
        }
    }

    /** Parse a uop from a line in the trace file */
    public Uop(String line) {
        StringTokenizer st = new StringTokenizer(line);
        assert 14 == st.countTokens();

        this.uopId = Integer.parseInt(st.nextToken());
        assert this.uopId >= 1;
        this.instructionAddress = Long.parseLong(st.nextToken(), 16);
        this.srcReg1 = Short.parseShort(st.nextToken());
        this.srcReg2 = Short.parseShort(st.nextToken());
        this.dstReg = Short.parseShort(st.nextToken());
        this.flags = FlagsOfChar(st.nextToken().charAt(0));
        this.branch = BranchOfChar(st.nextToken().charAt(0));
        this.mem = MemOfChar(st.nextToken().charAt(0));
        this.immediate = Long.parseLong(st.nextToken());
        this.dataAddress = Long.parseLong(st.nextToken(), 16);
        this.fallthroughPC = Long.parseLong(st.nextToken(), 16);
        this.targetAddressTakenBranch = Long.parseLong(st.nextToken(), 16);
        this.macroOperation = st.nextToken();
        this.microOperation = st.nextToken();
    }

    /** Create a uop manually. Used for testing. */
    public Uop(int sr1, int sr2, int dr, MemoryOp mop, int uid, long pc,
               Flags f, Direction dir, long immed, long dataAddr,
               long fallthruPC, long targetPC, String macro, String micro) {
        this.srcReg1 = (short) sr1;
        this.srcReg2 = (short) sr2;
        this.dstReg = (short) dr;
        this.mem = mop;
        this.uopId = uid;
        assert this.uopId >= 1;
        this.instructionAddress = pc;
        this.flags = f;
        this.branch = dir;
        this.immediate = immed;
        this.dataAddress = dataAddr;
        this.fallthroughPC = fallthruPC;
        this.targetAddressTakenBranch = targetPC;
        this.macroOperation = macro;
        this.microOperation = micro;
    }

}
