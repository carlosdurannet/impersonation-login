package net.carlosduran.liferay.service.impl;

import java.util.Date;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.Authenticator;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceWrapper;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;

import net.carlosduran.liferay.impersonation.sb.model.ImpersonationRegistry;
import net.carlosduran.liferay.impersonation.sb.service.ImpersonationRegistryLocalServiceUtil;
import net.carlosduran.liferay.service.ex.UserNotFoundException;
import net.carlosduran.liferay.service.util.ImpersonationConstants;

/**
 * @author Carlos Durán
 * https://carlosduran.net
 */
@Component(
	immediate = true,
	property = {
	},
	service = ServiceWrapper.class
)
public class UserLocalServiceImpl extends UserLocalServiceWrapper {
	
	
	private static final Log logger = LogFactoryUtil.getLog(UserLocalServiceImpl.class);

	public UserLocalServiceImpl() {
		super(null);
	}
	
	@Override
	public int authenticateByScreenName(long companyId, String screenName, String password,
			Map<String, String[]> headerMap, Map<String, String[]> parameterMap, Map<String, Object> resultsMap)
			throws PortalException, SystemException {
		
		User impersonationUser = null;
		String impersonationScreenName = StringPool.BLANK;
		int result = ImpersonationConstants.NO_IMPERSONATION;
		boolean impersonationMode = screenName.contains(StringPool.POUND);
		
		if(impersonationMode) {
			String[] impersonationComposition = screenName.split(StringPool.POUND);
			screenName = impersonationComposition[0].toLowerCase();
			impersonationScreenName = impersonationComposition[1].toLowerCase();
			try {
				impersonationUser = getUser(companyId, impersonationScreenName);
			} catch (UserNotFoundException userNotFoundEx) {
				result = ImpersonationConstants.IMPERSONATION_RESULT_USER_UNAVAILABLE;
				logger.warn("Cannot get the user to impersonate: " + userNotFoundEx.getMessage());
			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
			}
		}
		
		int authenticateResult = super.authenticateByScreenName(companyId, screenName, password, headerMap, parameterMap, resultsMap);
		
		if(impersonationMode && authenticateResult == Authenticator.SUCCESS) {

			long impersonationregistryId = CounterLocalServiceUtil.increment(ImpersonationRegistry.class.getName());

			ImpersonationRegistry impersonationRegistry = ImpersonationRegistryLocalServiceUtil.createImpersonationRegistry(impersonationregistryId);

			logger.info(screenName + " wants to impersonate " + impersonationScreenName);
			long userId = GetterUtil.getLong(resultsMap.get(ImpersonationConstants.KEY_USERID));
			impersonationRegistry.setCompanyId(companyId);
			impersonationRegistry.setOperationDate(new Date());
			impersonationRegistry.setUserId(userId);
			impersonationRegistry.setScreenName(screenName);
			
			if(Validator.isNotNull(impersonationUser)) {
				impersonationRegistry.setImpersonatedUserId(impersonationUser.getUserId());
				impersonationRegistry.setImpersonatedScreenName(impersonationUser.getScreenName());
				if(canImpersonate(companyId, userId)) {
					resultsMap.put(ImpersonationConstants.KEY_USER, impersonationUser);
					resultsMap.put(ImpersonationConstants.KEY_USERID, impersonationUser.getUserId());
					result = ImpersonationConstants.IMPERSONATION_RESULT_GRANTED;
					logger.info("User " + screenName + " has impersonated " + impersonationUser.getScreenName().toUpperCase());
				} else {
					result = ImpersonationConstants.IMPERSONATION_RESULT_DENIED;
					logger.info("User " + screenName + " can't impersonate " + impersonationUser.getScreenName().toUpperCase());
				}
			} else {
				impersonationRegistry.setImpersonatedUserId(0);
				impersonationRegistry.setImpersonatedScreenName(impersonationScreenName);
			}
			impersonationRegistry.setOperationResult(result);
			ImpersonationRegistryLocalServiceUtil.addImpersonationRegistry(impersonationRegistry);
		}
		
		return authenticateResult;
	}
	
	/**
	 * Checks if user has impersonation rights
	 * @param companyId Company ID
	 * @param userId User ID
	 * @return <strong>true</strong> if user has impersonate rights. Otherwise, <strong>false</strong>
	 */
	private static boolean canImpersonate(long companyId, long userId) {
		
		String impersonationRoleName = getImpersonationRoleName();
		logger.debug("Impersonation Role Name: " + impersonationRoleName);
		
		try {			
			return RoleLocalServiceUtil.hasUserRole(userId, companyId, impersonationRoleName, Boolean.TRUE);
		} catch (Exception ex) {
			logger.error(ex.getClass().getName() + ": " + ex.getMessage());
		}

		return false;
	}

	/**
	 * Retrieves the impersonation role name
	 * @return The value set in <strong>impersonation-role</strong> property. If it is not set, uses the default value (<em>ImpersonationUser</em>)
	 */
	private static String getImpersonationRoleName() {
		String impersonationRoleName = GetterUtil.getString(PropsUtil.get(ImpersonationConstants.PROPERTY_IMPERSONATION_ROLE));
		
		if (Validator.isBlank(impersonationRoleName)) {
			logger.debug("Impersonation role is not defined (impersonation-role property). Using default");
			impersonationRoleName = ImpersonationConstants.DEFAULT_IMPERSONATION_ROLE_NAME;
		}
		return impersonationRoleName;
	}
	
	/**
	 * Retrieves an user with the screen name
	 * @param companyId company ID
	 * @param screenName User screen name
	 * @return The user with the provided screen name or <em>null</em> if it doesn't exist
	 * @throws UserNotFoundException Usuario no encontrado
	 */
	private static User getUser(long companyId, String screenName) throws UserNotFoundException {
		User user;
		try {
			user = UserLocalServiceUtil.getUserByScreenName(companyId, screenName);
		} catch (Exception ex) {
			throw new UserNotFoundException("User " + screenName + " not found");
		}
		
		return user;
	}

}