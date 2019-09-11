package net.carlosduran.liferay.service.util;

public class ImpersonationConstants {
	
	public static final String DEFAULT_IMPERSONATION_ROLE_NAME = "ImpersonationUser";
	public static final String KEY_USER = "user";
	public static final String KEY_USERID = "userId";
	public static final String PROPERTY_IMPERSONATION_ROLE = "impersonation-role";
	
	public static final int IMPRESONATION_NO_RESULT = -1;
	public static final int IMPRESONATION_RESULT_DENIED = 2;
	public static final int IMPRESONATION_RESULT_GRANTED = 0;
	public static final int IMPRESONATION_RESULT_USER_UNAVAILABLE = 1;
}
