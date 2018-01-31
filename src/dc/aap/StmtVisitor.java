package dc.aap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dc.aap.PtgForwardAnalysis.AnalysisType;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;

public class StmtVisitor  extends AbstractStmtSwitch {
	private PointsToGraph ptg;
	AnalysisType analysis;

	public StmtVisitor(PointsToGraph out, AnalysisType analysisType) {
		ptg = out;
		analysis = analysisType;
	}

	@Override
	public void caseAssignStmt(AssignStmt arg0) {
		ExprVisitor leftOpVisitor = new ExprVisitor(analysis);
		ExprVisitor rightOpVisitor = new ExprVisitor(analysis);
		arg0.getLeftOp().apply(leftOpVisitor);
		arg0.getRightOp().apply(rightOpVisitor);

		/* Solo importan los objetos para el PTG */
		ExprInfo leftExprInfo = leftOpVisitor.getExprInfo();
		ExprInfo rightExprInfo = rightOpVisitor.getExprInfo();
		if (leftExprInfo == null || rightExprInfo == null || leftExprInfo.isConstructorAccess)
			return;
		
		// p: x = new A()
		if (rightExprInfo.isNewObj) {
			// G’ = G con L’(x) = {A_p} 
			int line = arg0.getJavaSourceStartLineNumber();
			String x = leftExprInfo.var;
			int count = ptg.lineCount(line, leftExprInfo.var);
			String A_p = rightExprInfo.var + "_" + count + "_" + line;
			Set<String> set = new HashSet<String>();
			set.add(A_p);
			ptg.locals.put(x, set);
		}
		// x = y
		else if (!leftExprInfo.isFieldAccess && !rightExprInfo.isFieldAccess && !rightExprInfo.isInvocation) {
			// G’ = G con L’(x) = L(y)
			String x = leftExprInfo.var;
			String y = rightExprInfo.var;
			if (ptg.locals.get(y) != null) {
				/* Si locals no tiene a "y" entonces es porque no es un obj */
				Set<String> set = new HashSet<String>();
				set.addAll(ptg.locals.get(y));
				ptg.locals.put(x, set);
			}
		}
		// x = y.f
		else if (!leftExprInfo.isFieldAccess && rightExprInfo.isFieldAccess) {
			if (analysis == AnalysisType.E2) {
				// G’ = G con R’ = R U { (n,f,ln) | n in L(y) } y L’(x) = { ln} con ln fresco
				String x = leftExprInfo.var;
				String y = rightExprInfo.var;

				Set<String> set = new HashSet<String>();
				if (leftExprInfo.isStatic) {
					makeSureStaticExists(y, ptg);
					set = ptg.statics.get(y);
				} else {
					set = ptg.locals.get(y);
				}
				
				// L’(x) = { ln} con ln fresco
				int line = arg0.getJavaSourceStartLineNumber();
				String ln = "ln_" + line;
				Set<String> lset = new HashSet<String>();
				lset.add(ln);
				ptg.locals.put(x,lset);

				// R’ = R U { (n,f,ln) | n in L(y) }
				for (String local : set) {
					Edge e = new Edge(local, rightExprInfo.field, ln);
					ptg.redges.add(e);
				}
			} else {
				// G’ = G con L’(x) = { n | (a,f,n) in E forall a in L(y)} 
				String x = leftExprInfo.var;
				String y = rightExprInfo.var;

				Set<String> set = new HashSet<String>();
				if (rightExprInfo.isStatic) {
					makeSureStaticExists(y, ptg);
					set = ptg.statics.get(y);
				} else {
					set = ptg.locals.get(y);
				}

				ptg.locals.put(x, new HashSet<String>());
				for (String s : set) {
					for (Edge edge : ptg.edges) {
						if (edge.source.equals(s)) {
							ptg.locals.get(x).add(edge.target);
						}
					}
				}
			}
		}
		// x.f = y
		else if (leftExprInfo.isFieldAccess && !rightExprInfo.isFieldAccess) {
			// G’ = G con E’ = E U { (a,f,n) | n in L(y) && a in L(x) }
			String x = leftExprInfo.var;
			String f = leftExprInfo.field;
			String y = rightExprInfo.var;

			Set<String> set = new HashSet<String>();
			if (leftExprInfo.isStatic) {
				makeSureStaticExists(x, ptg);
				set = ptg.statics.get(x);
			} else {
				set = ptg.locals.get(x);
			}

			for (String n : ptg.locals.get(y)) {
				for (String a : set) {
					Edge e = new Edge(a, f, n);
					ptg.edges.add(e);
				}
			}
		}
		// x = Invoke() / x.y = Invoke()
		else if (rightExprInfo.isInvocation) {
			if (analysis == AnalysisType.E4) {
				Set<String> ret = analyseInvocationExpr(rightExprInfo, arg0.getJavaSourceStartLineNumber());
				String x = leftExprInfo.var;

				if (leftExprInfo.isFieldAccess) {
					String f = leftExprInfo.field;
					for (String n : ret) {
						for (String a : ptg.locals.get(x)) {
							Edge e = new Edge(a, f, n);
							ptg.edges.add(e);
						}
					}				
				} else {
					Set<String> set = new HashSet<String>();
					set.addAll(ret);
					ptg.locals.put(x, set);				
				}
			}
		}
		else {
			System.out.println("Error: Assign no contemplado!");
		}
	}

