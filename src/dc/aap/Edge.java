package dc.aap;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Edge {
	public Edge(String src, String field, String trg) {
		source = src;
		target = trg;
		this.field = field;
	}
	public String source;
	public String target;
	public String field;
	
	@Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
            append(source).
            append(target).
            append(field).
            toHashCode();
    }
	
	@Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Edge))
            return false;
        if (obj == this)
            return true;

        Edge edge = (Edge) obj;
        return target.equals(edge.target) && source.equals(edge.source) && field.equals(edge.field);
    }

	@Override
	public String toString() {
		return "(" + source + "," + field  + "," + target + ")" ;  
	}
}