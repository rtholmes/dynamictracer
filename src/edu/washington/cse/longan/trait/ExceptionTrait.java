package edu.washington.cse.longan.trait;

import java.util.Stack;

import org.jdom.Element;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.io.ILonganIO;

public class ExceptionTrait extends AbstractTrait {

	private Stack<String> _callStack;

	private boolean _throws;

	private boolean _catches;

	private String _stringRep;

	public static String ID = "ExceptionTrait";

	/**
	 * 
	 * @param stack
	 *            call stack when the exception was raised
	 * @param isThrowing
	 *            XXX can this param go away? if an element isn't at the head of the call stack, obviously they're not
	 *            the originator. might need it after all; the stack isn't going to be constant, it's going to be
	 *            popping all of the time. (unless we keep an 'exception' stack around (might not be a bad idea)).
	 * @param isCatching
	 *            is the exception being caught here?
	 */
	@SuppressWarnings("unchecked")
	public void init(Stack<String> stack, boolean isThrowing, boolean isCatching) {
		Preconditions.checkArgument(_stringRep == null);
		Preconditions.checkArgument(_callStack == null);

		_callStack = (Stack<String>) stack.clone();
		_throws = isThrowing;
		_catches = isCatching;

		String tmp = "";
		for (String s : _callStack) {
			tmp += s + ":::";
		}
		tmp += _throws + ":::" + _catches;

		_stringRep = tmp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ExceptionTrait) {
			ExceptionTrait that = (ExceptionTrait) obj;
			return that.toString().equals(toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return _stringRep;
	}

	@Override
	public String getDescription() {
		return "What exceptions are thrown, caught, or pass through this element?";
	}

	@Override
	public String getName() {
		return ID;
	}

	@Override
	public void track(Object obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public Element toXML() {
		Element element = super.toXML();

		for (String kind : getSupplementalData().elementSet()) {
			Element valueElement = new Element(ILonganIO.SUPPLEMENTAL_DATA);
			valueElement.setAttribute(ILonganIO.KEY, kind + "");
			valueElement.setAttribute(ILonganIO.VALUE, getSupplementalData().count(kind) + "");
			element.addContent(valueElement);
		}

		return element;
	}

}
