package tests;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import dc.aap.Edge;
import dc.aap.PointsToGraph;
import dc.aap.PtgForwardAnalysis;
import dc.aap.PtgForwardAnalysis.AnalysisType;
import dc.aap.SootUtil;

public class TestE1 {
	
	String className = "tests.Tests";

	public TestE1() {
		SootUtil.init();
	}
	
	@Test
	public void test1() {
		// Test: Creacion de objeto.
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test1()", className, new PointsToGraph(), AnalysisType.E1);
		
		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_6"));
	}

	@Test
	public void test2() {
		// Test: Asignación simple.
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test2()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.containsKey("a2"));
		assertTrue(locals.get("a").equals(locals.get("a2")));
	}

	@Test
	public void test3() {
		// Test: Strong update.
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test3()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		Set<Edge> edges = analysis.getPointsToGraph().edges;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.containsKey("b"));
		
		for (Edge e : edges) {
			if (e.field.equals("classBAttributte")) {
				assertTrue(locals.get("b").equals(e.target));
			}
		}
	}

	@Test
	public void test4() {
		// Test: Existencia de eje entre objeto A y objeto B (a = new(), b= new(), a.f = b) 
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test4()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		Set<Edge> edges = analysis.getPointsToGraph().edges;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.containsKey("b"));

		String aObj = locals.get("a").iterator().next();
		String bObj = locals.get("b").iterator().next();
	
		for (Edge e : edges) {
			if (e.source.equals(aObj)) {
				assertTrue(e.target.equals(bObj));
			}
		}
	}

	@Test
	public void test5() {
		// Test: Strong update de fields.		
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test5()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		Set<Edge> edges = analysis.getPointsToGraph().edges;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.containsKey("a2"));

		String aObj = locals.get("a").iterator().next();
		String a2Obj = locals.get("a2").iterator().next();
		
		String aObjTarget = null;
		String a2ObjTarget = null;
		for (Edge e : edges) {
			if (e.source.equals(a2Obj)) {
				assertTrue(e.field.equals("classBAttribute"));
				a2ObjTarget = e.target;
			} else if (e.source.equals(aObj)) {
				assertTrue(e.field.equals("classBAttribute"));
				aObjTarget = e.target;
			}
		}

		assertTrue(aObjTarget.equals(a2ObjTarget));
	}
	
	@Test
	public void test6() {
		// Test: Dos allocs en la misma linea
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test6()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		assertTrue(locals.containsKey("a"));
		assertTrue(locals.containsKey("a2"));

		String aObj = locals.get("a").iterator().next();
		String a2Obj = locals.get("a2").iterator().next();

		assertTrue(!aObj.equals(a2Obj));
	}
	
	@Test
	public void test7() {
		// Test: Allocs en FOR
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test7()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		assertTrue(locals.containsKey("a"));
		assertTrue(locals.containsKey("a2"));

		assertTrue(locals.get("a2").contains("tests.Tests$ClassA_1_41"));
		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_41"));
	}
	
	@Test
	public void test8() {
		// Test: Join de Allocs en If-Then-Else
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test8()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		assertTrue(locals.containsKey("a"));

		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_50"));
		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_52"));
	}

	@Test
	public void test9() {
		// Test: Error en "a = new (), a = b,  a = b.f" (agrega nueva ref a’ a L (?))
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test9()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		Set<Edge> edges = analysis.getPointsToGraph().edges;

		assertTrue(locals.containsKey("a"));
		assertTrue(locals.containsKey("a2"));

		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_57"));
		String aObj = locals.get("a").iterator().next();

		// Por el soft update tienen que existir los dos objetos.
		String aObjTarget1 = null;
		String aObjTarget2 = null;
		for (Edge e : edges) {
			if (e.source.equals(aObj)) {
				assertTrue(e.field.equals("classAAttribute"));
				if (aObjTarget1 == null)
					aObjTarget1 = e.target;
				else
					aObjTarget2 = e.target;
			}
		}
		assertTrue(aObjTarget1 != null && aObjTarget2 != null);
		assertTrue(!aObjTarget1.equals(aObjTarget2));

		// Por el stronge update a2 no es ni el objeto de la linea 60 ni el de la linea 57
		assertTrue(!locals.get("a2").contains("tests.Tests$ClassA_1_60"));
		assertTrue(!locals.get("a2").contains("tests.Tests$ClassA_1_57"));

		// Por la tercer regla, "a2" debe contener los objetos asignados a "a"
		assertTrue(locals.get("a2").contains(aObjTarget1));
		assertTrue(locals.get("a2").contains(aObjTarget2));

		// Se chequea que no agrege nueva ref a’ a L
		Set<String> map = new HashSet<String>();
		for (String local : locals.keySet()) {
			if (!local.startsWith("$r")) {
				map.add(local);
			}
		}
		map.remove("a");
		map.remove("a2");
		map.remove("this");
		map.remove("_STATIC_");

		assertTrue(map.isEmpty());
	}

	@Test
	public void test12() {
		// Test: Comportamiento con funciones estaticas
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test12()", className, new PointsToGraph(), AnalysisType.E1);

		// Para el analisis del Ejercicio 1, a los stmts con funciones no los
		// analizamos. El objetivo del test es ver que no se cuelgue y que
		// efectivamente no haga nada.
		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		assertTrue(!locals.containsKey("c"));
	}

	@Test
	public void test14() {
		// Test: Este test es parecido al test 8, pero si hay return no se hace
		// merge en el analisis, por lo que hay que mergearlo al final... creo.
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test14()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		assertTrue(locals.containsKey("a"));

		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_89"));
		assertTrue(locals.get("a").contains("tests.Tests$ClassA_1_92"));
	}

	@Test
	public void test18() {
		// Test: Se testea la lectura y escritura de atributos estaticos.
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test18()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		Map<String, Set<String> > statics = analysis.getPointsToGraph().statics;
		Set<Edge> edges = analysis.getPointsToGraph().edges;

		assertTrue(statics.containsKey("tests.Tests$ClassA_STATIC"));
		assertTrue(locals.containsKey("a"));

		String source = new String("tests.Tests$ClassA_STATIC");
		String target = new String("tests.Tests$ClassA_1_119");

		assertTrue(statics.get("tests.Tests$ClassA_STATIC").contains(source));
		assertTrue(locals.get("a").contains(target));

		for (Edge e : edges) {
			if (e.source.equals(source)) {
				assertTrue(e.field.equals("staticAAttribute"));
				assertTrue(e.target.equals(target));
			}
		}
	}

	@Test
	public void test19() {
		// Test: Se testea la lectura y escritura de atributos de this (implicito).
		PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test19()", className, new PointsToGraph(), AnalysisType.E1);

		Map<String, Set<String> > locals = analysis.getPointsToGraph().locals;
		Set<Edge> edges = analysis.getPointsToGraph().edges;

		assertTrue(locals.containsKey("this"));
		assertTrue(locals.containsKey("a"));

		String source = new String("tests.Tests_THIS");
		String target = new String("tests.Tests$ClassA_1_124");

		assertTrue(locals.get("this").contains(source));
		assertTrue(locals.get("a").contains(target));

		for (Edge e : edges) {
			if (e.source.equals(source)) {
				assertTrue(e.field.equals("instanceAAttribute"));
				assertTrue(e.target.equals(target));
			}
		}
	}
}
