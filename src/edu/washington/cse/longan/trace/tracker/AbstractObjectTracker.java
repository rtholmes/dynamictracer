package edu.washington.cse.longan.trace.tracker;

import java.util.ArrayList;
import java.util.Collection;

import edu.washington.cse.longan.Logger;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.trait.ITrait;

public abstract class AbstractObjectTracker implements IObjectTracker {

	private boolean _isField = false;

	private boolean _isParameter = false;

	private boolean _isReturn = false;

	private String _staticTypeName = "";

	Logger _log = Logger.getLogger(this.getClass());
	private String _name = "";
	private int _position = -1;

	private ArrayList<ITrait> _traits = new ArrayList<ITrait>();

	@SuppressWarnings("unchecked")
	Class _clazz;

	/**
	 * Constructor for return type tracking.
	 * 
	 * @param clazz
	 */
	@SuppressWarnings("unchecked")
	public AbstractObjectTracker(Class clazz) {
		_clazz = clazz;
		_staticTypeName = clazz.getName();

		createTraits();

		_isReturn = true;
	}

	/**
	 * Constructor for parameter tracking.
	 * 
	 * @param clazz
	 * @param position
	 * @param name
	 */
	@SuppressWarnings("unchecked")
	public AbstractObjectTracker(Class clazz, int position, String name) {
		this(clazz);

		_isParameter = true;
		_isReturn = false;
		_name = name;
		_position = position;
		// _staticTypeName = clazz.getName();
		// createTraits();
	}

	/**
	 * Constructor for field tracking.
	 * 
	 * @param clazz
	 * @param name
	 */
	@SuppressWarnings("unchecked")
	public AbstractObjectTracker(Class clazz, String name) {
		this(clazz);
		_isField = true;
		_isReturn = false;
		_name = name;
		// _staticTypeName = clazz.getName();
		// createTraits();
	}

	public void addTrait(ITrait trait) {
		if (!_traits.contains(trait))
			_traits.add(trait);
	}

	/**
	 * Note: while the traits are cloned, their contents ARE NOT.
	 */
	public abstract IObjectTracker clone() throws CloneNotSupportedException;

	public abstract void createTraits();

	public String getName() {
		if (_isParameter || _isField) {
			return _name;
		} else {
			String msg = "Asking for the name of a return type doesn't make much sense.";
			_log.error(msg);
			throw new AssertionError(msg);
		}
	}

	public String getStaticTypeName() {
		return _staticTypeName;
	}

	public int getPosition() {
		if (_isParameter) {
			return _position;
		} else {
			String msg = "Asking for the position of a return type or field doesn't make much sense.";
			_log.error(msg);
			throw new AssertionError(msg);
		}
	}

	public Collection<ITrait> getTraits() {
		return _traits;
	}

	public boolean isField() {
		return _isField;
	}

	public boolean isParameter() {
		return _isParameter;
	}

	public boolean isReturn() {
		return _isReturn;
	}

	public void track(Object obj) {

		if (ILonganConstants.TRACK_TRAITS) {
			for (ITrait trait : getTraits()) {
				trait.track(obj);
			}
		}

	}

	@Override
	public String toString() {
		String ret = "";
		for (ITrait trait : getTraits())
			ret += trait.toString();

		return ret;
	}

	@SuppressWarnings("unchecked")
	protected Class getClazz() {
		return _clazz;
	}
}
