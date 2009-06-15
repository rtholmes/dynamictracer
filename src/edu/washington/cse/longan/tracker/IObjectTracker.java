package edu.washington.cse.longan.tracker;

import java.util.Collection;

import edu.washington.cse.longan.trait.ITrait;

public interface IObjectTracker extends Cloneable {

	public IObjectTracker clone() throws CloneNotSupportedException;

	/**
	 * 
	 * @return kind of tracker being used
	 */
	public String getTrackerName();
	
	/**
	 * 
	 * @return static type of the object.
	 */
	public String getStaticTypeName();
	
	/**
	 * 
	 * @return the name of the element (field name, param name). Does not apply to return types.
	 */
	public String getName();

	/**
	 * 
	 * @return the index of the element. Only applies to parameters.
	 */
	public int getPosition();

	public boolean isField();

	public boolean isParameter();

	public boolean isReturn();

	public String toString();

	/**
	 * Main method for trackers; keeps track of interesting properties for each instance.
	 * 
	 * @param obj element to be tracked.
	 */
	public void track(Object obj);

	public Collection<ITrait> getTraits();

}
