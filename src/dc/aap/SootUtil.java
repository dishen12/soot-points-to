package dc.aap;

import java.io.File;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class SootUtil {
	public static void init() {
		File someClassFile = new File("./bin/").getAbsoluteFile();
		soot.options.Options.v().set_keep_line_number(true);
		soot.options.Options.v().setPhaseOption("jb", "use-original-names:true");
		soot.options.Options.v().set_whole_program(true);
		Scene.v().setSootClassPath(Scene.v().getSootClassPath() + File.pathSeparator + someClassFile);
	}
	
	public static UnitGraph loadMethod(String methodSignature, String className) {
		if (!Scene.v().containsClass(className)) {
			SootClass c = Scene.v().tryLoadClass(className, 0);	
			c.setApplicationClass();
			Scene.v().loadNecessaryClasses();
		}
		SootMethod m = Scene.v().getSootClass(className).getMethod(methodSignature);
		Body b = m.retrieveActiveBody();
		System.out.println(b);
		return new BriefUnitGraph(b);
	}
}
