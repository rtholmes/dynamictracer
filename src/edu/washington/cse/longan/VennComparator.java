package edu.washington.cse.longan;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import ca.lsmr.common.log.LSMRLogger;
import ca.lsmr.common.util.TimeUtility;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import edu.washington.cse.longan.io.SessionXMLReader;
import edu.washington.cse.longan.io.SessionXMLWriter;
import edu.washington.cse.longan.model.AbstractElement;
import edu.washington.cse.longan.model.ILonganConstants;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

public class VennComparator {
	Logger _log = Logger.getLogger(this.getClass());
	private long _start;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// LOGGING
		LSMRLogger.startLog4J(false, ILonganConstantsPriv.LOGGING_LEVEL);

		long start = System.currentTimeMillis();

		String path = ILonganConstants.OUTPUT_PATH;

		// String staticAPath = path + "longAnTestC-1_static.xml";
		// String staticBPath = path + "longAnTestC-2_static.xml";
		// String dynamicAPath = path + "longAnTestC-1_dynamic-4.xml";
		// String dynamicBPath = path + "longAnTestC-2_dynamic-4.xml";

		// String staticAPath = path + "longAnTestObserver-1a_static.xml";
		// String staticBPath = path + "longAnTestObserver-2a_static.xml";
		// String dynamicAPath = path + "longAnTestObserver-1b_dynamic.xml";
		// String dynamicBPath = path + "longAnTestObserver-2b_dynamic.xml";

		// String staticAPath = path + "jodaTime_1371_staticA.xml";
		// String staticBPath = path + "jodaTime_1374_staticA.xml";
		// String dynamicAPath = path + "jodaTime_1371_dynamicA.xml";
		// String dynamicBPath = path + "jodaTime_1374_dynamicA.xml";

		String staticAPath = path + "jodaTime_1380_staticB.xml";
		String staticBPath = path + "jodaTime_1381_staticB.xml";
		String dynamicAPath = path + "jodaTime_1380_dynamicA.xml";
		String dynamicBPath = path + "jodaTime_1381_dynamicA.xml";

		// String staticAPath = path + "jodaTime_1381_staticB.xml";
		// String staticBPath = path + "jodaTime_1388_staticB.xml";
		// String dynamicAPath = path + "jodaTime_1381_dynamicA.xml";
		// String dynamicBPath = path + "jodaTime_1388_dynamicA.xml";

		DataProvider provider = new DataProvider(staticAPath, staticBPath, dynamicAPath, dynamicBPath);

		VennComparator ec = new VennComparator();

