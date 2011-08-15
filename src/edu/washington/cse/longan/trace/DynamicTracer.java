package edu.washington.cse.longan.trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import ca.lsmr.common.log.LSMRLogger;
import ca.uwaterloo.cs.se.inconsistency.core.model2.Model;
import ca.uwaterloo.cs.se.inconsistency.core.model2.io.Model2XMLReader;
import ca.uwaterloo.cs.se.inconsistency.core.model2.io.Model2XMLWriter;

public class DynamicTracer extends Task {

	static {
		LSMRLogger.startLog4J();
	}

	private Logger _log = Logger.getLogger(getClass());

	private File destfile;

	private File tmpdir;

	public File getDestfile() {
		return destfile;
	}

	public void setDestfile(File destfile) {
		this.destfile = destfile;
	}

	public File getTmpdir() {
		return tmpdir;
	}

	public void setTmpdir(File tmpdir) {
		this.tmpdir = tmpdir;
	}

	public void execute() throws BuildException {

		_log.trace("DynamicTracer::execute()");

		if (getDestfile() == null) {
			throw new BuildException("destfile must be set!");
		}

		if (getTmpdir() == null) {
			throw new BuildException("tmpdir must be set!");
		}

		String[] fileNames = tmpdir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}

		});

		Model baseModel = null;

		for (String fileName : fileNames) {
			String fName = tmpdir.getAbsolutePath() + File.separator + fileName;
			try {
				Model m = loadModel(fName);

				if (baseModel == null) {
					baseModel = m;
				} else {
					baseModel.addModel(m);
				}
			} catch (Exception e) {
				_log.fatal("Error loading model from: " + fName);
				_log.warn("Processing will continue, but results should be tested manually");
			}
		}

		try {
			Model2XMLWriter xmlwriter = new Model2XMLWriter(destfile.getAbsolutePath());
			xmlwriter.write(baseModel, AJCollector2.XML_ORIGIN, AJCollector2.XML_KIND, AJCollector2.XML_DESCRIPTION, new Date());
		} catch (FileNotFoundException fnfe) {
			_log.error("DynamicTracer::execute() - ERROR: " + fnfe);
		}

	}

	private Model loadModel(String fName) {
		Model2XMLReader xmlrdf = new Model2XMLReader(fName);
		Model model = xmlrdf.read();
		return model;
	}

}
