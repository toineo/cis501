package cis501.submission;

// Represents the cause of a stall:
// - Latency in a certain stage (such as mem uops in M stage)
// - Hazard w.r.t a previous uop (such as load-use), which *must* therefore
//   cause 1 or more nops to be inserted in between
enum StallCause {
	StageLatency, Hazard
}

// Represents a stall, via its cause and the number of additional cycles
// For a Hazard stall, ncycles represents the number of nops that must be
// inserted between this uop and the previous one
// This is of course only meant for the fetch stage
public final class Stall {
	public final StallCause cause;
	public final int ncycles;
	
	public Stall(StallCause cause, int ncycles) {
		this.cause = cause;
		this.ncycles = ncycles;
	}
}