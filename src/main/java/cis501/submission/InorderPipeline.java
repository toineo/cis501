package cis501.submission;

import java.util.EnumMap;

import cis501.IInorderPipeline;
import cis501.MemoryOp;
import cis501.Uop;
import cis501.submission.Stall;


//enum Stage {
//    Fetch(0), Decode(1), Execute(2), Memory(3), Writeback(4);
//
//    private final int index;
//
//    private Stage(int idx) {
//        this.index = idx;
//    }
//
//    /** Returns the index of this stage within the pipeline */
//    public int i() {
//        return index;
//    }
//}

enum Stage {
	Fetch, Decode, Execute, Memory, Writeback
}


// Running but unsatisfyingly basic implementation
public class InorderPipeline<T extends Uop> implements IInorderPipeline<T> {
	private static final Stage[] reverseStageOrder = 
		{Stage.Writeback, Stage.Memory, Stage.Execute, Stage.Decode, Stage.Fetch};
	
	// Let's use java's builtin option type to represent nops
	private static final Uop nop = null;
	
	private EnumMap<Stage, Uop>   pipeline;
	private EnumMap<Stage, Stall> pipelineStalls;
	
	private int cycles = 0;
	private int insns = 0;
	
	private final int addMemLatency;

	
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
    	addMemLatency = additionalMemLatency;

    	pipeline = new EnumMap<Stage, Uop>(Stage.class);
    	pipelineStalls = new EnumMap<Stage, Stall>(Stage.class);
    }

    
    @Override
    public void run(Iterable<T> ui) {
    	
    	// Enforce hw contract
    	if (cycles != 0)
    		throw new IllegalStateException("InorderPipeline.run can only be called once");
    	
    	// Init of pipeline state (empty)
		for (Stage s : Stage.values())
    		pipeline.put(s, null);
    	
        for (Uop uop : ui) {
        	// Count insns
        	if (uop.uopId == 1)
        		insns++;
        	
        	// Invariant: the fetch stage must have no uop stored in it
        	// To maintain this invariant, we performed as many cycles
        	// as needed to free this stage
        	// In the case where there is no stall, this is of course
        	// exactly 1 cycle
        	pipeline.put(Stage.Fetch, uop);
        	
        	
        	// Different cases in which the added uop must be delayed
        	// (currently, only the load-use case)
        	Uop decUop = pipeline.get(Stage.Decode);
        	if (decUop != null && decUop.mem == MemoryOp.Load &&
        			(
        					(uop.srcReg1 == decUop.dstReg) 
        				|| 
        					(uop.srcReg2 == decUop.dstReg && uop.mem != MemoryOp.Store)
        			)
        		)
        		pipelineStalls.put(Stage.Fetch, new Stall(StallCause.Hazard, 1));
        	else
        		pipelineStalls.put(Stage.Fetch, null); // No stall
        	
        	cycleUntilFreeFetch ();
        }
        
        // FIXME: more elegant solution
        cycles++; // Otherwise we omit the very first cycle
        
        // Perform the last operations (flush the pipeline)
        while (pipelineContainsUops())
        	cycleUntilFreeFetch();

    }
    
    
    // Cycle until the fetch stage gets freed
    // That is, until every uop in the pipeline make at least one step forward
    // (some may do more than one)
    private void cycleUntilFreeFetch() {
    	boolean moving; // FIXME name
    	
		do {
			moving = true;
			cycles++;
			
			for (Stage st : reverseStageOrder) {
				Stall stall = pipelineStalls.get(st);
				
				if (moving) {	
					// Stalled uop case
					if (stall != null && stall.ncycles > 0) {
						stall.ncycles--;
						moving = false;
					}
					// No stall, let's just proceed
					else
						stepUopAtStage(st);
				}
				
				else {
					// Not moving, so just update uops that are going through
					// stages with latency (in case there's more than one such
					// stage)
					if (stall != null && stall.cause == StallCause.StageLatency && stall.ncycles > 0)
						stall.ncycles--;					
				}
			}
		} while (!moving);
		// The condition ensures that even the uop in the fetch stage has moved
	}

    private void stepUopAtStage(Stage curStage) {
    	Stage nextStage = getNextStage (curStage);
    	Uop curUop = pipeline.get(curStage);
    	
    	if (nextStage != null) {
    		pipeline.put(nextStage, curUop);
    		pipelineStalls.put(nextStage, getStallAtStage(curUop, nextStage));
    	}

    	// FIXME: we don't need to do it for every stage
		pipeline.put(curStage, nop);
    }
    
    private Stage getNextStage(Stage stage) {
    	switch (stage) {
		case Fetch:
			return Stage.Decode;
		case Decode:
			return Stage.Execute;
		case Execute:
			return Stage.Memory;
		case Memory:
			return Stage.Writeback;
		default:
			return null;
		}
    }
    
    // FIXME: remove, useless
	// Get uop in previous stage, or nop if stage = fetch
    private Uop getPreviousStageUop(Stage stage) {
    	switch (stage) {
    	case Decode:
    		return pipeline.get(Stage.Fetch);
    	case Execute:
    		return pipeline.get(Stage.Decode);
    	case Memory:
    		return pipeline.get(Stage.Execute);
    	case Writeback:
    		return pipeline.get(Stage.Memory);
    	case Fetch: 
    	default:
    		return nop; 
    	}
    }
    
    private boolean pipelineContainsUops() {
    	for (Stage st : reverseStageOrder)
    		if (pipeline.get(st) != nop)
    			return true;
    	
    	return false;
    }
    
	// Get the (potential) stall of <uop> at <stage>
    private Stall getStallAtStage(Uop uop, Stage stage) {
    	// Nops never stall
    	if (uop == null)
    		return null;
    	
		// Currently only one case: memory operation
    	// The test on addMemLatency just ensures we don't add stalls with no cycles
    	if (stage == Stage.Memory && uop.mem != null && addMemLatency > 0)
    		return new Stall(StallCause.StageLatency, addMemLatency);
    		    	
    	return null; // No stall otherwise
    }

    @Override
    public long getInsns() {
        return insns;
    }

    @Override
    public long getCycles() {
        return cycles;
    }
}