		ec.run(provider);

	}

	ExecutionDelta v1sPrime;
	ExecutionDelta v2sPrime;
	ExecutionDelta v1dPrime;
	ExecutionDelta v2dPrime;

	ExecutionDelta r1;
	ExecutionDelta r2;
	ExecutionDelta r3;
	ExecutionDelta r4;
	ExecutionDelta r5;
	ExecutionDelta r6;
	ExecutionDelta r7;
	ExecutionDelta r8;
	ExecutionDelta r9;
	ExecutionDelta r10;
	ExecutionDelta r11;
	ExecutionDelta r12;
	ExecutionDelta r13;
	ExecutionDelta r14;
	ExecutionDelta r15;

	ExecutionDelta staticOnlyNew;
	ExecutionDelta staticOnlyOld;
	ExecutionDelta staticOnlyCommon;
	ExecutionDelta dynamicOnlyNew;
	ExecutionDelta dynamicOnlyOld;
	ExecutionDelta dynamicOnlyCommon;

	public ComparatorResult run(DataProvider provider) {
		start();

		_log.debug("DPC.run()");

		ExecutionDelta v1s = convertSessionToExecutionDelta(provider.getStaticA());
		ExecutionDelta v1d = convertSessionToExecutionDelta(provider.getDynamicA());
		ExecutionDelta v2s = convertSessionToExecutionDelta(provider.getStaticB());
		ExecutionDelta v2d = convertSessionToExecutionDelta(provider.getDynamicB());

		_log.info("static1; elements: " + v1s.getElements().size() + " paths: " + v1s.getPaths().size());
		_log.info("static2; elements: " + v2s.getElements().size() + " paths:  " + v2s.getPaths().size());
		_log.info("dyanmic1; elements: " + v1d.getElements().size() + " paths: " + v1d.getPaths().size());
		_log.info("dyanmic2; elements: " + v2d.getElements().size() + " paths: " + v2d.getPaths().size());

		ExecutionDelta tmp = null;

		staticOnlyOld = difference(v1s, v2s);
		_log.info("v1s^~v2s; elements:  " + staticOnlyOld.getElements().size() + " paths: " + staticOnlyOld.getPaths().size());

		staticOnlyNew = difference(v2s, v1s);
		_log.info("v2s^~v1s; elements:  " + staticOnlyNew.getElements().size() + " paths: " + staticOnlyNew.getPaths().size());

		dynamicOnlyOld = difference(v1d, v2d);
		_log.info("v1d^~v2d; elements: " + dynamicOnlyOld.getElements().size() + " paths: " + dynamicOnlyOld.getPaths().size());

		dynamicOnlyNew = difference(v2d, v1d);
		_log.info("v2d^~v1d; elements: " + dynamicOnlyNew.getElements().size() + " paths: " + dynamicOnlyNew.getPaths().size());

		// V1s
		tmp = difference(v1s, v2s);
		tmp = difference(tmp, v1d);
		v1sPrime = difference(tmp, v2d);
		r1 = v1sPrime;
		_log.info("s- (V1S'); elements: " + v1sPrime.getElements().size() + " paths: " + v1sPrime.getPaths().size());

		tmp = null;
		tmp = difference(v2s, v1s);
		tmp = difference(tmp, v1d);
		v2sPrime = difference(tmp, v2d);
		r2 = v2sPrime;
		_log.info("s+ (V2S'); elements: " + v2sPrime.getElements().size() + " paths: " + v2sPrime.getPaths().size());

		tmp = null;
		tmp = difference(v1d, v2d);
		tmp = difference(tmp, v1s);
		v1dPrime = difference(tmp, v2s);
		r8 = v1dPrime;
		_log.info("d- (V1D'); elements: " + v1dPrime.getElements().size() + " paths: " + v1dPrime.getPaths().size());

		tmp = null;
		tmp = difference(v2d, v1d);
		tmp = difference(tmp, v1s);
		v2dPrime = difference(tmp, v2s);
		r4 = v2dPrime;
		_log.info("d+ (V2D'); elements: " + v2dPrime.getElements().size() + " paths: " + v2dPrime.getPaths().size());

		staticOnlyCommon = difference(v2s, staticOnlyOld);
		staticOnlyCommon = difference(staticOnlyCommon, staticOnlyNew);
		_log.info("v1s^v2s; elements:  " + staticOnlyCommon.getElements().size() + " paths: " + staticOnlyCommon.getPaths().size());

		dynamicOnlyCommon = difference(v2d, dynamicOnlyOld);
		dynamicOnlyCommon = difference(dynamicOnlyCommon, dynamicOnlyNew);
		_log.info("v1d^v2d; elements: " + dynamicOnlyCommon.getElements().size() + " paths: " + dynamicOnlyCommon.getPaths().size());

		_log.info("Named Partitions");

		_log.info("s-   (r1);   elements: " + r1.getElements().size() + " paths: " + r1.getPaths().size());

		_log.info("s+   (r2);   elements: " + r2.getElements().size() + " paths: " + r2.getPaths().size());

		tmp = null;
		tmp = difference(v1s, v2d);
		tmp = difference(tmp, v1d);
		r3 = difference(tmp, r1);
		_log.info("s    (r3);   elements: " + r3.getElements().size() + " paths: " + r3.getPaths().size());
		// printDetails(r3);

		_log.info("d+   (r4);   elements: " + r4.getElements().size() + " paths: " + r4.getPaths().size());

		tmp = null;
		tmp = difference(v1s, v2s);
		tmp = difference(tmp, v1d);
		r5 = difference(tmp, r1);
		_log.info("s-d+ (r5);   elements: " + r5.getElements().size() + " paths: " + r5.getPaths().size());
		// if (!r5.isEmpty()) {
		// printDetails(r5);
		// }

		tmp = null;
		tmp = difference(v2s, v1s);
		tmp = difference(tmp, v1d);
		r6 = difference(tmp, r2);
		_log.info("s+d+ (r6);   elements: " + r6.getElements().size() + " paths: " + r6.getPaths().size());
		// if (!r6.isEmpty()) {
		// printDetails(r6);
		// }

		tmp = null;
		tmp = difference(v1s, v1d);
		tmp = difference(tmp, r1);
		tmp = difference(tmp, r3);
		r7 = difference(tmp, r5);
		_log.info("sd+  (r7);   elements: " + r7.getElements().size() + " paths: " + r7.getPaths().size());
		// if (!r7.isEmpty()) {
		// printDetails(r7);
		// }

		_log.info("d-   (r8);   elements: " + r8.getElements().size() + " paths: " + r8.getPaths().size());
		tmp = null;
		tmp = difference(v1s, v2s);
		tmp = difference(tmp, v2d);
		r9 = difference(tmp, r1);
		_log.info("s-d- (r9);   elements: " + r9.getElements().size() + " paths: " + r9.getPaths().size());
		// if (!r9.isEmpty()) {
		// printDetails(r9);
		// }

		tmp = null;
		tmp = difference(v2s, v1s);
		tmp = difference(tmp, v2d);
		r10 = difference(tmp, r2);
		_log.info("s+d- (r10);  elements: " + r10.getElements().size() + " paths: " + r10.getPaths().size());
		// if (!r10.isEmpty()) {
		// printDetails(r10);
		// }

		tmp = null;
		tmp = difference(v1s, v2d);
		tmp = difference(tmp, r1);
		tmp = difference(tmp, r3);
		r11 = difference(tmp, r9);
		_log.info("sd-  (r11);  elements: " + r11.getElements().size() + " paths: " + r11.getPaths().size());
		// if (!r11.isEmpty()) {
		// printDetails(r11);
		// }

		tmp = null;
		tmp = difference(v1d, r8);
		tmp = difference(tmp, v1s);
		r12 = difference(tmp, v2s);
		_log.info("d    (r12);  elements: " + r12.getElements().size() + " paths: " + r12.getPaths().size());
		// printDetails(r12);

		tmp = null;
		tmp = difference(v1s, v2s);
		tmp = difference(tmp, r9);
		tmp = difference(tmp, r5);
		r13 = difference(tmp, r1);
		_log.info("s-d  (r13);  elements: " + r13.getElements().size() + " paths: " + r13.getPaths().size());
		// if (!r13.isEmpty()) {
		// printDetails(r13);
		// }

		tmp = null;
		tmp = difference(v2s, v1s);
		tmp = difference(tmp, r10);
		tmp = difference(tmp, r2);
		r14 = difference(tmp, r6);
		_log.info("s+d  (r14);  elements: " + r14.getElements().size() + " paths: " + r14.getPaths().size());
		// if (!r14.isEmpty()) {
		// printDetails(r14);
		// }

		tmp = null;
		tmp = difference(v1s, r1);
		tmp = difference(tmp, r3);
		tmp = difference(tmp, r7);
		tmp = difference(tmp, r5);
		tmp = difference(tmp, r9);
		tmp = difference(tmp, r11);
		r15 = difference(tmp, r13);
		_log.info("sd   (r15);  elements: " + r15.getElements().size() + " paths: " + r15.getPaths().size());

		// _log.info("V1S'");
		// printDetails(v1sPrime);
		//
		// _log.info("V2S'");
		// printDetails(v2sPrime);
		//
		// _log.info("V1D'");
		// printDetails(v1dPrime);
		//
		// _log.info("V2D'");
		// printDetails(v2dPrime);

		_log.info("Unlikely Partitions");

		_log.info("s-d+ (r5);   elements: " + r5.getElements().size() + " paths: " + r5.getPaths().size());
		_log.info("s+d- (r10);  elements: " + r10.getElements().size() + " paths: " + r10.getPaths().size());
		_log.info("s-d  (r13);  elements: " + r13.getElements().size() + " paths: " + r13.getPaths().size());
		_log.info("s+d  (r14);  elements: " + r14.getElements().size() + " paths: " + r14.getPaths().size());

		_log.info("Inconsistent Partitions");

		_log.info("d+   (r4);   elements: " + r4.getElements().size() + " paths: " + r4.getPaths().size());
		// printDetails(r4);
		_log.info("d-   (r8);   elements: " + r8.getElements().size() + " paths: " + r8.getPaths().size());
		// printDetails(r8);
		_log.info("sd+  (r7);   elements: " + r7.getElements().size() + " paths: " + r7.getPaths().size());
		// printDetails(r7);
		_log.info("sd-  (r11);  elements: " + r11.getElements().size() + " paths: " + r11.getPaths().size());
		// printDetails(r11);

		ComparatorResult cr = new ComparatorResult.Builder().v1s(v1s).v2s(v2s).v1d(v1d).v2d(v2d).r1(r1).r2(r2).r3(r3).r4(r4).r5(r5).r6(r6).r7(r7)
				.r8(r8).r9(r9).r10(r10).r11(r11).r12(r12).r13(r13).r14(r14).r15(r15).static1less2(staticOnlyOld).static2less1(staticOnlyNew)
				.dynamic1less2(dynamicOnlyOld).dynamic2less1(dynamicOnlyNew).build();

		_log.info("Set details");

		_log.info("v1d^~v2d details (dynamic only; deleted elements / edges):");
		// printDetails(dynamicOnlyOld);

		_log.info("V1D' details (combined; deleted elements that were never run):");
		// printDetails(v1dPrime);

		_log.info("v2d^~v1d details (dynamic only; new elements / edges):");
		// printDetails(dynamicOnlyNew);

		_log.info("V2D' details (combined; new elements that aren't statically obvious):");
		// printDetails(v2dPrime);

		_log.info("v2s^~v1s details (static only; new elements / edges):");
		// printDetails(staticOnlyNew);

		_log.info("V2S' details (combined; new elements that aren't dynamically run):");
		// printDetails(v2sPrime);

		_log.info("v1s^~v2s details (static only; deleted elements / edges):");
		// printDetails(staticOnlyOld);

		_log.info("V1S' details (combined; deleted elements that were never run):");
		// printDetails(v1sPrime);

		done(_start);

		return cr;
	}

	private void printHTMLResults(ExecutionDelta delta, PrintStream out) {

		out.println("<pre>");
		if (delta.getPaths().size() > 0) {

			Vector<Path> paths = new Vector<Path>();
			paths.addAll(delta.getPaths());
			Collections.sort(paths, new Comparator<Path>() {

				@Override
				public int compare(Path o1, Path o2) {
					return o1.toString().compareTo(o2.toString());
				}

			});

			for (Path p : paths) {
				out.println("  " + p.toString());
			}
		} else {
			out.println("--EMPTY PARTITION--");
		}
		out.println("</pre>");
	}

	private void printDetails(ExecutionDelta ed) {
		if (true) {
			Vector<String> elements = new Vector<String>(ed.getElements());
			Vector<Path> paths = new Vector<Path>(ed.getPaths());

			Collections.sort(elements);
			Collections.sort(paths, new Comparator<Path>() {
				public int compare(Path p1, Path p2) {
					return p1.toString().compareTo(p2.toString());
				}
			});

			if (paths.size() < 1) {
				for (String element : elements)
					_log.info("\telement: " + element);
			}

			for (Path path : paths)
				_log.info("\tpath: " + path);

			// if (elements.size() == 0 && paths.size() == 0)
			// _log.info("\t--empty set--");
		}
	}

	private ExecutionDelta difference(ExecutionDelta delta1, ExecutionDelta delta2) {
		Set<String> elements = Sets.difference(delta1.getElements(), delta2.getElements());
		Set<Path> paths = Sets.difference(delta1.getPaths(), delta2.getPaths());

		ExecutionDelta ed = new ExecutionDelta(elements, paths);
		return ed;
	}

	private void start() {
		_start = System.currentTimeMillis();
	}

	private void done(long start) {
		_log.info("Done in: " + TimeUtility.msToHumanReadableDelta(start));
	}

	private void readAndWrite(String fName) {
		_log.info("Read and Write");
		SessionXMLReader sxmlr = new SessionXMLReader();
		Session sess = sxmlr.readXML(fName);
		SessionXMLWriter sxmlw = new SessionXMLWriter();
		sxmlw.write(fName + "_out.xml", sess);
	}

	private ExecutionDelta convertSessionToExecutionDelta(Session sA) {
		Preconditions.checkNotNull(sA, "Session A is null");

		_log.debug("Extracting: " + sA.getSessionName());// + " to: " + sB.getSessionName());

		ExecutionDelta ed = new ExecutionDelta(new HashSet<String>(), new HashSet<Path>());

		extractElements(sA, ed);
		extractPaths(sA, ed);

		return ed;
	}

	// private void checkFieldDifferences(Session sA, Session sB) {
	// ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getFieldNames());
	// ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getFieldNames());
	//
	// if (_outputIndividual) {
	// _log.info("CHECK FIELD GET DIFFERENCES");
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// _log.debug("common field: " + eName);
	//
	// FieldElement fA = sA.getField(sA.getIdForElement(eName));
	// FieldElement fB = sB.getField(sB.getIdForElement(eName));
	//
	// FieldTraitContainer ftgA = fA.getFieldGetTraitContainer();
	// FieldTraitContainer ftgB = fB.getFieldGetTraitContainer();
	//
	// _log.debug("\tA gets: " + fA.getGetBy().size() + "\tsets: " + fA.getSetBy().size());
	// _log.debug("\tB gets: " + fB.getGetBy().size() + "\tsets: " + fB.getSetBy().size());
	//
	// for (int getById : fA.getGetBy().elementSet()) {
	// String elemName = sA.getElementNameForID(getById);
	//
	// ITrait traitsA[] = null;
	// ITrait traitsB[] = null;
	//
	// traitsA = ftgA.getTraitsForCaller(getById);
	// if (sB.hasIDForElement(elemName))
	// traitsB = ftgB.getTraitsForCaller(sB.getIdForElement(elemName));
	//
	// // compare trait arrays
	//
	// compareTraitDifferences(traitsA, traitsB, fA.getName() + " referenced by: " + elemName);
	// }
	//
	// for (int getById : fB.getGetBy().elementSet()) {
	// String elemName = sB.getElementNameForID(getById);
	//
	// ITrait traitsB[] = null;
	// ITrait traitsA[] = null;
	//
	// traitsB = ftgB.getTraitsForCaller(getById);
	// if (sA.hasIDForElement(elemName))
	// traitsA = ftgA.getTraitsForCaller(sA.getIdForElement(elemName));
	//
	// // compare trait arrays
	// compareTraitDifferences(traitsA, traitsB, fB.getName() + " referenced by: " + elemName);
	// }
	//
	// }
	//
	// // Iterates through elements that exist in A but not in B
	// // e.g., this gives an indication of new elements in B
	// for (String eName : Sets.difference(eAnames, eBnames)) {
	//
	// }
	//
	// // Iterates through elements that exist in B but not in C
	// // e.g., this gives an indication of elements in A that
	// // are no longer present in B
	// for (String eName : Sets.difference(eBnames, eAnames)) {
	//
	// }
	// }
	//
	// if (_outputIndividual) {
	// _log.info("CHECK FIELD SET DIFFERENCES");
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// _log.debug("common field: " + eName);
	//
	// FieldElement fA = sA.getField(sA.getIdForElement(eName));
	// FieldElement fB = sB.getField(sB.getIdForElement(eName));
	//
	// FieldTraitContainer ftgA = fA.getFieldSetTraitContainer();
	// FieldTraitContainer ftgB = fB.getFieldSetTraitContainer();
	//
	// _log.debug("\tgets: " + fA.getGetBy().size() + "\tsets: " + fA.getSetBy().size());
	// for (int setById : fA.getSetBy().elementSet()) {
	// String elemName = sA.getElementNameForID(setById);
	//
	// ITrait traitsA[] = null;
	// ITrait traitsB[] = null;
	//
	// traitsA = ftgA.getTraitsForCaller(setById);
	// if (sB.hasIDForElement(elemName))
	// traitsB = ftgB.getTraitsForCaller(sB.getIdForElement(elemName));
	//
	// // compare trait arrays
	// compareTraitDifferences(traitsA, traitsB, fA.getName() + " referenced by: " + elemName);
	// }
	//
	// for (int setById : fB.getSetBy().elementSet()) {
	// String elemName = sB.getElementNameForID(setById);
	//
	// ITrait traitsB[] = null;
	// ITrait traitsA[] = null;
	//
	// traitsB = ftgB.getTraitsForCaller(setById);
	// if (sA.hasIDForElement(elemName))
	// traitsA = ftgA.getTraitsForCaller(sA.getIdForElement(elemName));
	//
	// // compare trait arrays
	// compareTraitDifferences(traitsA, traitsB, fB.getName() + " referenced by: " + elemName);
	// }
	//
	// }
	// }
	//
	// if (_outputCombined) {
	// _log.info("CHECK COMBINED FIELD GET DIFFERENCES");
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	// _log.debug("common field: " + eName);
	//
	// FieldElement fA = sA.getField(sA.getIdForElement(eName));
	// FieldElement fB = sB.getField(sB.getIdForElement(eName));
	//
	// FieldTraitContainer ftgA = fA.getFieldGetTraitContainer();
	// FieldTraitContainer ftgB = fB.getFieldGetTraitContainer();
	//
	// compareTraitDifferences(ftgA.getTraitsCollapsed(), ftgB.getTraitsCollapsed(), fA.getName());
	// }
	// }
	//
	// if (_outputCombined) {
	// _log.info("CHECK COMBINED FIELD SET DIFFERENCES");
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	// _log.debug("common field: " + eName);
	//
	// FieldElement fA = sA.getField(sA.getIdForElement(eName));
	// FieldElement fB = sB.getField(sB.getIdForElement(eName));
	//
	// FieldTraitContainer ftgA = fA.getFieldSetTraitContainer();
	// FieldTraitContainer ftgB = fB.getFieldSetTraitContainer();
	//
	// compareTraitDifferences(ftgA.getTraitsCollapsed(), ftgB.getTraitsCollapsed(), fA.getName());
	// }
	// }
	// }
	//
	// private void checkExceptionDifferences(Session sA, Session sB) {
	// _log.info("CHECK EXCEPTION DIFFERENCES");
	//
	// ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
	// ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());
	//
	// // we can be a lot more precise here; eg., we could note exceptions that
	// // have different sources (rather than whole paths) or are caught in different locations
	// for (String eName : Sets.intersection(eAnames, eBnames)) {
	//
	// MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
	// MethodElement mB = sB.getMethod(sB.getIdForElement(eName));
	//
	// for (ExceptionTrait etA : mA.getExceptions()) {
	// boolean matched = false;
	// for (ExceptionTrait etB : mB.getExceptions()) {
	// if (ExceptionTrait.equals(etA, etB, sA, sB)) {
	// matched = true;
	// }
	// }
	// if (matched) {
	//
	// } else {
	// if (_outputMissing)
	// _log.info("Exception disappeared from: " + mA + " ex: " + etA);
	// }
	// }
	//
	// for (ExceptionTrait etB : mB.getExceptions()) {
	// boolean matched = false;
	// for (ExceptionTrait etA : mA.getExceptions()) {
	// if (ExceptionTrait.equals(etA, etB, sA, sB)) {
	// matched = true;
	// }
	// }
	// if (matched) {
	//
	// } else {
	// if (_outputAdded)
	// _log.info("Exception added to: " + mB + " ex: " + etB);
	// }
	// }
	//
	// }
	//
	// // Iterates through elements that exist in A but not in B
	// // e.g., this gives an indication of new elements in B
	// for (String eName : Sets.difference(eAnames, eBnames)) {
	//
	// }
	//
	// // Iterates through elements that exist in B but not in C
	// // e.g., this gives an indication of elements in A that
	// // are no longer present in B
	// for (String eName : Sets.difference(eBnames, eAnames)) {
	//
	// }
	//
	// }
	//
	private void checkTotalMethodInvocationCounts(Session sA, Session sB) {
		_log.info("CHECK INVOCATION COUNTS");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());

		for (String eName : Sets.intersection(eAnames, eBnames)) {

			MethodElement mA = sA.getMethod(sA.getIdForElement(eName));
			MethodElement mB = sB.getMethod(sB.getIdForElement(eName));

			int aInvokationTotal = 0;
			int bInvokationTotal = 0;

			Multiset<Integer> calledBys;

			calledBys = mA.getCalledBy();
			for (int calledBy : calledBys.elementSet()) {
				aInvokationTotal += calledBys.count(calledBy);
			}

			calledBys = mB.getCalledBy();
			for (int calledBy : calledBys.elementSet()) {
				bInvokationTotal += calledBys.count(calledBy);
			}

			if (aInvokationTotal != bInvokationTotal) {
				// if (_outputInvocationDifferences)
				_log.warn("Invocation differences ( " + aInvokationTotal + " -> " + bInvokationTotal + " ) for: " + mA.getName());
			}
		}

		// Iterates through elements that exist in A but not in B
		// e.g., this gives an indication of new elements in B
		for (String eName : Sets.difference(eAnames, eBnames)) {

		}

		// Iterates through elements that exist in B but not in C
		// e.g., this gives an indication of elements in A that
		// are no longer present in B
		for (String eName : Sets.difference(eBnames, eAnames)) {

		}

	}

	private Set<String> extractNames(Set<AbstractElement> elements) {
		HashSet<String> set = new HashSet<String>();
		for (AbstractElement ae : elements)
			set.add(ae.getName());

		return set;
	}

	private void checkForMissingPaths(Session sA, Session sB, ExecutionDelta ed) {

		_log.info("CHECK FOR MISSING PATHS");

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());
		ImmutableSet<String> eBnames = ImmutableSet.copyOf(sB.getMethodNames());

		ImmutableSet<String> interNames = Sets.intersection(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> missingNames = Sets.difference(eAnames, eBnames).immutableCopy();
		ImmutableSet<String> newNames = Sets.difference(eBnames, eAnames).immutableCopy();

		for (String eName : interNames) {

			MethodElement mA = sA.getMethodForName(eName);
			MethodElement mB = sB.getMethodForName(eName);

			Set<AbstractElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			Set<AbstractElement> mBCBs = sB.getElementSet(mB.getCalledBy().elementSet());

			Set<String> mACBnames = extractNames(mACBs);
			Set<String> mBCBnames = extractNames(mBCBs);

			ImmutableSet<String> interCBNames = Sets.intersection(mACBnames, mBCBnames).immutableCopy();
			ImmutableSet<String> missingCBNames = Sets.difference(mACBnames, mBCBnames).immutableCopy();
			ImmutableSet<String> newCBNames = Sets.difference(mBCBnames, mACBnames).immutableCopy();

			for (String cbName : interCBNames) {
				// common paths
			}

			for (String cbName : missingCBNames) {

				boolean missingTarget = _missingElementNames.contains(eName);
				boolean missingSource = _missingElementNames.contains(cbName);

				if (missingSource && missingTarget) {
					// if the source and target are missing nothing can happen
					_log.info("Path missing ( non-exersized source & target ) to: " + eName + " from: " + cbName);
				} else if (missingSource) {
					// if a source is missing, it isn't call is gone
					_log.info("Path missing ( non-exersized source ) to: " + eName + " from: " + cbName);
				} else if (missingTarget) {
					// if a target is missing, it isn't surprising that it can't be called
					_log.info("Path missing ( non-exersized target ) to: " + eName + " from: " + cbName);
				} else {
					Preconditions.checkArgument(!missingSource && !missingTarget);
					_log.warn("Path removed to: " + eName + " from: " + cbName);
				}
				// ed.removedPath(cbName, eName);
			}

			for (String cbName : newCBNames) {
				// RFE: report path counts for new paths?
			}

		}

		for (String eName : missingNames) {

			MethodElement mA = sA.getMethodForName(eName);
			// mB doesn't exist because these are new names
			MethodElement mB = null;

			Set<AbstractElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			// Set<AbstractElement> mBCBs = sB.getElementSet(mB.getCalledBy().elementSet());

			Set<String> mACBnames = extractNames(mACBs);
			// Set<String> mBCBnames = extractNames(mBCBs);

			for (String cbName : mACBnames) {
				if (sB.hasIDForElement(cbName)) {
					_log.info("Path missing (with existing source) from: " + cbName + " to: " + eName);
				} else {
					_log.info("Path missing (with missing source and target) from: " + cbName + " to: " + eName);
				}
				// ed.removedPath(cbName, eName);
			}

		}

	}

	ImmutableSet<String> _newElementNames = null;
	ImmutableSet<String> _missingElementNames = null;

	/**
	 * Retunrs all of the paths that are in sA that aren't in sB. Mainly used initially to compare against an empty session.
	 * 
	 * @param sA
	 * @param sB
	 * @param ed
	 */
	private void extractPaths(Session sA, ExecutionDelta ed) {

		_log.debug("CHECK FOR PATH DIFFERENCES");

		// ImmutableSet<Path> sAPaths = sA.getPaths();

		ImmutableSet<String> eAnames = ImmutableSet.copyOf(sA.getMethodNames());

		for (String eName : eAnames) {

			MethodElement mA = sA.getMethodForName(eName);
			Set<AbstractElement> mACBs = sA.getElementSet(mA.getCalledBy().elementSet());
			Set<String> mACBnames = extractNames(mACBs);

			for (String cbName : mACBnames) {
				String target = eName;
				String source = cbName;

				ed.addPath(source, target);
			}
		}
	}

	/**
	 * Essentially find any element in sessionA that isn't in sessionB. This is used mainly at the beginning comparing to an empty set, so it should
	 * be pretty easy.
	 * 
	 * @param sessionA
	 * @param sessionB
	 * @param ed
	 */
	private void extractElements(Session sessionA, ExecutionDelta ed) {

		_log.debug("CHECK FOR ELEMENT DIFFERENCES");

		Set<String> missingElements = sessionA.getElementNames();

		Vector<String> names = new Vector<String>();
		for (String elemName : missingElements) {
			names.add(elemName);

			ed.addElement(elemName);
		}
	}

	private void checkForNewElements(Session sessionA, Session sessionB, ExecutionDelta ed) {

		_log.info("CHECK FOR NEW ELEMENTS");

		Vector<String> newElements = new Vector<String>();

		ImmutableSet<String> newNames = Sets.difference(sessionB.getElementNames(), sessionA.getElementNames()).immutableCopy();
		_newElementNames = newNames;

		for (String elemName : _newElementNames) {
			newElements.add(elemName);
		}

		Collections.sort(newElements);
		for (String elem : newElements) {
			_log.warn("Session B adds new element: " + elem);
			// ed.addedElementhat'(elem);
		}
	}

	public void writeHTML(PrintStream out) {
		out.println("");
		out.println("");
		out.println("<div class='index'>");
		out.println("<ul>");
		out.println("<li><a href='#s+'>s+ (" + r2.getPaths().size() + ")</a></li>");
		out.println("<li><a href='#s-'>s- (" + r1.getPaths().size() + ")</a></li>");
		out.println("<li><a href='#s+d+'>s+d+ (" + r6.getPaths().size() + ")</a></li>");
		out.println("<li><a href='#s-d-'>s-d- (" + r9.getPaths().size() + ")</a></li>");
		out.println("<li><a href='#sd+'>sd+ (" + r7.getPaths().size() + ")</a></li>");
		out.println("<li><a href='#sd-'>sd- (" + r11.getPaths().size() + ")</a></li>");
		out.println("<li><a href='#d+'>d+ (" + r4.getPaths().size() + ")</a></li>");
		out.println("<li><a href='#d-'>d- (" + r8.getPaths().size() + ")</a></li>");
		out.println("</ul>");
		out.println("</div>");

		out.println("<div class='section'>");
		out.println("<div class='title'><a name='s+'/>s+</div>");
		printHTMLResults(r2, out);
		out.println("<div class='title'><a name='s-'/>s-</div>");
		printHTMLResults(r1, out);
		out.println("<div class='title'><a name='s+d+'/>s+d+</div>");
		printHTMLResults(r6, out);
		out.println("<div class='title'><a name='s-d-'/>s-d-</div>");
		printHTMLResults(r9, out);
		out.println("<div class='title'><a name='sd+'/>sd+</div>");
		printHTMLResults(r7, out);
		out.println("<div class='title'><a name='sd-'/>sd-</div>");
		printHTMLResults(r11, out);
		out.println("<div class='title'><a name='d+'/>d+</div>");
		printHTMLResults(r4, out);
		out.println("<div class='title'><a name='d-'/>d-</div>");
		printHTMLResults(r8, out);
		out.println("</div>");
		out.println("</body>");
		out.println("</html>");
		out.println("");
		out.println("");
	}

}

