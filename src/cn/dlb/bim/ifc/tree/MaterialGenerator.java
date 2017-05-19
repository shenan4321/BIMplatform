package cn.dlb.bim.ifc.tree;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.engine.cells.Colord;

public class MaterialGenerator {
	private IfcModelInterface model;
	private BiMap<IdEObject, IdEObject> materialMap = HashBiMap.create();
	
	public MaterialGenerator(IfcModelInterface model) {
		this.model = model;
		generateMaterialMap();
	}
	
	@SuppressWarnings("rawtypes")
	private void generateMaterialMap() {
		EClass ifcRelAssociatesMaterialClass = (EClass) model.getPackageMetaData().getEClassifierCaseInsensitive("IfcRelAssociatesMaterial");
		List<IdEObject> materiallist = model.getAllWithSubTypes(ifcRelAssociatesMaterialClass);
		for (IdEObject material : materiallist) {
			List relatedObjects = (List) getFeature(material, "RelatedObjects");
			IdEObject relatingMaterial = (IdEObject) getFeature(material, "RelatingMaterial");
			for (Object relatedObject : relatedObjects) {
				if (relatedObject instanceof IdEObject) {
					materialMap.put(relatingMaterial, (IdEObject) relatedObject);
				}
			}
		}
	}
	
	private Object getFeature(IdEObject origin, String featureName) {
		EClass eClass = origin.eClass();
		EStructuralFeature feature = eClass.getEStructuralFeature(featureName);
		if (feature != null) {
			return origin.eGet(feature);
		}
		return null;
	}
	
	public Material getMaterial(IdEObject ifcProduct) {
		Material material = null;
		
		IdEObject ifcProductRepresentation = (IdEObject) ifcProduct.eGet(ifcProduct.eClass().getEStructuralFeature("Representation"));
		
		if (ifcProductRepresentation != null) {
			material = getRgbProductDefinitionShape(ifcProductRepresentation);
			if (material != null) {
				return material;
			}
		}
		
		IdEObject materialInMap = materialMap.inverse().get(ifcProduct);
		if (materialInMap == null) {
			return null;
		}
		if (isInstanceOf(materialInMap, "IfcMaterial")) {
			material = getRGBifcMaterial(materialInMap);
		} else if (isInstanceOf(materialInMap, "IfcMaterialList")) {
			material = getRGBifcMaterialList(materialInMap);
		} else if (isInstanceOf(materialInMap, "IfcMaterialLayerSetUsage")) {
			material = getRGBifcMaterialLayerSetUsage(materialInMap);
		} else if (isInstanceOf(materialInMap, "IfcMaterialLayerSet")) {
			material = getRGBifcMaterialLayerSet(materialInMap);
		} else if (isInstanceOf(materialInMap, "IfcMaterialLayer")) {
			material = getRGBifcMaterialLayer(materialInMap);
		}
		
		return material;
	}
	
	private Material getRGBifcMaterialList(IdEObject ifcMaterialList) {
		Object ifcMaterialsObject = ifcMaterialList.eGet(ifcMaterialList.eClass().getEStructuralFeature("Materials"));
		if (ifcMaterialsObject == null) {
			return null;
		}
		Material material = null;
		List ifcMaterials = (List) ifcMaterialsObject;
		if (ifcMaterials.size() > 0) {
			if (ifcMaterials.get(0) instanceof IdEObject) {
				IdEObject ifcMaterial = (IdEObject) ifcMaterials.get(0);
				if (isInstanceOf(ifcMaterial, "IfcMaterial")) {
					material = getRGBifcMaterial(ifcMaterial);
				}
			}
		}
		return material;
	}
	
	private Material getRGBifcMaterialLayerSet(IdEObject ifcMaterialLayerSet) {
		Material material = null;
		List ifcMaterialLayerList = (List) ifcMaterialLayerSet.eGet(ifcMaterialLayerSet.eClass().getEStructuralFeature("MaterialLayers"));
		if (ifcMaterialLayerList.size() > 0) {
			Object ifcMaterialLayer = ifcMaterialLayerList.get(0);
			material = getRGBifcMaterialLayer((IdEObject) ifcMaterialLayer);
		}
		return material;
	}
	
	private Material getRGBifcMaterialLayer(IdEObject ifcMaterialLayer) {
		IdEObject ifcMaterial = (IdEObject) ifcMaterialLayer.eGet(ifcMaterialLayer.eClass().getEStructuralFeature("Material"));
		Material material = getRGBifcMaterial(ifcMaterial);
		return material;
	}
	
