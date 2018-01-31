package dc.aap;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Switch;

@SuppressWarnings("rawtypes")
public class PtgForwardAnalysis extends ForwardFlowAnalysis {
	public enum AnalysisType {
		E1, E2, E4
	}
	AnalysisType analysisType;
	PointsToGraph ptg;

	@SuppressWarnings("unchecked")
	public PtgForwardAnalysis(String methodSignature, String className, PointsToGraph pointsTo, AnalysisType type) {
		super(Init(methodSignature, className));
		ptg = pointsTo;

		analysisType = type;

		String beforeAnalysisMethod = ptg.methodSignature;
		String beforeAnalysisClass = ptg.className;

		ptg.methodSignature = methodSignature;
		ptg.className = className;
		ptg.addThis();

		doAnalysis();

		ptg.methodSignature = beforeAnalysisMethod;
		ptg.className = beforeAnalysisClass;
	}

	private static DirectedGraph Init(String methodSignature, String className) {
		return SootUtil.loadMethod(methodSignature, className);
	}

	@Override
	protected Object newInitialFlow() {
		PointsToGraph initPtg = new PointsToGraph();
		ptg.copy(initPtg);
		return initPtg;
	}

	@Override
	protected void copy(Object source, Object dest) {
		((PointsToGraph)source).copy((PointsToGraph)dest);
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {
		PointsToGraph in1_flow = (PointsToGraph) in1;
		PointsToGraph in2_flow = (PointsToGraph) in2;
		PointsToGraph out_flow = (PointsToGraph) out;
		
		in1_flow.merge(out_flow);
		in2_flow.merge(out_flow);
	}

	@Override
	protected void flowThrough(Object in, Object u, Object out) {
		Unit d = (Unit)u;
		PointsToGraph out_flow = (PointsToGraph) out;
		PointsToGraph in_flow = (PointsToGraph) in;
		in_flow.copy(out_flow);

		d.apply((Switch) new StmtVisitor(out_flow, analysisType));
	}

	@SuppressWarnings("unchecked")
	public PointsToGraph getPointsToGraph() {
		// Merge final de todos los analisis.
		PointsToGraph ptg = new PointsToGraph();
		for (Unit unit : ((UnitGraph)graph).getTails()) {
			  ((PointsToGraph)getFlowAfter(unit)).merge(ptg);
		}
		return ptg;
	}
}