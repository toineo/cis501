package cis501.logic;

import java.util.Set;

public interface FiniteTotalOrder<T> {
	public FiniteType<T> isFinite ();
	
	public T minimum ();
	
	public T minimumOf (Set<T> subset);
	
	public T successor (T el) throws IsMaximumException;
	
	// If r is the result, represents t1 is r than/to t2
	public TotalOrderComp order (T t1,T t2);

}
