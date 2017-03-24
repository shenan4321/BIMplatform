package cn.dlb.bim.ifc.model;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.PackageMetaData;


public class BasicIfcModel extends IfcModel {

	public BasicIfcModel(PackageMetaData packageMetaData) {
		super(packageMetaData);
	}

	public BasicIfcModel(PackageMetaData packageMetaData, int size) {
		super(packageMetaData, size);
	}

	@Override
	public void load(IdEObject idEObject) {
	}
}
