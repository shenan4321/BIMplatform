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

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ifc Blob Texture</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link cn.dlb.bim.models.ifc2x3tc1.IfcBlobTexture#getRasterFormat <em>Raster Format</em>}</li>
 *   <li>{@link cn.dlb.bim.models.ifc2x3tc1.IfcBlobTexture#getRasterCode <em>Raster Code</em>}</li>
 * </ul>
 *
 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcBlobTexture()
 * @model
 * @generated
 */
public interface IfcBlobTexture extends IfcSurfaceTexture {
	/**
	 * Returns the value of the '<em><b>Raster Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Raster Format</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Raster Format</em>' attribute.
	 * @see #setRasterFormat(String)
	 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcBlobTexture_RasterFormat()
	 * @model
	 * @generated
	 */
	String getRasterFormat();

	/**
	 * Sets the value of the '{@link cn.dlb.bim.models.ifc2x3tc1.IfcBlobTexture#getRasterFormat <em>Raster Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Raster Format</em>' attribute.
	 * @see #getRasterFormat()
	 * @generated
	 */
	void setRasterFormat(String value);

	/**
	 * Returns the value of the '<em><b>Raster Code</b></em>' attribute.
	 * The literals are from the enumeration {@link cn.dlb.bim.models.ifc2x3tc1.Tristate}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Raster Code</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Raster Code</em>' attribute.
	 * @see cn.dlb.bim.models.ifc2x3tc1.Tristate
	 * @see #setRasterCode(Tristate)
	 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcBlobTexture_RasterCode()
	 * @model
	 * @generated
	 */
	Tristate getRasterCode();

	/**
	 * Sets the value of the '{@link cn.dlb.bim.models.ifc2x3tc1.IfcBlobTexture#getRasterCode <em>Raster Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Raster Code</em>' attribute.
	 * @see cn.dlb.bim.models.ifc2x3tc1.Tristate
	 * @see #getRasterCode()
	 * @generated
	 */
	void setRasterCode(Tristate value);

} // IfcBlobTexture
