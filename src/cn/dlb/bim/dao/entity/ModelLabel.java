package cn.dlb.bim.dao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ModelLabel")
public class ModelLabel {
	@Id
	private Integer labelId;
	@Indexed
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
