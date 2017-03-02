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
package cn.dlb.bim.models.log.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

import cn.dlb.bim.models.log.CheckoutRelated;
import cn.dlb.bim.models.log.DatabaseCreated;
import cn.dlb.bim.models.log.Download;
import cn.dlb.bim.models.log.ExtendedDataAddedToProject;
import cn.dlb.bim.models.log.ExtendedDataAddedToRevision;
import cn.dlb.bim.models.log.GeoTagUpdated;
import cn.dlb.bim.models.log.LogAction;
import cn.dlb.bim.models.log.LogPackage;
import cn.dlb.bim.models.log.NewCheckoutAdded;
import cn.dlb.bim.models.log.NewObjectIDMUploaded;
import cn.dlb.bim.models.log.NewProjectAdded;
import cn.dlb.bim.models.log.NewRevisionAdded;
import cn.dlb.bim.models.log.NewUserAdded;
import cn.dlb.bim.models.log.PasswordChanged;
import cn.dlb.bim.models.log.PasswordReset;
import cn.dlb.bim.models.log.ProjectDeleted;
import cn.dlb.bim.models.log.ProjectRelated;
import cn.dlb.bim.models.log.ProjectUndeleted;
import cn.dlb.bim.models.log.ProjectUpdated;
import cn.dlb.bim.models.log.RemoteServiceCalled;
import cn.dlb.bim.models.log.RevisionBranched;
import cn.dlb.bim.models.log.RevisionRelated;
import cn.dlb.bim.models.log.RevisionUpdated;
import cn.dlb.bim.models.log.ServerLog;
import cn.dlb.bim.models.log.ServerStarted;
import cn.dlb.bim.models.log.SettingsSaved;
import cn.dlb.bim.models.log.UserAddedToProject;
import cn.dlb.bim.models.log.UserChanged;
import cn.dlb.bim.models.log.UserDeleted;
import cn.dlb.bim.models.log.UserRelated;
import cn.dlb.bim.models.log.UserRemovedFromProject;
import cn.dlb.bim.models.log.UserUndeleted;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see cn.dlb.bim.models.log.LogPackage
 * @generated
 */
public class LogAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static LogPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LogAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = LogPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject) object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LogSwitch<Adapter> modelSwitch = new LogSwitch<Adapter>() {
		@Override
		public Adapter caseLogAction(LogAction object) {
			return createLogActionAdapter();
		}

		@Override
		public Adapter caseServerLog(ServerLog object) {
			return createServerLogAdapter();
		}

		@Override
		public Adapter caseProjectRelated(ProjectRelated object) {
			return createProjectRelatedAdapter();
		}

		@Override
		public Adapter caseCheckoutRelated(CheckoutRelated object) {
			return createCheckoutRelatedAdapter();
		}

		@Override
		public Adapter caseRevisionRelated(RevisionRelated object) {
			return createRevisionRelatedAdapter();
		}

		@Override
		public Adapter caseUserRelated(UserRelated object) {
			return createUserRelatedAdapter();
		}

		@Override
		public Adapter caseNewUserAdded(NewUserAdded object) {
			return createNewUserAddedAdapter();
		}

		@Override
		public Adapter caseNewProjectAdded(NewProjectAdded object) {
			return createNewProjectAddedAdapter();
		}

		@Override
		public Adapter caseRevisionBranched(RevisionBranched object) {
			return createRevisionBranchedAdapter();
		}

		@Override
		public Adapter caseNewRevisionAdded(NewRevisionAdded object) {
			return createNewRevisionAddedAdapter();
		}

		@Override
		public Adapter caseNewCheckoutAdded(NewCheckoutAdded object) {
			return createNewCheckoutAddedAdapter();
		}

		@Override
		public Adapter caseSettingsSaved(SettingsSaved object) {
			return createSettingsSavedAdapter();
		}

		@Override
		public Adapter caseUserAddedToProject(UserAddedToProject object) {
			return createUserAddedToProjectAdapter();
		}

		@Override
		public Adapter caseNewObjectIDMUploaded(NewObjectIDMUploaded object) {
			return createNewObjectIDMUploadedAdapter();
		}

		@Override
		public Adapter caseDownload(Download object) {
			return createDownloadAdapter();
		}

		@Override
		public Adapter caseUserRemovedFromProject(UserRemovedFromProject object) {
			return createUserRemovedFromProjectAdapter();
		}

		@Override
		public Adapter caseProjectDeleted(ProjectDeleted object) {
			return createProjectDeletedAdapter();
		}

		@Override
		public Adapter caseUserDeleted(UserDeleted object) {
			return createUserDeletedAdapter();
		}

		@Override
		public Adapter casePasswordReset(PasswordReset object) {
			return createPasswordResetAdapter();
		}

		@Override
		public Adapter caseDatabaseCreated(DatabaseCreated object) {
			return createDatabaseCreatedAdapter();
		}

		@Override
		public Adapter caseServerStarted(ServerStarted object) {
			return createServerStartedAdapter();
		}

		@Override
		public Adapter caseProjectUpdated(ProjectUpdated object) {
			return createProjectUpdatedAdapter();
		}

		@Override
		public Adapter caseUserUndeleted(UserUndeleted object) {
			return createUserUndeletedAdapter();
		}

		@Override
		public Adapter caseProjectUndeleted(ProjectUndeleted object) {
			return createProjectUndeletedAdapter();
		}

		@Override
		public Adapter caseRevisionUpdated(RevisionUpdated object) {
			return createRevisionUpdatedAdapter();
		}