class DataProvider {
	Logger _log = Logger.getLogger(this.getClass());

	Session _staticA;
	Session _staticB;
	Session _dynamicA;
	Session _dynamicB;

	DataProvider(String staticAPath, String staticBPath, String dynamicAPath, String dynamicBPath) {
		try {
			_staticA = loadSession(staticAPath);
			_staticB = loadSession(staticBPath);
			_dynamicA = loadSession(dynamicAPath);
			_dynamicB = loadSession(dynamicBPath);
		} catch (Exception e) {
			_log.fatal(e);
		}
	}

	private Session loadSession(String path) {
		try {
			SessionXMLReader sxmlr = new SessionXMLReader();
			Session sess = sxmlr.readXML(path);
			return sess;
		} catch (Exception e) {
			_log.error("Error loading session file: " + path, e);
		}
		return null;
	}

	public Session getStaticA() {
		return _staticA;
	}

	public Session getStaticB() {
		return _staticB;
	}

	public Session getDynamicA() {
		return _dynamicA;
	}

	public Session getDynamicB() {
		return _dynamicB;
	}
}

class ExecutionDelta {

	private Set<String> _elements = new HashSet<String>();
	private Set<Path> _paths = new HashSet<Path>();

	ExecutionDelta(Set<String> elements, Set<Path> paths) {
		_paths = paths;
		_elements = elements;
	}

	public void addPath(String source, String target) {
		_paths.add(new Path(source, target));
	}

	public void addElement(String elementName) {
		_elements.add(elementName);
	}

	public Set<String> getElements() {
		return _elements;
	}

	public Set<Path> getPaths() {
		return _paths;
	}

	public int getPSize() {
		return _paths.size();
	}

	public boolean isEmpty() {
		if (getPaths().size() < 1 && getElements().size() < 1)
			return true;
		return false;
	}
}
