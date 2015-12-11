package com.iseu.CSlicer;

import java.util.ArrayList;

import org.neo4j.cypher.internal.compiler.v2_2.helpers.StringRenderingSupport;

public class SliceCriterion {
	public String filePath;
	public int lineNumber;
	public ArrayList<String> interestVars;
	public String traceFilePath;
	public SliceCriterion(String filePath, int lineNumber, ArrayList<String> interestVars) {
		super();
		this.filePath = filePath;
		this.lineNumber = lineNumber;
		this.interestVars = interestVars;
	}
	public SliceCriterion(String filePath, int lineNumber, ArrayList<String> interestVars, String traceFilePath) {
		super();
		this.filePath = filePath;
		this.lineNumber = lineNumber;
		this.interestVars = interestVars;
		this.traceFilePath=traceFilePath;
	}
}
