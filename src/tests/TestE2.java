package tests;

import static org.junit.Assert.assertTrue;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import dc.aap.Edge;
import dc.aap.PointsToGraph;
import dc.aap.PtgForwardAnalysis;
import dc.aap.SootUtil;
import dc.aap.PtgForwardAnalysis.AnalysisType;

public class TestE2 {

	String className = "tests.Tests";

	public TestE2() {
		SootUtil.init();
	}

	@Test
	public void test10() {
		// Test: Carga de parametros L(p) = {PN} para todo p.
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test10(java.lang.Object,java.lang.Object)", className, new PointsToGraph(), AnalysisType.E2);

		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("this"));
		assertTrue(locals.get("this").contains("PN_this"));
		assertTrue(locals.containsKey("x"));
		assertTrue(locals.get("x").contains("PN_x"));
		assertTrue(locals.containsKey("y"));
		assertTrue(locals.get("y").contains("PN_y"));
	}

	@Test
	public void test11() {
		// Test: Comprobar el efecto de ln.
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test11()", className, new PointsToGraph(), AnalysisType.E2);

		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;
		Set<Edge> edges = analysis.getPointsToGraph().edges;
		Set<Edge> redges = analysis.getPointsToGraph().redges;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.containsKey("b"));
		assertTrue(locals.containsKey("b2"));

		// x = y.f carga un ln fresco (se distinguen con numero de linea).
		assertTrue(locals.get("b").contains("ln_72"));
		assertTrue(locals.get("b2").contains("ln_73"));

		String aObj = "tests.Tests$ClassA_1_68";
		assertTrue(locals.get("a").contains(aObj));

		// Los nodos que apunta y, van por classBAttribute a los nuevos nodos ln
		Edge r = new Edge(aObj, "classBAttribute", "ln_72");
		assertTrue(redges.contains(r));
		r = new Edge(aObj, "classBAttribute", "ln_73");
		assertTrue(redges.contains(r));
		
		// Los ejes quedan igual
		Edge e = new Edge(aObj, "classBAttribute", "tests.Tests$ClassB_1_69");
		assertTrue(edges.contains(e));
	}

}
