/**
 * Copyright (C) 2009-2014 BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cn.dlb.bim.models.ifc2x3tc1;

import org.eclipse.emf.common.util.EList;

import cn.dlb.bim.emf.IdEObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ifc Representation Map</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link cn.dlb.bim.models.ifc2x3tc1.IfcRepresentationMap#getMappingOrigin <em>Mapping Origin</em>}</li>
 *   <li>{@link cn.dlb.bim.models.ifc2x3tc1.IfcRepresentationMap#getMappedRepresentation <em>Mapped Representation</em>}</li>
 *   <li>{@link cn.dlb.bim.models.ifc2x3tc1.IfcRepresentationMap#getMapUsage <em>Map Usage</em>}</li>
 * </ul>
 *
 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcRepresentationMap()
 * @model
 * @extends IdEObject
 * @generated
 */
public interface IfcRepresentationMap extends IdEObject {
	/**
	 * Returns the value of the '<em><b>Mapping Origin</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mapping Origin</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mapping Origin</em>' reference.
	 * @see #setMappingOrigin(IfcAxis2Placement)
	 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcRepresentationMap_MappingOrigin()
	 * @model
	 * @generated
	 */
	IfcAxis2Placement getMappingOrigin();

	/**
	 * Sets the value of the '{@link cn.dlb.bim.models.ifc2x3tc1.IfcRepresentationMap#getMappingOrigin <em>Mapping Origin</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mapping Origin</em>' reference.
	 * @see #getMappingOrigin()
	 * @generated
	 */
	void setMappingOrigin(IfcAxis2Placement value);

	/**
	 * Returns the value of the '<em><b>Mapped Representation</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link cn.dlb.bim.models.ifc2x3tc1.IfcRepresentation#getRepresentationMap <em>Representation Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mapped Representation</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mapped Representation</em>' reference.
	 * @see #setMappedRepresentation(IfcRepresentation)
	 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcRepresentationMap_MappedRepresentation()
	 * @see cn.dlb.bim.models.ifc2x3tc1.IfcRepresentation#getRepresentationMap
	 * @model opposite="RepresentationMap"
	 * @generated
	 */
	IfcRepresentation getMappedRepresentation();

	/**
	 * Sets the value of the '{@link cn.dlb.bim.models.ifc2x3tc1.IfcRepresentationMap#getMappedRepresentation <em>Mapped Representation</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mapped Representation</em>' reference.
	 * @see #getMappedRepresentation()
	 * @generated
	 */
	void setMappedRepresentation(IfcRepresentation value);

	/**
	 * Returns the value of the '<em><b>Map Usage</b></em>' reference list.
	 * The list contents are of type {@link cn.dlb.bim.models.ifc2x3tc1.IfcMappedItem}.
	 * It is bidirectional and its opposite is '{@link cn.dlb.bim.models.ifc2x3tc1.IfcMappedItem#getMappingSource <em>Mapping Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Map Usage</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Map Usage</em>' reference list.
	 * @see #isSetMapUsage()
	 * @see #unsetMapUsage()
	 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcRepresentationMap_MapUsage()
	 * @see cn.dlb.bim.models.ifc2x3tc1.IfcMappedItem#getMappingSource
	 * @model opposite="MappingSource" unsettable="true"
	 * @generated
	 */
	EList<IfcMappedItem> getMapUsage();

	/**
	 * Unsets the value of the '{@link cn.dlb.bim.models.ifc2x3tc1.IfcRepresentationMap#getMapUsage <em>Map Usage</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMapUsage()
	 * @see #getMapUsage()
	 * @generated
	 */
	void unsetMapUsage();

	/**
	 * Returns whether the value of the '{@link cn.dlb.bim.models.ifc2x3tc1.IfcRepresentationMap#getMapUsage <em>Map Usage</em>}' reference list is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Map Usage</em>' reference list is set.
	 * @see #unsetMapUsage()
	 * @see #getMapUsage()
	 * @generated
	 */
	boolean isSetMapUsage();

} // IfcRepresentationMap
