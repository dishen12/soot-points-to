package dc.aap;

import java.util.ArrayList;
import dc.aap.PtgForwardAnalysis.AnalysisType;
import soot.Local;
import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.ThisRef;
import soot.jimple.VirtualInvokeExpr;

public class ExprVisitor extends AbstractJimpleValueSwitch {
	protected ExprInfo exprInfo = null;
	AnalysisType analysis;

	public ExprVisitor(AnalysisType analysisType) {
		analysis = analysisType;
	}
	
	public ExprInfo getExprInfo() {
		return exprInfo;
	}

	@Override
	public void caseInstanceFieldRef(InstanceFieldRef arg0) {
		exprInfo = new ExprInfo();
		if (arg0.getField().getName().equals("this$0")) {
			// XXX: Hack, es para que this.this$0 = tests.Tests, es decir, la clase.
			// Como no se como funciona, lo voy a evitar
			exprInfo.isConstructorAccess = true;
		}
		exprInfo.field = arg0.getField().getName();
		exprInfo.var = arg0.getBase().toString();
		exprInfo.isFieldAccess = true;
	}

	@Override
	public void caseStaticFieldRef(StaticFieldRef arg0) {
		exprInfo = new ExprInfo();
		exprInfo.field = arg0.getField().getName();
		exprInfo.var = arg0.getType() + "_STATIC";
		exprInfo.isFieldAccess = true;
		exprInfo.isStatic = true;
	}

	@Override
	public void caseLocal(Local arg0) {
		exprInfo = new ExprInfo();
		exprInfo.var = arg0.getName();
	}

	@Override
	public void caseNewExpr(NewExpr arg0) {
		exprInfo = new ExprInfo();
		exprInfo.var = arg0.getType().toString();
		exprInfo.isNewObj = true;
	}
	
	/*** Analisis E4 ****/
	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr arg0) {
		if (analysis == AnalysisType.E4) {
			generalInvokeInfo(arg0);
			exprInfo.receiver = arg0.getBase().toString();
		} else {
			super.caseSpecialInvokeExpr(arg0);
		}
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr arg0) {
		if (analysis == AnalysisType.E4) {
			generalInvokeInfo(arg0);
			exprInfo.isStatic = true;
			exprInfo.receiver = arg0.getMethod().getDeclaringClass().toString();
		} else {
			super.caseStaticInvokeExpr(arg0);
		}
	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr arg0) {
		if (analysis == AnalysisType.E4) {
			generalInvokeInfo(arg0);
			exprInfo.receiver = arg0.getBase().toString();
		} else {
			super.caseVirtualInvokeExpr(arg0);
		}
	}

	@Override
	public void caseParameterRef(ParameterRef arg0) {
		if (analysis == AnalysisType.E4) {
			exprInfo = new ExprInfo();
			exprInfo.isParameter = true;
			exprInfo.paramIndex = arg0.getIndex();
		} else {
			super.caseParameterRef(arg0);
		}
	}

	@Override
	public void caseThisRef(ThisRef arg0) {
		if (analysis == AnalysisType.E4) {
			exprInfo = new ExprInfo();
			exprInfo.isThis = true;
		} else {
			super.caseThisRef(arg0);
		}
	}

	public void generalInvokeInfo(InvokeExpr arg0) {
		exprInfo = new ExprInfo();
		exprInfo.isInvocation = true;
		exprInfo.methodSignature = arg0.getMethod().getSubSignature();
		exprInfo.args = new ArrayList<String>();
		for (Value v : arg0.getArgs())
			exprInfo.args.add(v.toString());
	}
}
