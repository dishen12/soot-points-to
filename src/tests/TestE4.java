package tests;

import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import dc.aap.Edge;
import dc.aap.PointsToGraph;
import dc.aap.PtgForwardAnalysis;
import dc.aap.SootUtil;
import dc.aap.PtgForwardAnalysis.AnalysisType;

public class TestE4 {

	String className = "tests.Tests";

	public TestE4() {
		SootUtil.init();
	}

	@Test
	public void test13() {
		// Test: x = o.invoke();
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test13()", className, ptg, AnalysisType.E4);
		
		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("o"));
		assertTrue(locals.containsKey("a"));
		assertTrue(!locals.containsKey("x")); // Se borra al salir de la funcion.

		assertTrue(locals.get("o").contains("java.lang.Object_1_81"));
		assertTrue(locals.get("a").contains("java.lang.Object_1_99"));
	}

	@Test
	public void test15() {
		// Test: x.f = o.invoke();
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test15()", className, ptg, AnalysisType.E4);
		
		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;
		Set<Edge> edges = analysis.getPointsToGraph().edges;

		assertTrue(locals.containsKey("a"));

		String obj = new String("tests.Tests$ClassA_1_104");
		
		assertTrue(locals.get("a").contains(obj));

		Set<String> targets = new HashSet<String>();
		for (Edge e : edges) {
			if (e.source.equals(obj)) {
				assertTrue(e.field.equals("ObjectAttribute"));
				targets.add(e.target);
			}
		}
		assertTrue(targets.contains("java.lang.Object_1_105"));
		assertTrue(targets.contains("java.lang.Object_1_99"));
	}

	@Test
	public void test16() {
		// Test: field = new A(); = this.field = new A(); | El this es implícito.
		PointsToGraph ptg = new PointsToGraph();
		
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test16()", className, ptg, AnalysisType.E4);
		
		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;
		Set<Edge> edges = analysis.getPointsToGraph().edges;

		assertTrue(locals.containsKey("this"));

		String source = new String("tests.Tests_THIS");
		String target = new String("tests.Tests$ClassA_1_111");

		for (Edge e : edges) {
			if (e.source.equals(source)) {
				assertTrue(e.field.equals("classAAttribute"));
				assertTrue(e.target.equals(target));
			}
		}
	}

	@Test
	public void test17() {
		// Test: Llamado a metodo estatico
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test17()", className, ptg, AnalysisType.E4);

		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("c"));
		assertTrue(locals.get("c").contains("tests.TestClassC_1_11"));
	}

	@Test
	public void test20() {
		// Test: x = o.invoke();
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test20()", className, ptg, AnalysisType.E4);
		
		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.containsKey("b"));

		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_142"));
		assertTrue(locals.get("b").contains("tests.Tests$ClassB_1_137"));
	}

	@Test
	public void test21() {
		// Test: method inside static.
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test21()", className, ptg, AnalysisType.E4);
		
		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("b"));
		assertTrue(locals.get("b").contains("tests.Tests$ClassB_1_137"));
	}

	@Test
	public void test22() {
		// Test: Se prueba recursion y wrongs(que hasta ahora no se probo).
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test22()", className, ptg, AnalysisType.E4);

		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;
		Set<String> wrongs = analysis.getPointsToGraph().wrongs;

		assertTrue(locals.containsKey("c"));
		assertTrue(locals.get("c").contains("tests.TestClassC_1_169"));

		// El jimple del constructor llama a otro <init>, eso me hacia colgar.
		assertTrue(wrongs.contains("rec_tests.TestClassC_void <init>()"));

		// El llamado recursivo de recursive se mete en wrongs.
		assertTrue(wrongs.contains("rec_tests.TestClassC_int recursive(int)"));
	}

	@Test
	public void test27() {
		// Test: Modificacion de atributo del this mediante metodo (this implicito)
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test27()", className, ptg, AnalysisType.E4);

		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_200"));
	}

	@Test
	public void test28() {
		// Test: Analisis del constructor (con parametros).
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test28()", className, ptg, AnalysisType.E4);

		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("a2"));
		assertTrue(locals.get("a2").contains("tests.Tests$ClassA_1_209"));
	}

	@Test
	public void test29() {
		// Test: Llamados a funcion dentro de un FOR.
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test29()", className, ptg, AnalysisType.E4);

		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_216"));

		// XXX: El JIMPLE no respeta el nombre de la variable, en lugar de "a2" le pone "i".
		assertTrue(locals.containsKey("i"));
		assertTrue(locals.get("i").contains("tests.Tests$ClassA_1_216"));
	}
	@Test
	public void test30() {
		// Test: Llamados a funcion dentro de un FOR.
		PointsToGraph ptg = new PointsToGraph();
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test30()", className, ptg, AnalysisType.E4);

		Map<String, Set<String>> locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("b"));
		assertTrue(locals.get("b").contains("tests.Tests$ClassB_1_241"));
	}
}