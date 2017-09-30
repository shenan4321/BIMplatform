package cn.dlb.bim.ifc.tree;

public class Propertry {
	private String name;
	private Object value;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public String text() {
		if (value != null) {
			return name + ":" + value.toString();
		} else {
			return name;
		}
	}
}
