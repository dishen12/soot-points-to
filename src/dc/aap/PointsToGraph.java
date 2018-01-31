package dc.aap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PointsToGraph {
	public Set<Edge> edges;
	public Set<Edge> redges;	
	public Map<String, Set<String> > locals;
	public Set<String> wrongs;
	public Map<Integer, Set<String>> lines;
	public Map<String, Set<String> > statics;

	/* Interprocedural */
	public List<Set<String>> args;
	public Set<String> thisnodes;
	public String methodSignature;
	public String className;
	public Set<String> retnodes;

	/* CallGraph */
	public Set<Edge> callgraph;

	public PointsToGraph() {
		edges = new HashSet<Edge>();
		redges = new HashSet<Edge>();
		locals = new HashMap<String, Set<String> >();
		wrongs = new HashSet<String>();
		lines = new HashMap<Integer, Set<String> >();
		args = new ArrayList<Set<String>>();
		retnodes = new HashSet<String>();
		thisnodes = new HashSet<String>();
		statics = new HashMap<String, Set<String> >();
		callgraph = new HashSet<Edge>();
	}

	public void addThis() {
		String p = "this";
		String TN = className + "_THIS";
		Set<String> set = new HashSet<String>();
		set.add(TN);
		this.locals.put(p, set);
		this.thisnodes = new HashSet<String>();
		this.thisnodes.addAll(set);
	}

	public void merge(PointsToGraph dst){
		dst.edges.addAll(this.edges);
		dst.redges.addAll(this.redges);
		for (String local : this.locals.keySet()) {
			if (dst.locals.containsKey(local)) {
				Set<String> l = dst.locals.get(local);
				l.addAll(this.locals.get(local));
				dst.locals.put(local, l);
			} else {
				dst.locals.put(local, this.locals.get(local));
			}
		}
		dst.wrongs.addAll(this.wrongs);
		for (Integer line : this.lines.keySet()) {
			if (dst.lines.containsKey(line)) {
				Set<String> l = dst.lines.get(line);
				l.addAll(this.lines.get(line));
				dst.lines.put(line, l);
			} else {
				dst.lines.put(line, this.lines.get(line));
			}
		}
		dst.retnodes.addAll(this.retnodes);
		for (String s : this.statics.keySet()) {
			if (dst.statics.containsKey(s)) {
				Set<String> l = dst.statics.get(s);
				l.addAll(this.statics.get(s));
				dst.statics.put(s, l);
			} else {
				dst.statics.put(s, this.statics.get(s));
			}
		}
		dst.callgraph.addAll(this.callgraph);
	}
	
	public void copy(PointsToGraph dest) {
		dest.edges = new HashSet<Edge>();
		dest.redges = new HashSet<Edge>();
		dest.locals = new HashMap<String, Set<String> >();
		dest.wrongs = new HashSet<String>();
		dest.args = new ArrayList<Set<String>>();
		dest.thisnodes = new HashSet<String>();
		dest.retnodes = new HashSet<String>();
		dest.statics = new HashMap<String, Set<String> >();
		dest.callgraph = new HashSet<Edge>();
		
		for (Edge e : this.edges) {
			dest.edges.add(new Edge(e.source, e.field, e.target));
		}
		for (Edge e : this.redges) {
			dest.redges.add(new Edge(e.source, e.field, e.target));
		}
		for (String local : this.locals.keySet()) {
			Set<String> l = this.locals.get(local);
			Set<String> dstl = new HashSet<String>();
			dstl.addAll(l);
			dest.locals.put(local, dstl);
		}
		dest.wrongs.addAll(this.wrongs);
		for (Integer line : this.lines.keySet()) {
			Set<String> l = this.lines.get(line);
			Set<String> dstl = new HashSet<String>();
			dstl.addAll(l);
			dest.lines.put(line, dstl);
		}
		for (Set<String> s : this.args) {
			Set<String> ds = new HashSet<String>();
			ds.addAll(s);
			dest.args.add(ds);
		}
		dest.methodSignature = this.methodSignature;
		dest.className = this.className;
		dest.thisnodes.addAll(this.thisnodes);
		dest.retnodes.addAll(this.retnodes);
		for (String s : this.statics.keySet()) {
			Set<String> l = this.statics.get(s);
			Set<String> dstl = new HashSet<String>();
			dstl.addAll(l);
			dest.statics.put(s, dstl);
		}
		for (Edge e : this.callgraph) {
			dest.callgraph.add(new Edge(e.source, e.field, e.target));
		}
	}

	@Override
    public boolean equals(Object obj) {
		if (!(obj instanceof PointsToGraph))
            return false;
        if (obj == this)
            return true;

        PointsToGraph graph = (PointsToGraph) obj;
        return
        		edges.equals(graph.edges) &&
        		redges.equals(graph.edges) &&
        		locals.equals(graph.locals) &&
        		wrongs.equals(graph.wrongs) &&
        		lines.equals(graph.lines) &&
        		args.equals(graph.args) &&
        		statics.equals(graph.statics) &&
        		callgraph.equals(graph.callgraph);
    }
	
	public int lineCount(int line, String var) {
		int count = 1;
		if (lines.containsKey(line)) {
			if (!lines.get(line).contains(var)) {
				lines.get(line).add(var);
			}
			count = lines.get(line).size();
		} else {
			HashSet<String> set = new HashSet<String>();
			set.add(var);
			lines.put(line, set);
		}
		return count;
	}
}