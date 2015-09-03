package cis501;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class UopIterator implements Iterator<Uop>, Iterable<Uop> {

    private final BufferedReader reader;
    private final int LIMIT;
    private final IUopFactory uopFactory;
    private int uopsProcessed = 0;

    /**
     * @param filename The path to the compressed trace file
     * @param limit    Stop after processing this many uops. If -1, process the entire trace.
     */
    public UopIterator(String filename, int limit, IUopFactory uopf) {
        if (-1 == limit) {
            LIMIT = Integer.MAX_VALUE; // no limit
        } else {
            LIMIT = limit;
        }

        uopFactory = uopf;

        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(filename)), "US-ASCII");
        } catch (IOException e) {
            e.printStackTrace();
        }
        reader = new BufferedReader(isr);
    }

    public boolean hasNext() {
        try {
            return uopsProcessed < LIMIT && reader.ready();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Uop next() {
        try {
            String ln = reader.readLine();
            uopsProcessed++;
            return uopFactory.create(ln);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Iterator<Uop> iterator() {
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
