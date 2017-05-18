package cn.dlb.bim.vo;

import cn.dlb.bim.dao.entity.ModelLabel;

public class ModelLabelVo {
	private Integer labelId;
	private Integer rid;
	private String name;
	private String description;
	private String developData;
	private Double x;
	private Double y;
	private Double z;
	private Double red;
	private Double green;
	private Double blue;
	
	public void setEntity(ModelLabel label) {
		this.labelId = label.getLabelId();
		this.rid = label.getRid();
		this.name = label.getName();
		this.description = label.getDescription();
		this.developData = label.getDevelopData();
		this.x = label.getX();
		this.y = label.getY();
		this.z = label.getZ();
		this.red = label.getRed();
		this.green = label.getGreen();
		this.blue = label.getBlue();
	}
	
	public ModelLabel getEntity() {
		ModelLabel entity = new ModelLabel();
		entity.setLabelId(this.labelId);
		entity.setRid(this.rid);
		entity.setDescription(this.description);
		entity.setDevelopData(this.developData);
		entity.setX(this.x);
		entity.setY(this.y);
		entity.setZ(this.z);
		entity.setRed(this.red);
		entity.setGreen(this.green);
		entity.setBlue(this.blue);
		return entity;
	}
	
	public Integer getLabelId() {
		return labelId;
	}
	public void setLabelId(Integer labelId) {
		this.labelId = labelId;
	}
	public Integer getRid() {
		return rid;
	}
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDevelopData() {
		return developData;
	}
	public void setDevelopData(String developData) {
		this.developData = developData;
	}
	public Double getX() {
		return x;
	}
	public void setX(Double x) {
		this.x = x;
	}
	public Double getY() {
		return y;
	}
	public void setY(Double y) {
		this.y = y;
	}
	public Double getZ() {
		return z;
	}
	public void setZ(Double z) {
		this.z = z;
	}
	public Double getRed() {
		return red;
	}
	public void setRed(Double red) {
		this.red = red;
	}
	public Double getGreen() {
		return green;
	}
	public void setGreen(Double green) {
		this.green = green;
	}
	public Double getBlue() {
		return blue;
	}
	public void setBlue(Double blue) {
		this.blue = blue;
	}
}
