package cn.dlb.bim.ifc.tree;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.engine.cells.Colord;
import cn.dlb.bim.models.ifc2x3tc1.IfcColourOrFactor;
import cn.dlb.bim.models.ifc2x3tc1.IfcColourRgb;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterial;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialDefinitionRepresentation;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialLayer;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialLayerSet;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialLayerSetUsage;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialList;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialSelect;
import cn.dlb.bim.models.ifc2x3tc1.IfcPresentationStyleAssignment;
import cn.dlb.bim.models.ifc2x3tc1.IfcPresentationStyleSelect;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;
import cn.dlb.bim.models.ifc2x3tc1.IfcProductRepresentation;
import cn.dlb.bim.models.ifc2x3tc1.IfcRelAssociates;
import cn.dlb.bim.models.ifc2x3tc1.IfcRelAssociatesMaterial;
import cn.dlb.bim.models.ifc2x3tc1.IfcRepresentation;
import cn.dlb.bim.models.ifc2x3tc1.IfcRepresentationItem;
import cn.dlb.bim.models.ifc2x3tc1.IfcStyledItem;
import cn.dlb.bim.models.ifc2x3tc1.IfcStyledRepresentation;
import cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyle;
import cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyleElementSelect;
import cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyleRendering;

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
		IfcProductRepresentation ifcProductRepresentation = ((IfcProduct)ifcProduct).getRepresentation();
		
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
		if (materialInMap instanceof IfcMaterial) {
			material = getRGBifcMaterial((IfcMaterial) materialInMap);
		} else if (materialInMap instanceof IfcMaterialList) {
			material = getRGBifcMaterialList((IfcMaterialList) materialInMap);
		} else if (materialInMap instanceof IfcMaterialLayerSetUsage) {
			material = getRGBifcMaterialLayerSetUsage( (IfcMaterialLayerSetUsage) materialInMap);
		} else if (materialInMap instanceof IfcMaterialLayerSet) {
			material = getRGBifcMaterialLayerSet((IfcMaterialLayerSet) materialInMap);
		} else if (materialInMap instanceof IfcMaterialLayer) {
			material = getRGBifcMaterialLayer((IfcMaterialLayer) materialInMap);
		}
		
		return material;
	}
	
	private Material getRGBifcMaterialList(IfcMaterialList ifcMaterialList) {
		Material material = null;
		EList<IfcMaterial> ifcMaterials = ifcMaterialList.getMaterials();
		if (ifcMaterials.size() > 0) {
			IfcMaterial ifcMaterial = ifcMaterials.get(0);
			material = getRGBifcMaterial(ifcMaterial);
		}
		return material;
	}
	
	private Material getRGBifcMaterialLayerSet(IfcMaterialLayerSet ifcMaterialLayerSet) {
		Material material = null;
		EList<IfcMaterialLayer> ifcMaterialLayerList = ifcMaterialLayerSet.getMaterialLayers();
		if (ifcMaterialLayerList.size() > 0) {
			IfcMaterialLayer ifcMaterialLayer = ifcMaterialLayerList.get(0);
			material = getRGBifcMaterialLayer(ifcMaterialLayer);
		}
		return material;
	}
	
	private Material getRGBifcMaterialLayer(IfcMaterialLayer ifcMaterialLayer) {
		IfcMaterial ifcMaterial =ifcMaterialLayer.getMaterial();
		Material material = getRGBifcMaterial(ifcMaterial);
		return material;
	}
	
	private Material getRGBifcMaterial(IfcMaterial ifcMaterial) {
		Material material = null;
		EList<IfcMaterialDefinitionRepresentation> ifcMaterialDefinitionRepresentationList = ifcMaterial.getHasRepresentation();
		for (IfcMaterialDefinitionRepresentation ifcMaterialDefinitionRepresentation : ifcMaterialDefinitionRepresentationList) {
			EList<IfcRepresentation> ifcRepresentationList = ifcMaterialDefinitionRepresentation.getRepresentations();
			for (IfcRepresentation ifcRepresentation : ifcRepresentationList) {
				if (ifcRepresentation instanceof IfcStyledRepresentation) {
					material = getRGBifcStyledRepresentation((IfcStyledRepresentation) ifcRepresentation);
				}
			}
		}
		return material;
	}
	
	private Material getRGBifcStyledRepresentation(IfcStyledRepresentation ifcStyledRepresentation) {
		Material material = null;
		EList<IfcRepresentationItem> ifcRepresentationItemAggr = ifcStyledRepresentation.getItems();
		for (IfcRepresentationItem ifcRepresentationItem : ifcRepresentationItemAggr) {
			if (ifcRepresentationItem instanceof IfcStyledItem) {
				IfcStyledItem ifcStyledItem = (IfcStyledItem) ifcRepresentationItem;
				material = getRgbStyledItem(ifcStyledItem);
			}
		}
		return material;
	}
	
	private Material getRGBifcMaterialLayerSetUsage(IfcMaterialLayerSetUsage ifcMaterialLayerSetUsage) {
		Material material = null;
		IfcMaterialLayerSet ifcMaterialLayerSet = ifcMaterialLayerSetUsage.getForLayerSet();
		EList<IfcMaterialLayer> ifcMaterialLayerAggr = ifcMaterialLayerSet.getMaterialLayers();
		int count = ifcMaterialLayerAggr.size();
		if (count != 0) {
			IfcMaterialLayer ifcMaterialLayer = ifcMaterialLayerAggr.get(0);
			IfcMaterial ifcMaterial = ifcMaterialLayer.getMaterial();
			EList<IfcMaterialDefinitionRepresentation> ifcMaterialDefinitionRepresentationAggr = ifcMaterial.getHasRepresentation();
			for (IfcMaterialDefinitionRepresentation ifcMaterialDefinitionRepresentation : ifcMaterialDefinitionRepresentationAggr) {
				material = getRgbIfcMaterialDefinitionRepresentation(ifcMaterialDefinitionRepresentation);
			}
		}
		return material;
	}
	
	private Material getRgbProductDefinitionShape(IfcProductRepresentation ifcProductRepresentation) {
		Material material = null;
		EList<IfcRepresentation> representationsSet = ifcProductRepresentation.getRepresentations();
		for (IfcRepresentation ifcRepresentation : representationsSet) {
			String representationIdentifier = ifcRepresentation.getRepresentationIdentifier();
			String representationType = ifcRepresentation.getRepresentationType();
			
			if ((representationIdentifier.equals("Body") || representationIdentifier.equals("Mesh") || representationIdentifier.equals("Facetation")) && 
					!representationType.equals("BoundingBox")) {
				EList<IfcRepresentationItem> geometrySet = ifcRepresentation.getItems();
				for (IfcRepresentationItem geometry : geometrySet) {
					EList<IfcStyledItem> styledItemAggr = geometry.getStyledByItem();
					for (IfcStyledItem ifcStyledItem : styledItemAggr) {
						material = getRgbStyledItem(ifcStyledItem);
					}
				}
			}
		}
		return material;
	}
	
	private Material getRgbStyledItem(IfcStyledItem ifcStyledItem) {
		Material material = null;
		EList<IfcPresentationStyleAssignment> ifcPresentationStyleAssignmentAggr = ifcStyledItem.getStyles();
		for (IfcPresentationStyleAssignment ifcPresentationStyleAssignment : ifcPresentationStyleAssignmentAggr) {
			EList<IfcPresentationStyleSelect> ifcPresentationStyleSelectAggr = ifcPresentationStyleAssignment.getStyles();
			for (IfcPresentationStyleSelect ifcPresentationStyleSelect : ifcPresentationStyleSelectAggr) {
				if (ifcPresentationStyleSelect instanceof IfcSurfaceStyle) {
					IfcSurfaceStyle ifcSurfaceStyle = (IfcSurfaceStyle) ifcPresentationStyleSelect;
					EList<IfcSurfaceStyleElementSelect> ifcSurfaceStyleElementSelectAggr = ifcSurfaceStyle.getStyles();
					for (IfcSurfaceStyleElementSelect ifcSurfaceStyleElementSelect : ifcSurfaceStyleElementSelectAggr) {
						if (ifcSurfaceStyleElementSelect instanceof IfcSurfaceStyleRendering) {
							IfcSurfaceStyleRendering ifcSurfaceStyleRendering = (IfcSurfaceStyleRendering) ifcSurfaceStyleElementSelect;
							material = getRgbSurfaceStyle(ifcSurfaceStyleRendering);
						}
					}
				}
			}
		}
		return material;
	}
	
	private Material getRgbIfcMaterialDefinitionRepresentation(IfcMaterialDefinitionRepresentation ifcMaterialDefinitionRepresentation) {
		Material material = null;
		EList<IfcRepresentation> ifcRepresentationAggr = ifcMaterialDefinitionRepresentation.getRepresentations();
		for (IfcRepresentation ifcRepresentation : ifcRepresentationAggr) {
			if (ifcRepresentation instanceof IfcStyledRepresentation) {
				material = getRGBifcStyledRepresentation((IfcStyledRepresentation) ifcRepresentation);
			}
		}
		return material;
	}
	
	private Material getRgbSurfaceStyle(IfcSurfaceStyleRendering ifcSurfaceStyleRendering) {
		Material material = new Material();
		double transparency = 1.0 - ifcSurfaceStyleRendering.getTransparency();
		IfcColourRgb surfaceColour = ifcSurfaceStyleRendering.getSurfaceColour();
		double red = surfaceColour.getRed();
		double green = surfaceColour.getGreen();
		double blue = surfaceColour.getBlue();
		IfcColourOrFactor diffuseColour = ifcSurfaceStyleRendering.getDiffuseColour();
		IfcColourOrFactor specularColour = ifcSurfaceStyleRendering.getSpecularColour();
		Colord color = new Colord(red, green, blue, transparency);
		material.setAmbient(color);
		return material;
	}
}
