package cn.dlb.bim.ifc.engine;

import org.eclipse.emf.common.util.EList;

import cn.dlb.bim.ifc.engine.cells.Colord;
import cn.dlb.bim.ifc.engine.cells.Material;
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

public class MaterialGetter {
	public Material getMaterial(IfcProduct ifcProduct) {
		Material material = null;
		IfcProductRepresentation ifcProductRepresentation = ifcProduct.getRepresentation();
		
		if (ifcProductRepresentation != null) {//TODO 如果找到就返回
			material = getRgbProductDefinitionShape(ifcProductRepresentation);
			if (material != null) {
				return material;
			}
		}
		
		EList<IfcRelAssociates> ifcRelAssociateses = ifcProduct.getHasAssociations();
		for (IfcRelAssociates ifcRelAssociates : ifcRelAssociateses) {
			if (ifcRelAssociates instanceof IfcRelAssociatesMaterial) {
				IfcRelAssociatesMaterial ifcRelAssociatesMaterial = (IfcRelAssociatesMaterial) ifcRelAssociates;
				IfcMaterialSelect ifcMaterialSelect = ifcRelAssociatesMaterial.getRelatingMaterial();
				if (ifcMaterialSelect instanceof IfcMaterial) {
					getRGBifcMaterial((IfcMaterial) ifcMaterialSelect);
				} else if (ifcMaterialSelect instanceof IfcMaterialList) {
					getRGBifcMaterialList((IfcMaterialList) ifcMaterialSelect);
				} else if (ifcMaterialSelect instanceof IfcMaterialLayerSetUsage) {
					getRGBifcMaterialLayerSetUsage( (IfcMaterialLayerSetUsage) ifcMaterialSelect);
				} else if (ifcMaterialSelect instanceof IfcMaterialLayerSet) {
					getRGBifcMaterialLayerSet((IfcMaterialLayerSet) ifcMaterialSelect);
				} else if (ifcMaterialSelect instanceof IfcMaterialLayer) {
					getRGBifcMaterialLayer((IfcMaterialLayer) ifcMaterialSelect);
				}
			}
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
		double transparency = ifcSurfaceStyleRendering.getTransparency();
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
