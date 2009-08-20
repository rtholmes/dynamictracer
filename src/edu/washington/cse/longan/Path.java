package edu.washington.cse.longan;

import com.google.common.base.Preconditions;

import edu.washington.cse.longan.model.AbstractElement;
import edu.washington.cse.longan.model.FieldElement;
import edu.washington.cse.longan.model.MethodElement;
import edu.washington.cse.longan.model.Session;

public class Path {
	final String _source;
	final String _target;
	final String _sig;

	Path(String source, String target) {
		Preconditions.checkNotNull(source);
		Preconditions.checkNotNull(target);
		_source = source;
		_target = target;
		_sig = source + " -> " + target;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Path)
			return ((Path) obj).toString().equals(toString());
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return _sig;
	}

	public String get_source() {
		return _source;
	}

	public String get_target() {
		return _target;
	}

	public boolean existsInSession(Session session) {
		boolean exists = false;

		if (session.hasIDForElement(_source) && session.hasIDForElement(_target)) {
			AbstractElement target = session.getElementForName(_target);

			if (target instanceof MethodElement) {
				MethodElement targetM = ((MethodElement) target);

				exists = targetM.getCalledBy().contains(session.getIdForElement(_source));

			} else if (target instanceof FieldElement) {
				Preconditions.checkNotNull(null, "not implemented yet");
			}
		}

		// System.out.println("Session contains path: " + toString() + " ? " + exists);
		return exists;
	}
}