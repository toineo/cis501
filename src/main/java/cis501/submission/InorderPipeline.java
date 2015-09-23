package cis501.submission;

import java.util.EnumMap;

import cis501.IInorderPipeline;
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
        	
        	// TODO
        	
        	// TODO: cycleUntil method
        }

    }
    
    
    // Get uop in previous stage, or nop if stage = fetch
    private Uop getPreviousStageUop(EnumMap<Stage, Uop> pipeline, Stage stage) {
    	switch (stage) {
    	case Fetch: 
    		return nop;
    	case Decode:
    		return pipeline.get(Stage.Fetch);
    	case Execute:
    		return pipeline.get(Stage.Decode);
    	case Memory:
    		return pipeline.get(Stage.Execute);
    	case Writeback:
    		return pipeline.get(Stage.Memory);
    	default:
    		return nop; // Just to kill compiler warnings
    	}
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