	private Material getRGBifcMaterial(IdEObject ifcMaterial) {
		Material material = null;
		Object ifcMaterialDefinitionRepresentationListObject = ifcMaterial.eGet(ifcMaterial.eClass().getEStructuralFeature("HasRepresentation"));
		List ifcMaterialDefinitionRepresentationList = (List) ifcMaterialDefinitionRepresentationListObject;
		for (Object ifcMaterialDefinitionRepresentationObject : ifcMaterialDefinitionRepresentationList) {
			IdEObject ifcMaterialDefinitionRepresentation = (IdEObject) ifcMaterialDefinitionRepresentationObject;
			
			List ifcRepresentationList = (List) ifcMaterialDefinitionRepresentation.eGet(ifcMaterialDefinitionRepresentation.eClass().getEStructuralFeature("Representations"));
			for (Object ifcRepresentation : ifcRepresentationList) {
				if (isInstanceOf((IdEObject) ifcRepresentation, "IfcStyledRepresentation")) {
					material = getRGBifcStyledRepresentation((IdEObject) ifcRepresentation);
				}
			}
		}
		return material;
	}
	
	private Material getRGBifcStyledRepresentation(IdEObject ifcStyledRepresentation) {
		Material material = null;
		Object ifcRepresentationItemListObject = ifcStyledRepresentation.eGet(ifcStyledRepresentation.eClass().getEStructuralFeature("Items"));
		List ifcRepresentationItemList = (List) ifcRepresentationItemListObject;
		for (Object ifcRepresentationItem : ifcRepresentationItemList) {
			if (isInstanceOf((IdEObject) ifcRepresentationItem, "IfcStyledItem")) {
				material = getRgbifcStyledItem((IdEObject) ifcRepresentationItem);
			}
		}
		return material;
	}
	
	private Material getRGBifcMaterialLayerSetUsage(IdEObject ifcMaterialLayerSetUsage) {
		Material material = null;
		Object ifcMaterialLayerSetObject = ifcMaterialLayerSetUsage.eGet(ifcMaterialLayerSetUsage.eClass().getEStructuralFeature("ForLayerSet"));
		IdEObject ifcMaterialLayerSet = (IdEObject) ifcMaterialLayerSetObject;
		List ifcMaterialLayerList = (List) ifcMaterialLayerSet.eGet(ifcMaterialLayerSet.eClass().getEStructuralFeature("MaterialLayers"));
		int count = ifcMaterialLayerList.size();
		if (count != 0) {
			IdEObject ifcMaterialLayer = (IdEObject) ifcMaterialLayerList.get(0);
			IdEObject ifcMaterial = (IdEObject) ifcMaterialLayer.eGet(ifcMaterialLayer.eClass().getEStructuralFeature("Material"));
			List ifcMaterialDefinitionRepresentationList = (List) ifcMaterial.eGet(ifcMaterial.eClass().getEStructuralFeature("HasRepresentation"));
			for (Object ifcMaterialDefinitionRepresentation : ifcMaterialDefinitionRepresentationList) {
				material = getRgbIfcMaterialDefinitionRepresentation((IdEObject) ifcMaterialDefinitionRepresentation);
			}
		}
		return material;
	}
	
	private Material getRgbProductDefinitionShape(IdEObject ifcProductRepresentation) {
		Material material = null;
		Object representationsSetObject = ifcProductRepresentation.eGet(ifcProductRepresentation.eClass().getEStructuralFeature("Representations"));
		List representationsSet = (List) representationsSetObject;
		for (Object ifcRepresentationObject : representationsSet) {
			IdEObject ifcRepresentation = (IdEObject) ifcRepresentationObject;
			String representationIdentifier = (String) ifcRepresentation.eGet(ifcRepresentation.eClass().getEStructuralFeature("RepresentationIdentifier"));
			String representationType = (String) ifcRepresentation.eGet(ifcRepresentation.eClass().getEStructuralFeature("RepresentationType"));
			
			if ((representationIdentifier.equals("Body") || representationIdentifier.equals("Mesh") || representationIdentifier.equals("Facetation")) && 
					!representationType.equals("BoundingBox")) {
				List geometrySet = (List) ifcRepresentation.eGet(ifcRepresentation.eClass().getEStructuralFeature("Items"));
				for (Object geometryObject : geometrySet) {
					IdEObject geometry = (IdEObject) geometryObject;
					List styledItemList = (List) geometry.eGet(geometry.eClass().getEStructuralFeature("StyledByItem"));
					for (Object ifcStyledItem : styledItemList) {
						material = getRgbifcStyledItem((IdEObject) ifcStyledItem);
					}
				}
			}
		}
		return material;
	}
	
