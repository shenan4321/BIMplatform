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
package cn.dlb.bim.models.store;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Query Engine Plugin Configuration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link cn.dlb.bim.models.store.QueryEnginePluginConfiguration#getUserSettings <em>User Settings</em>}</li>
 * </ul>
 *
 * @see cn.dlb.bim.models.store.StorePackage#getQueryEnginePluginConfiguration()
 * @model
 * @generated
 */
public interface QueryEnginePluginConfiguration extends PluginConfiguration {
	/**
	 * Returns the value of the '<em><b>User Settings</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link cn.dlb.bim.models.store.UserSettings#getQueryEngines <em>Query Engines</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>User Settings</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>User Settings</em>' reference.
	 * @see #setUserSettings(UserSettings)
	 * @see cn.dlb.bim.models.store.StorePackage#getQueryEnginePluginConfiguration_UserSettings()
	 * @see cn.dlb.bim.models.store.UserSettings#getQueryEngines
	 * @model opposite="queryEngines"
	 * @generated
	 */
	UserSettings getUserSettings();

	/**
	 * Sets the value of the '{@link cn.dlb.bim.models.store.QueryEnginePluginConfiguration#getUserSettings <em>User Settings</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>User Settings</em>' reference.
	 * @see #getUserSettings()
	 * @generated
	 */
	void setUserSettings(UserSettings value);

} // QueryEnginePluginConfiguration
