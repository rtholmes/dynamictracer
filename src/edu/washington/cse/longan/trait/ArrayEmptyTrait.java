package edu.washington.cse.longan.trait;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

import edu.washington.cse.longan.io.ILonganIO;

public class ArrayEmptyTrait extends AbstractTrait {

	private Logger _log = Logger.getLogger(this.getClass());

	public static String ID = "ArrayEmptyTrait";

	@Override
	public String getDescription() {
		return getName();
	}

	@Override
	public String getName() {
		return ID;
	}

	@Override
	public String toString() {

		int empty = getData().count(DATA_KINDS.EMPTY);
		int notEmpty = getData().count(DATA_KINDS.NOT_EMPTY);

		String ret = "ArrayEmpty - Total: " + getData().size() + " Empty" + ": " + empty + " not empty: " + notEmpty
				+ ". ";

		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void track(Object obj) {
		if (obj != null) {

			// TODO: track primitive arrays
			// BUG: crashes with primitive arrays
			boolean isArray = obj.getClass().isArray();
			boolean isPrim = obj.getClass().isPrimitive();
			int size = Arrays.asList(obj).size();
			boolean isNull = (obj == null);
			String name = obj.getClass().getName();
			String compKind = obj.getClass().getComponentType().toString();

			int i = -1;

			if (obj instanceof Object[]) {
				i = ((Object[]) obj).length;
			} else { // catch (ClassCastException cce) {

				Class c = obj.getClass();
				Class cType = c.getComponentType();

				if (cType.equals(Integer.TYPE)) {
					i = ((int[]) obj).length;
				} else if (cType.equals(Long.TYPE)) {
					i = ((long[]) obj).length;
				} else if (cType.equals(Float.TYPE)) {
					i = ((float[]) obj).length;
				} else if (cType.equals(Double.TYPE)) {
					i = ((double[]) obj).length;
				} else if (cType.equals(Byte.TYPE)) {
					i = ((byte[]) obj).length;
				} else {
					_log.warn("Unhandled array kind: " + cType);
				}
			}

			// long[] arr = (long[])obj;
			// int al = arr.length;
			//			

			if (i == 0)
				getData().add(DATA_KINDS.EMPTY);
			else
				getData().add(DATA_KINDS.NOT_EMPTY);
		}

	}

//	@Override
//	public Element toXML() {
//		Element element = new Element(ILonganIO.TRAIT);
//		element.setAttribute(ILonganIO.KEY, getName());
//
//		int empty = getData().count(DATA_KINDS.EMPTY);
//		int notEmpty = getData().count(DATA_KINDS.NOT_EMPTY);
//
//		Element valueElement = new Element(ILonganIO.DATA);
//		valueElement.setAttribute(ILonganIO.KEY, DATA_KINDS.EMPTY + "");
//		valueElement.setAttribute(ILonganIO.DATA, empty + "");
//		element.addContent(valueElement);
//
//		valueElement = new Element(ILonganIO.DATA);
//		valueElement.setAttribute(ILonganIO.KEY, DATA_KINDS.NOT_EMPTY + "");
//		valueElement.setAttribute(ILonganIO.DATA, notEmpty + "");
//		element.addContent(valueElement);
//
//		return element;
//	}

	@SuppressWarnings("unchecked")
	public static ITrait parseXML(Element element) {
		if (element.getName().equals(ILonganIO.TRAIT) && element.getAttribute(ILonganIO.KEY).equals(ID)) {

			ArrayEmptyTrait trait = new ArrayEmptyTrait();

			int empty = -1;
			int notEmpty = -1;
			
			for (Element child : (List<Element>)element.getChildren()){
				if (child.getAttributeValue(ILonganIO.KEY).equals(DATA_KINDS.EMPTY.toString()))
					empty = Integer.parseInt(child.getAttributeValue(ILonganIO.VALUE));
				else if (child.getAttributeValue(ILonganIO.KEY).equals(DATA_KINDS.NOT_EMPTY.toString()))
					notEmpty = Integer.parseInt(child.getAttributeValue(ILonganIO.VALUE));
				else
					throw new AssertionError("Unhanlded key: "+child.getAttributeValue(ILonganIO.KEY));
			}
			
			trait.getData().setCount(DATA_KINDS.EMPTY, empty);
			trait.getData().setCount(DATA_KINDS.NOT_EMPTY, notEmpty);

			return trait;
		} else {
			throw new AssertionError("Calling parseXML with the wrong element for " + ID);
		}
	}
}
