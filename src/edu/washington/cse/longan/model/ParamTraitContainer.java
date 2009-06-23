package edu.washington.cse.longan.model;


public class ParamTraitContainer extends AbstractTraitContainer {

	private int _position;
	private String _name;


	public ParamTraitContainer(String name, String staticType, int position) {
		super(staticType,CONTAINER_KIND.PARAM);
		
		_name = name;
		_position = position;
	}
//
//	/**
//	 * @param caller
//	 * @param traits
//	 */
//	public void addTraits(int caller, ITrait[] traits) {
//		_traits.put(caller, traits);
//	}
//
//	public String getStaticTypeName() {
//		return _staticType;
//	}

	public String getName() {
		return _name;
	}

	public int getPosition() {
		return _position;
	}


	

}
