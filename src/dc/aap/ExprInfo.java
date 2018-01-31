package dc.aap;

import java.util.List;

public class ExprInfo {
	public boolean isFieldAccess = false;
	public boolean isNewObj = false;
	public String field = "_NO_FIELD_";
	public String var = "_NO_VAR_";
	public boolean isStatic;

	/* Invocations */
	public boolean isInvocation;
	public String methodSignature;
	public List<String> args;
	public String receiver;

	/* Parameters */
	public boolean isParameter;
	public int paramIndex;
	public boolean isThis;
	public boolean isConstructorAccess;
}
