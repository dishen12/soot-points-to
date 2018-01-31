package tests;

import static org.junit.Assert.assertTrue;
import java.util.Set;
import org.junit.Test;
import dc.aap.Edge;
import dc.aap.PointsToGraph;
import dc.aap.PtgForwardAnalysis;
import dc.aap.SootUtil;
import dc.aap.PtgForwardAnalysis.AnalysisType;

public class TestE5 {
		String className = "tests.Tests";

		public TestE5() {
			SootUtil.init();
		}

		@Test
		public void test23() {
			// Test: Test básico de CallGraph
			PointsToGraph ptg = new PointsToGraph();
			PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test23()", className, ptg, AnalysisType.E4);

			Set<Edge> callgraph = analysis.getPointsToGraph().callgraph;

			// Llamado al new de ClassA.
			Edge e = new Edge("tests.Tests_void test23()","l_174","tests.Tests$ClassA_void <init>(tests.Tests)");
			assertTrue(callgraph.contains(e));

			// Llamado a la funcion initClassBAAttribute()
			e = new Edge("tests.Tests_void test23()","l_175","tests.Tests$ClassA_void initClassBAttribute()");
			assertTrue(callgraph.contains(e));

			// Dentro del constructor de ClassA se llama a un constructor <init> sin parametros.
			e = new Edge("tests.Tests$ClassA_void <init>(tests.Tests)","l_132","tests.Tests$ClassA_void <init>()");
			assertTrue(callgraph.contains(e));

			// Lo mismo que el anterior.
			e = new Edge("tests.Tests$ClassB_void <init>(tests.Tests)","l_148","tests.Tests$ClassB_void <init>()");
			assertTrue(callgraph.contains(e));

			// El llamado al new de ClassB en initClassBAttribute().
			e = new Edge("tests.Tests$ClassA_void initClassBAttribute()","l_137","tests.Tests$ClassB_void <init>(tests.Tests)");
			assertTrue(callgraph.contains(e));
		}

		@Test
		public void test24() {
			// Test: Doble llamado desde el mismo metodo, para identificarlos.
			PointsToGraph ptg = new PointsToGraph();
			PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test24()", className, ptg, AnalysisType.E4);
			Set<Edge> callgraph = analysis.getPointsToGraph().callgraph;
			Edge e = new Edge("tests.Tests_void test24()","l_180","tests.Tests$ClassA_void initClassBAttribute()");
			assertTrue(callgraph.contains(e));
			e = new Edge("tests.Tests_void test24()","l_181","tests.Tests$ClassA_void initClassBAttribute()");
			assertTrue(callgraph.contains(e));
		}

		@Test
		public void test25() {
			// Test: El llamado a a.lives() debe hacer que el callgraph apunte a perro y a gato.
			PointsToGraph ptg = new PointsToGraph();
			PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test25()", className, ptg, AnalysisType.E4);
			Set<Edge> callgraph = analysis.getPointsToGraph().callgraph;

			// La misma linea, tiene que dar el llamado a lives() de Dog y de Cat
			Edge e = new Edge("tests.Tests_void test25()", "l_192", "tests.Dog_int lives()");
			assertTrue(callgraph.contains(e));
			e = new Edge("tests.Tests_void test25()", "l_192", "tests.Cat_int lives()");
			assertTrue(callgraph.contains(e));

			// De yapa, los llamados a los new de Cat y Dog.
			e = new Edge("tests.Tests_void test25()", "l_188", "tests.Dog_void <init>()");
			assertTrue(callgraph.contains(e));
			e = new Edge("tests.Tests_void test25()", "l_190", "tests.Cat_void <init>()");
			assertTrue(callgraph.contains(e));
		}

		@Test
		public void test26() {
			// Test: Varias llamadas anidadas (El ptg se testea en la suite para E4).
			PointsToGraph ptg = new PointsToGraph();
			PtgForwardAnalysis analysis = new PtgForwardAnalysis("void test26()", className, ptg, AnalysisType.E4);

			Set<Edge> callgraph = analysis.getPointsToGraph().callgraph;
			Edge e = null;
			// De test26 se llama a classBFromConstructor() L:196
			e = new Edge("tests.Tests_void test26()", "l_196", "tests.Tests_tests.Tests$ClassB classBFromConstructor()");
			assertTrue(callgraph.contains(e));
			
			// Desde classBFromConstructor() se llama a func() L:196
			e = new Edge("tests.Tests_tests.Tests$ClassB classBFromConstructor()", "l_163", "tests.Tests$ClassB_void func()");
			assertTrue(callgraph.contains(e));

			// Desde func() se llama a initClassBAttribute() L:152
			e = new Edge("tests.Tests$ClassB_void func()", "l_152", "tests.Tests$ClassA_void initClassBAttribute()");
			assertTrue(callgraph.contains(e));

			// Desde initClassBAttribute se llama al constructor de ClassB L:137
			e = new Edge("tests.Tests$ClassA_void initClassBAttribute()", "l_137", "tests.Tests$ClassB_void <init>(tests.Tests)");
			assertTrue(callgraph.contains(e));
		}
}