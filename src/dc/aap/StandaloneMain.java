package dc.aap;

import dc.aap.PtgForwardAnalysis.AnalysisType;

public class StandaloneMain {

	public static void main(String[] args) throws Exception {		
		SootUtil.init();
		new PtgForwardAnalysis("entryPoint", "dc.aap.SomeClass", new PointsToGraph(), AnalysisType.E1);
	}	
}
