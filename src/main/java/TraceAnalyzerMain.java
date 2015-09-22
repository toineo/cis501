import cis501.ITraceAnalyzer;
import cis501.Uop;
import cis501.UopIterator;
import cis501.submission.TraceAnalyzer;
import cis501.submission.UopFactory;

import java.io.IOException;

public class TraceAnalyzerMain {

    public static void main(String[] args) throws IOException {
        final int uopLimit;

        switch (args.length) {
            case 1:
                uopLimit = -1; // by default, run on entire trace
                break;
            case 2: // use user-provided limit
                uopLimit = Integer.parseInt(args[1]);
                break;
            default:
                System.err.println("Usage: path/to/trace-file [uop-limit]");
                return;
        }

        ITraceAnalyzer<Uop> ta = new TraceAnalyzer<>();
        UopIterator uiter = new UopIterator(args[0], uopLimit, new UopFactory());
        ta.run(uiter);
        System.out.println("Avg insn size is: " + ta.avgInsnSize());
    }

}