	private Material getRgbifcStyledItem(IdEObject ifcStyledItem) {
		Material material = null;
		Object ifcPresentationStyleAssignmentListObject = ifcStyledItem.eGet(ifcStyledItem.eClass().getEStructuralFeature("Styles"));
		List ifcPresentationStyleAssignmentList = (List) ifcPresentationStyleAssignmentListObject;
		for (Object ifcPresentationStyleAssignmentObject : ifcPresentationStyleAssignmentList) {
			IdEObject ifcPresentationStyleAssignment = (IdEObject) ifcPresentationStyleAssignmentObject;
			List ifcPresentationStyleSelectList = (List) ifcPresentationStyleAssignment.eGet(ifcPresentationStyleAssignment.eClass().getEStructuralFeature("Styles"));
			for (Object ifcPresentationStyleSelectObject : ifcPresentationStyleSelectList) {
				IdEObject ifcPresentationStyleSelect = (IdEObject) ifcPresentationStyleSelectObject;
				if (isInstanceOf(ifcPresentationStyleSelect, "IfcSurfaceStyle")) {
					List ifcSurfaceStyleElementSelectList = (List) ifcPresentationStyleSelect.eGet(ifcPresentationStyleSelect.eClass().getEStructuralFeature("Styles"));
					for (Object ifcSurfaceStyleElementSelectObject : ifcSurfaceStyleElementSelectList) {
						IdEObject ifcSurfaceStyleElementSelect = (IdEObject) ifcSurfaceStyleElementSelectObject;
						if (isInstanceOf(ifcSurfaceStyleElementSelect, "IfcSurfaceStyleRendering")) {//TODO IFCSURFACESTYLESHADING
							material = getRgbSurfaceStyle(ifcSurfaceStyleElementSelect);
						}
					}
				}
			}
		}
		return material;
	}
	
	private Material getRgbIfcMaterialDefinitionRepresentation(IdEObject ifcMaterialDefinitionRepresentation) {
		Material material = null;
		List ifcRepresentationList = (List) ifcMaterialDefinitionRepresentation.eGet(ifcMaterialDefinitionRepresentation.eClass().getEStructuralFeature("Representations"));
		for (Object ifcRepresentation : ifcRepresentationList) {
			if (isInstanceOf((IdEObject) ifcRepresentation, "IfcStyledRepresentation")) {
				material = getRGBifcStyledRepresentation((IdEObject) ifcRepresentation);
			}
		}
		return material;
	}
	
	private Material getRgbSurfaceStyle(IdEObject ifcSurfaceStyleRendering) {
		Material material = new Material();
		Object transparencyObject = ifcSurfaceStyleRendering.eGet(ifcSurfaceStyleRendering.eClass().getEStructuralFeature("Transparency"));
		double transparency = 1.0 - (double) transparencyObject;
		IdEObject surfaceColour = (IdEObject) ifcSurfaceStyleRendering.eGet(ifcSurfaceStyleRendering.eClass().getEStructuralFeature("SurfaceColour"));;
		double red = (double) surfaceColour.eGet(surfaceColour.eClass().getEStructuralFeature("Red"));
		double green = (double) surfaceColour.eGet(surfaceColour.eClass().getEStructuralFeature("Green"));
		double blue = (double) surfaceColour.eGet(surfaceColour.eClass().getEStructuralFeature("Blue"));
//		IfcColourOrFactor diffuseColour = ifcSurfaceStyleRendering.getDiffuseColour();
//		IfcColourOrFactor specularColour = ifcSurfaceStyleRendering.getSpecularColour();
		Colord color = new Colord(red, green, blue, transparency);
		material.setAmbient(color);
		return material;
	}
	
	private Boolean isInstanceOf(IdEObject originObject, String type) {
		PackageMetaData packageMetaData = model.getPackageMetaData();
		EClass eClass = packageMetaData.getEClass(type);
		return eClass.isSuperTypeOf(originObject.eClass());
	}
}