	@Override
	public void caseIdentityStmt(IdentityStmt arg0) {
		switch (analysis) {
			case E1: {
				super.caseIdentityStmt(arg0);
				break;
			}
			case E2: {
				ExprVisitor leftOpVisitor = new ExprVisitor(analysis);
				ExprVisitor rightOpVisitor = new ExprVisitor(analysis);
				arg0.getLeftOp().apply(leftOpVisitor);
				arg0.getRightOp().apply(rightOpVisitor);

				/* Solo importan los objetos para el PTG */
				ExprInfo leftExprInfo = leftOpVisitor.getExprInfo();
				if (leftExprInfo == null)
					return;
				
				// El this lo tomo como parametro.
				String p = leftExprInfo.var;
				String PN = "PN_" + p;
				Set<String> set = new HashSet<String>();
				set.add(PN);
				ptg.locals.put(p, set);
				break;
			}
			case E4: {
				ExprVisitor leftOpVisitor = new ExprVisitor(analysis);
				ExprVisitor rightOpVisitor = new ExprVisitor(analysis);
				arg0.getLeftOp().apply(leftOpVisitor);
				arg0.getRightOp().apply(rightOpVisitor);

				ExprInfo leftExprInfo = leftOpVisitor.getExprInfo();
				ExprInfo rightExprInfo = rightOpVisitor.getExprInfo();

				Set<String> arg = new HashSet<String>();
				if (rightExprInfo.isParameter) {
					arg = ptg.args.get(rightExprInfo.paramIndex);
				} else if (rightExprInfo.isThis) {
					arg = ptg.thisnodes;
				}

				/* Si no esta vacio, entonces son argumentos que apuntan a algo en el PTG
				 * por lo que lo agrego a las locales. */
				if (!arg.isEmpty()) {
					String p = leftExprInfo.var;
					ptg.locals.put(p, arg);
				}
				break;
			}
		}
	}

	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		if (analysis == AnalysisType.E4) {
			ExprVisitor visitor = new ExprVisitor(analysis);
			stmt.getInvokeExpr().apply(visitor);
			ExprInfo exprInfo = visitor.getExprInfo();
			analyseInvocationExpr(exprInfo, stmt.getJavaSourceStartLineNumber());
		}
	}

	@Override
	public void caseReturnStmt(ReturnStmt stmt) {
		if (analysis == AnalysisType.E4) {
			if (ptg.locals.containsKey(stmt.getOp().toString()))
				ptg.retnodes.addAll(ptg.locals.get(stmt.getOp().toString()));
		}
	}

	private Set<String> analyseInvocationExpr(ExprInfo exprInfo, int line) {
		Set<String> ret = new HashSet<String>();

		/* Se evita la recursion infinita */
		if (exprInfo.methodSignature.equals(ptg.methodSignature)) {
			ptg.wrongs.add("rec_" + ptg.className + "_" + ptg.methodSignature);
			return ret;
		}

		Set<String> effectiveTypes = new HashSet<String>();
		Set<String> receiverNodes = new HashSet<String>();
		if (!exprInfo.isStatic) {
			/* El receiver debe pertenecer al PTG */
			if (!ptg.locals.containsKey(exprInfo.receiver)) {
				System.out.println("Error fatal! No existe el receiver en el PTG!");
				System.exit(0);
			}

			/* Se extraen los tipos efectivos. */
			receiverNodes = ptg.locals.get(exprInfo.receiver);
			for (String node : receiverNodes) {
				effectiveTypes.add(node.substring(0, node.indexOf("_")));
			}
		} else {
			effectiveTypes.add(exprInfo.receiver);
		}

		/* Por cada argumento, se extraen los nodos a los que apuntan. */
		/* TODO: Esto vale solo para x, no para x.f! OJO! */
		List<Set<String>> argsNodes = new ArrayList<Set<String>>();
		for (String arg : exprInfo.args) {
			Set<String> set = new HashSet<String>();
			if (ptg.locals.containsKey(arg)) {
				set.addAll(ptg.locals.get(arg));
			}
			argsNodes.add(set);
		}

		/* Se guardan los argumentos y locales de este scope */
		List<Set<String>> currentArgs = ptg.args;
		Map<String, Set<String> > currentLocals = ptg.locals;
		String currentMethodSignature = ptg.methodSignature;
		String currentClassName = ptg.className;
		Set<String> currentThis = ptg.thisnodes;

		/* Se genera el analisis por cada metodo que podría haberse llamado. */
		for (String effectiveType : effectiveTypes) {
			ptg.args = argsNodes;
			ptg.thisnodes = receiverNodes;
			ptg.locals = new HashMap<String, Set<String>>();

			Edge edge = new Edge(currentClassName + "_" + currentMethodSignature, "l_" + line, effectiveType + "_" + exprInfo.methodSignature);
			ptg.callgraph.add(edge);
			
			PointsToGraph p = null;
			try {
				PtgForwardAnalysis analysis = new PtgForwardAnalysis(exprInfo.methodSignature, effectiveType, ptg, AnalysisType.E4);
				p = analysis.getPointsToGraph();
				p.merge(ptg);
			} catch (Exception e) {
				/* Si no puede cargar la clase, no la analizo */
				if (!e.getMessage().contains("No method")) {
					System.out.println("Imposible analizar metodo: " + exprInfo.methodSignature + ", clase: " + effectiveType);
					e.printStackTrace();
				}
			}

			ptg.thisnodes = currentThis;
			ptg.locals = currentLocals;
			ptg.methodSignature = currentMethodSignature;
			ptg.className = currentClassName;
			ptg.args = currentArgs;
			ret.addAll(ptg.retnodes);

			// Si no es static, a los que apunta "this", hay que ligarlos al receiver.
			if (!exprInfo.isStatic && p != null) {
				for (String thisl : p.locals.get("this")) {
					for (Edge e : p.edges) {
						if (e.source.equals(thisl)) {
							for (String rel : ptg.locals.get(exprInfo.receiver)) {
								Edge ree = new Edge(rel, e.field, e.target);
								ptg.edges.add(ree);
							}
						}
					}
				}
			}
		}

		return ret;
	}

	public static void makeSureStaticExists(String staticName, PointsToGraph ptg) {
		if (!ptg.statics.containsKey(staticName)) {
			String p = staticName;
			String TN = staticName;
			Set<String> set = new HashSet<String>();
			set.add(TN);
			ptg.statics.put(p, set);
		}
	}
}
