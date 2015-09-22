package cis501.submission;

import cis501.ITraceAnalyzer;
import cis501.MemoryOp;
import cis501.Uop;
import cis501.Flags;

public class TraceAnalyzer<T extends Uop> implements ITraceAnalyzer<T> {
	private int [] uopCatCount;
	private int [] insnsSizeCount;
	private int [] branchTgtSizeCount;
	
	// Making these inner classes is totally useless, but thereby total swag as well.
	enum UopCategory {
		Load, Store, UnCondBranch, CondBranch, Other
	}
	
	interface IUopCat {
		int toInt ();
	}
	
	private static class LoadCat implements IUopCat {
		@Override
		public int toInt() {
			return 0;
		}
		@Override
		public String toString() {
			// Plurals because of the subject
			return "loads";
		}
	}
	private static class StoreCat implements IUopCat {
		@Override
		public int toInt() {
			return 1;
		}
		@Override
		public String toString() {
			return "stores";
		}
	}
	private static class UncondBranchCat implements IUopCat {
		@Override
		public int toInt() {
			return 2;
		}
		@Override
		public String toString() {
			return "unconditionalbranches";
		}
	}
	private static class CondBranchCat implements IUopCat {
		@Override
		public int toInt() {
			return 3;
		}
		@Override
		public String toString() {
			return "conditionalbranches";
		}
	}
	private static class OtherCat implements IUopCat {
		@Override
		public int toInt() {
			return 4;
		}
		@Override
		public String toString() {
			return "other";
		}
	}
	
	private LoadCat loadCat = new LoadCat();
	private StoreCat storeCat = new StoreCat();
	private UncondBranchCat uncondBranchCat = new UncondBranchCat();
	private CondBranchCat condBranchCat = new CondBranchCat();
	private OtherCat otherCat = new OtherCat();
	
	private IUopCat UopCatDispatch (UopCategory category) {
		switch (category) {
		case Load:
			return loadCat;
		case Store:
			return storeCat;
		case UnCondBranch:
			return uncondBranchCat;
		case CondBranch:
			return condBranchCat;
		case Other:
			return otherCat;

		// Here, the compiler screams without the default, while it should detect that it's dead code.
		default:
			return loadCat;
		}
	}
	
	private IUopCat uopCatDispatchLong(long i) {
		// Tendentiously awesome
		for (UopCategory cat : UopCategory.values())
			if (UopCatDispatch(cat).toInt() == i)
				return UopCatDispatch(cat);

		throw new IllegalArgumentException("The integer given is not the code of a Uop category.");
	}
	
	private IUopCat FindCategory (Uop uop) {
		UopCategory cat = UopCategory.Other;
		
		if (uop.mem == MemoryOp.Load)
			cat = UopCategory.Load;
		else if (uop.mem == MemoryOp.Store)
			cat = UopCategory.Store;
		else if (uop.targetAddressTakenBranch != 0) {
			 if (uop.flags == Flags.IgnoreFlags)
				 cat = UopCategory.UnCondBranch;
			 else if (uop.flags == Flags.ReadFlags)
				 cat = UopCategory.CondBranch;
		}
		
		return UopCatDispatch(cat);
	}

    public TraceAnalyzer() {
		super();
		
		// Admittedly a little hacky
		uopCatCount = new int [UopCategory.values().length]; // Init'd to 0 according to the java ref
		
		// Let's make our life easy and use an unnecessary additional cell;
		// that way, the number of insns of size s is stored at a[s], *not* a[s-1]
		insnsSizeCount = new int [33];
		branchTgtSizeCount = new int [33];
		
		System.out.println("XXX TODO XXX: name");
	}

	@Override
    public String author() {
        return ""; // TODO
    }
	
	// FIXME: [cf stackoverflow] using FP for log2 on integers can return erroneous results
	private double log2 (long n) {
		return Math.log(n) / Math.log(2);
	}

    @Override
    public void run(Iterable<T> uiter) {
        for (Uop uop : uiter) {
        	IUopCat c = FindCategory(uop);
        	// Update the categories count
        	uopCatCount[c.toInt()]++;
        	
        	// Statistics with respect to macro-ops (have to be computed on the first uop of the macro-op only)
        	if (uop.uopId == 1) {
        		// Compute the size
            	int inssize = (int) (uop.fallthroughPC - uop.instructionAddress);
            	insnsSizeCount[inssize]++;
        	}
        	
        	// The target PC in less than n bits thing is computed uops-wise
        	if (uop.targetAddressTakenBranch != 0) {
        		int sz = 2 + (int) (Math.floor(log2(Math.abs
        				(/* InstructionPC*/ uop.instructionAddress - /* TargetPC */ uop.targetAddressTakenBranch))));
        		branchTgtSizeCount[sz]++;
        		
        	}
        }
    }

    @Override
    public double avgInsnSize() {
    	long total = 0;
    	long count = 0;
    	
    	for (int sz = 1; sz < insnsSizeCount.length; sz++) {
    		total += sz * insnsSizeCount[sz];
    		count += insnsSizeCount[sz];
    	}
    	
    	// Ok, that's not really an illegal argument (not directly), but at least
    	// we don't have to declare that we can throw this exception
    	if (count == 0)
    		throw new IllegalArgumentException("No instruction processed - cannot compute mean.");
    	
    	// The conversion to double is made only at the end, in case it matters
    	// for precision (although it shouldn't at this level)
    	return ((double) total) / ((double) count);
    }

    @Override
    public double fractionOfBranchTargetsLteNBits(int bits) {
    	long insLteNCount = 0;
    	long totalInsCount = 0;
    	
    	if (bits <= 0 || bits > 32)
    		throw new IllegalArgumentException("Parameter <bytes> should be in the range [1, 32]");
    	
    	for (int sz = 1; sz <= bits; sz++)
    		insLteNCount += branchTgtSizeCount[sz];
    	
    	totalInsCount += insLteNCount;
    	
    	for (int sz = bits + 1; sz < branchTgtSizeCount.length; sz++)
    		totalInsCount += branchTgtSizeCount[sz];
    	
    	// Same remark as before
    	if (totalInsCount == 0)
    		throw new IllegalArgumentException("No branch target processed - cannot compute fraction.");
    	
        return ((double) insLteNCount) / ((double) totalInsCount);
    }

    @Override
    public double fractionOfInsnsLteNBytes(int bytes) {
    	long insLteNCount = 0;
    	long totalInsCount = 0;
    	
    	if (bytes == 0 || bytes > 32)
    		throw new IllegalArgumentException("Parameter <bytes> should be in the range [1, 32]");
    	
    	for (int sz = 1; sz <= bytes; sz++)
    		insLteNCount += insnsSizeCount[sz];
    	
    	totalInsCount += insLteNCount;
    	
    	for (int sz = bytes + 1; sz < insnsSizeCount.length; sz++)
    		totalInsCount += insnsSizeCount[sz];
    	
    	// Same remark as before
    	if (totalInsCount == 0)
    		throw new IllegalArgumentException("No instruction processed - cannot compute fraction.");
    	
        return ((double) insLteNCount) / ((double) totalInsCount);
    }

    @Override
    public String mostCommonUopCategory() {
    	long max = 0, argmax = 0;
    	
    	for (int i = 0; i < uopCatCount.length; i++)
    		if (uopCatCount[i] > max) {
    			max = uopCatCount[i];
    			argmax = i;
    		}
        return uopCatDispatchLong(argmax).toString();
    }

}
