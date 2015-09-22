package cis501.logic;

// Type of the witnesses of T being finite
public interface FiniteType<T> {
	// We return an array instead of a set in order to represent finiteness.
	// Indeed, the Set interface should allow for finitely representable sets,
	// thereby breaking the property we are trying to represent.
	// (java.util.set currently doesn't support such sets, but that's a 
	// different matter).
	public T[] elements ();
	// In a dependently typed language, we would require the following types
	// to be populated (exhaustivity and injectivity) :
	// forall t : T, exists n : int, elements()[n] = t
	// forall n1 n2 : int, let el = elements() in el[n1] = el[n2] -> n1 = n2
}
