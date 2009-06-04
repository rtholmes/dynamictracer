package edu.washington.cse.longan.trait;

import com.google.common.collect.Multiset;

public interface ITrait {

	public enum DATA_KINDS {
		IS_NULL,
		NOT_NULL,
		IS_ZERO,
		IS_NEGATIVE,
		IS_POSITIVE,
		IS_TRUE,
		IS_FALSE,
		TYPE,
		SIZE,
		EMPTY, 
		NOT_EMPTY
	}

	public String getName();
	
	public String getDescription();
	
	public Multiset<ITrait.DATA_KINDS> getData();

	public Multiset<String> getSupplementalData();
	
	public void track(Object obj);
}
