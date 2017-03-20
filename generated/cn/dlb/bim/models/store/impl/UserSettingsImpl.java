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
package cn.dlb.bim.models.store.impl;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.ifc.emf.IdEObjectImpl;
import cn.dlb.bim.models.store.DeserializerPluginConfiguration;
import cn.dlb.bim.models.store.InternalServicePluginConfiguration;
import cn.dlb.bim.models.store.ModelComparePluginConfiguration;
import cn.dlb.bim.models.store.ModelMergerPluginConfiguration;
import cn.dlb.bim.models.store.ObjectIDMPluginConfiguration;
import cn.dlb.bim.models.store.QueryEnginePluginConfiguration;
import cn.dlb.bim.models.store.RenderEnginePluginConfiguration;
import cn.dlb.bim.models.store.SerializerPluginConfiguration;
import cn.dlb.bim.models.store.StorePackage;
import cn.dlb.bim.models.store.UserSettings;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>User Settings</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getSerializers <em>Serializers</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getObjectIDMs <em>Object ID Ms</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getRenderEngines <em>Render Engines</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getDeserializers <em>Deserializers</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getQueryEngines <em>Query Engines</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getModelMergers <em>Model Mergers</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getModelCompares <em>Model Compares</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getDefaultModelMerger <em>Default Model Merger</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getDefaultModelCompare <em>Default Model Compare</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getDefaultQueryEngine <em>Default Query Engine</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getDefaultRenderEngine <em>Default Render Engine</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getDefaultSerializer <em>Default Serializer</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getDefaultObjectIDM <em>Default Object IDM</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.UserSettingsImpl#getServices <em>Services</em>}</li>
 * </ul>
 *
 * @generated
 */
public class UserSettingsImpl extends IdEObjectImpl implements UserSettings {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected UserSettingsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return StorePackage.Literals.USER_SETTINGS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected int eStaticFeatureCount() {
		return 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<SerializerPluginConfiguration> getSerializers() {
		return (EList<SerializerPluginConfiguration>) eGet(StorePackage.Literals.USER_SETTINGS__SERIALIZERS, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<ObjectIDMPluginConfiguration> getObjectIDMs() {
		return (EList<ObjectIDMPluginConfiguration>) eGet(StorePackage.Literals.USER_SETTINGS__OBJECT_ID_MS, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<RenderEnginePluginConfiguration> getRenderEngines() {
		return (EList<RenderEnginePluginConfiguration>) eGet(StorePackage.Literals.USER_SETTINGS__RENDER_ENGINES, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<DeserializerPluginConfiguration> getDeserializers() {
		return (EList<DeserializerPluginConfiguration>) eGet(StorePackage.Literals.USER_SETTINGS__DESERIALIZERS, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<QueryEnginePluginConfiguration> getQueryEngines() {
		return (EList<QueryEnginePluginConfiguration>) eGet(StorePackage.Literals.USER_SETTINGS__QUERY_ENGINES, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<ModelMergerPluginConfiguration> getModelMergers() {
		return (EList<ModelMergerPluginConfiguration>) eGet(StorePackage.Literals.USER_SETTINGS__MODEL_MERGERS, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<ModelComparePluginConfiguration> getModelCompares() {
		return (EList<ModelComparePluginConfiguration>) eGet(StorePackage.Literals.USER_SETTINGS__MODEL_COMPARES, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelMergerPluginConfiguration getDefaultModelMerger() {
		return (ModelMergerPluginConfiguration) eGet(StorePackage.Literals.USER_SETTINGS__DEFAULT_MODEL_MERGER, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefaultModelMerger(ModelMergerPluginConfiguration newDefaultModelMerger) {
		eSet(StorePackage.Literals.USER_SETTINGS__DEFAULT_MODEL_MERGER, newDefaultModelMerger);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelComparePluginConfiguration getDefaultModelCompare() {
		return (ModelComparePluginConfiguration) eGet(StorePackage.Literals.USER_SETTINGS__DEFAULT_MODEL_COMPARE, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefaultModelCompare(ModelComparePluginConfiguration newDefaultModelCompare) {
		eSet(StorePackage.Literals.USER_SETTINGS__DEFAULT_MODEL_COMPARE, newDefaultModelCompare);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public QueryEnginePluginConfiguration getDefaultQueryEngine() {
		return (QueryEnginePluginConfiguration) eGet(StorePackage.Literals.USER_SETTINGS__DEFAULT_QUERY_ENGINE, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefaultQueryEngine(QueryEnginePluginConfiguration newDefaultQueryEngine) {
		eSet(StorePackage.Literals.USER_SETTINGS__DEFAULT_QUERY_ENGINE, newDefaultQueryEngine);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RenderEnginePluginConfiguration getDefaultRenderEngine() {
		return (RenderEnginePluginConfiguration) eGet(StorePackage.Literals.USER_SETTINGS__DEFAULT_RENDER_ENGINE, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefaultRenderEngine(RenderEnginePluginConfiguration newDefaultRenderEngine) {
		eSet(StorePackage.Literals.USER_SETTINGS__DEFAULT_RENDER_ENGINE, newDefaultRenderEngine);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SerializerPluginConfiguration getDefaultSerializer() {
		return (SerializerPluginConfiguration) eGet(StorePackage.Literals.USER_SETTINGS__DEFAULT_SERIALIZER, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefaultSerializer(SerializerPluginConfiguration newDefaultSerializer) {
		eSet(StorePackage.Literals.USER_SETTINGS__DEFAULT_SERIALIZER, newDefaultSerializer);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ObjectIDMPluginConfiguration getDefaultObjectIDM() {
		return (ObjectIDMPluginConfiguration) eGet(StorePackage.Literals.USER_SETTINGS__DEFAULT_OBJECT_IDM, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefaultObjectIDM(ObjectIDMPluginConfiguration newDefaultObjectIDM) {
		eSet(StorePackage.Literals.USER_SETTINGS__DEFAULT_OBJECT_IDM, newDefaultObjectIDM);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<InternalServicePluginConfiguration> getServices() {
		return (EList<InternalServicePluginConfiguration>) eGet(StorePackage.Literals.USER_SETTINGS__SERVICES, true);
	}

} //UserSettingsImpl
