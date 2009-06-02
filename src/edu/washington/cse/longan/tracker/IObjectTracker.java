package edu.washington.cse.longan.tracker;

public interface IObjectTracker extends Cloneable {
	
	public void track(Object obj);

	public IObjectTracker clone() throws CloneNotSupportedException;
	
	public String toString();
}
