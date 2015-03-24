package il.ac.technion.ie.utils;

import il.ac.technion.ie.data.structure.Clearer;

import java.io.Serializable;


public interface ClearerFactory extends Serializable {

	public Clearer createInstance();
}
