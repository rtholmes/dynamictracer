package edu.washington.cse.longan.trait;

import java.util.Stack;

import org.jdom.Element;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.io.ILonganIO;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.Session;

public class ExceptionTrait extends AbstractTrait {

	private Stack<Integer> _callStack;

	private boolean _throws;

	private boolean _rethrows;

	private boolean _catches;

	private String _stringRep;

	private String _exceptionType;

	private String _exceptionMessage;

	public static String ID = "ExceptionTrait";

	/**
	 * 
	 * @param stack
	 *            call stack when the exception was raised
	 * @param exceptionObject
	 * @param isThrowing
	 *            XXX can this param go away? if an element isn't at the head of the call stack, obviously they're not
	 *            the originator. might need it after all; the stack isn't going to be constant, it's going to be
	 *            popping all of the time. (unless we keep an 'exception' stack around (might not be a bad idea)).
	 * @param isCatching
	 *            is the exception being caught here?
	 */
	@SuppressWarnings("unchecked")
	public void init(Stack<Integer> stack, String exceptionType, String exceptionMessage, boolean isThrowing, boolean isRethrowing, boolean isCatching) {
		Preconditions.checkArgument(_stringRep == null);
		Preconditions.checkArgument(_callStack == null);

		_callStack = (Stack<Integer>) stack.clone();
		_exceptionType = exceptionType;
		_exceptionMessage = exceptionMessage;
		_throws = isThrowing;
		_rethrows = isRethrowing;
		_catches = isCatching;

		// PERFORMANCE: including the exception message in these makes for larger traces.
		_exceptionMessage = ILonganConstants.ELIDED_STRING;

		String tmp = "";
		for (Integer i : _callStack) {
			tmp += i.toString() + ILonganConstants.SEPARATOR;
		}
		tmp += _exceptionType + ILonganConstants.SEPARATOR + _exceptionMessage + ILonganConstants.SEPARATOR;
		tmp += _throws + ILonganConstants.SEPARATOR + _rethrows + ILonganConstants.SEPARATOR + _catches;

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

	public Stack<Integer> getCallStack() {
		return _callStack;
	}

	@SuppressWarnings("unchecked")
	public Stack<String> getCallStackNames(Session session) {

		Stack<String> callStackNames = new Stack<String>();
		Stack<Integer> cs = (Stack<Integer>) _callStack.clone();

		for (Integer id : cs) {
			callStackNames.add(0, session.getElementNameForID(id));
		}

		return callStackNames;
	}

	public boolean getThrows() {
		return _throws;
	}

	public boolean getReThrows() {
		return _rethrows;
	}

	public boolean getCatches() {
		return _catches;
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

	public static boolean equals(ExceptionTrait tA, ExceptionTrait tB, Session sA, Session sB) {

		if (tA.getCatches() == tB.getCatches() && tA.getReThrows() == tB.getReThrows() && tA.getThrows() == tB.getThrows()) {
			boolean equivStacks = tA.getCallStackNames(sA).equals(tB.getCallStackNames(sB));
			return equivStacks;
		}

		return false;
	}
}