		@Override
		public Adapter caseGeoTagUpdated(GeoTagUpdated object) {
			return createGeoTagUpdatedAdapter();
		}

		@Override
		public Adapter casePasswordChanged(PasswordChanged object) {
			return createPasswordChangedAdapter();
		}

		@Override
		public Adapter caseUserChanged(UserChanged object) {
			return createUserChangedAdapter();
		}

		@Override
		public Adapter caseExtendedDataAddedToRevision(ExtendedDataAddedToRevision object) {
			return createExtendedDataAddedToRevisionAdapter();
		}

		@Override
		public Adapter caseExtendedDataAddedToProject(ExtendedDataAddedToProject object) {
			return createExtendedDataAddedToProjectAdapter();
		}

		@Override
		public Adapter caseRemoteServiceCalled(RemoteServiceCalled object) {
			return createRemoteServiceCalledAdapter();
		}

		@Override
		public Adapter defaultCase(EObject object) {
			return createEObjectAdapter();
		}
	};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject) target);
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.LogAction <em>Action</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.LogAction
	 * @generated
	 */
	public Adapter createLogActionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.ServerLog <em>Server Log</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.ServerLog
	 * @generated
	 */
	public Adapter createServerLogAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.ProjectRelated <em>Project Related</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.ProjectRelated
	 * @generated
	 */
	public Adapter createProjectRelatedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.CheckoutRelated <em>Checkout Related</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.CheckoutRelated
	 * @generated
	 */
	public Adapter createCheckoutRelatedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.RevisionRelated <em>Revision Related</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.RevisionRelated
	 * @generated
	 */
	public Adapter createRevisionRelatedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.UserRelated <em>User Related</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.UserRelated
	 * @generated
	 */
	public Adapter createUserRelatedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.NewUserAdded <em>New User Added</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.NewUserAdded
	 * @generated
	 */
	public Adapter createNewUserAddedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.NewProjectAdded <em>New Project Added</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.NewProjectAdded
	 * @generated
	 */
	public Adapter createNewProjectAddedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.RevisionBranched <em>Revision Branched</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.RevisionBranched
	 * @generated
	 */
	public Adapter createRevisionBranchedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.NewRevisionAdded <em>New Revision Added</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.NewRevisionAdded
	 * @generated
	 */
	public Adapter createNewRevisionAddedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.NewCheckoutAdded <em>New Checkout Added</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.NewCheckoutAdded
	 * @generated
	 */
	public Adapter createNewCheckoutAddedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.SettingsSaved <em>Settings Saved</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.SettingsSaved
	 * @generated
	 */
	public Adapter createSettingsSavedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.UserAddedToProject <em>User Added To Project</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.UserAddedToProject
	 * @generated
	 */
	public Adapter createUserAddedToProjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.NewObjectIDMUploaded <em>New Object IDM Uploaded</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.NewObjectIDMUploaded
	 * @generated
	 */
	public Adapter createNewObjectIDMUploadedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.Download <em>Download</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.Download
	 * @generated
	 */
	public Adapter createDownloadAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.UserRemovedFromProject <em>User Removed From Project</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.UserRemovedFromProject
	 * @generated
	 */
	public Adapter createUserRemovedFromProjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.ProjectDeleted <em>Project Deleted</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.ProjectDeleted
	 * @generated
	 */
	public Adapter createProjectDeletedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.UserDeleted <em>User Deleted</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.UserDeleted
	 * @generated
	 */
	public Adapter createUserDeletedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.PasswordReset <em>Password Reset</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.PasswordReset
	 * @generated
	 */
	public Adapter createPasswordResetAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.DatabaseCreated <em>Database Created</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.DatabaseCreated
	 * @generated
	 */
	public Adapter createDatabaseCreatedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.ServerStarted <em>Server Started</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.ServerStarted
	 * @generated
	 */
	public Adapter createServerStartedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.ProjectUpdated <em>Project Updated</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.ProjectUpdated
	 * @generated
	 */
	public Adapter createProjectUpdatedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.UserUndeleted <em>User Undeleted</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.UserUndeleted
	 * @generated
	 */
	public Adapter createUserUndeletedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.ProjectUndeleted <em>Project Undeleted</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.ProjectUndeleted
	 * @generated
	 */
	public Adapter createProjectUndeletedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.RevisionUpdated <em>Revision Updated</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.RevisionUpdated
	 * @generated
	 */
	public Adapter createRevisionUpdatedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.GeoTagUpdated <em>Geo Tag Updated</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.GeoTagUpdated
	 * @generated
	 */
	public Adapter createGeoTagUpdatedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.PasswordChanged <em>Password Changed</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.PasswordChanged
	 * @generated
	 */
	public Adapter createPasswordChangedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.UserChanged <em>User Changed</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.UserChanged
	 * @generated
	 */
	public Adapter createUserChangedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.ExtendedDataAddedToRevision <em>Extended Data Added To Revision</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.ExtendedDataAddedToRevision
	 * @generated
	 */
	public Adapter createExtendedDataAddedToRevisionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.ExtendedDataAddedToProject <em>Extended Data Added To Project</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.ExtendedDataAddedToProject
	 * @generated
	 */
	public Adapter createExtendedDataAddedToProjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link cn.dlb.bim.models.log.RemoteServiceCalled <em>Remote Service Called</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see cn.dlb.bim.models.log.RemoteServiceCalled
	 * @generated
	 */
	public Adapter createRemoteServiceCalledAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //LogAdapterFactory
