package de.dfki.appsensor.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import de.dfki.appsensor.R;
import de.dfki.appsensor.data.AppUsageProvider;


/**
* @author Matthias Boehmer, matthias.boehmer@dfki.de
*/
public class Authenticator extends AbstractAccountAuthenticator {

	private final Context mContext;
    
    private AccountManager mAccountManager;
    
    public static final String ACCOUNT_TYPE = "de.dfki.appsensor.ACCOUNT_TYPE";
    public static final String AUTHTOKEN_TYPE = "de.dfki.appsensor.AUTHTOKEN_TYPE";
    
    public static final String ACCOUNT_NAME = "appsensor"; 
    public static final String ACCOUNT_PASSWORD = "vICOniSeGE";

    public Authenticator(Context context) {
        super(context);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    /**
     * Creates a new account for sync, and sets sync to be enabled by default.
     * 
     * @param am
     * @return
     */
    public static boolean createAccount(AccountManager am) {
		Account account = new Account(Authenticator.ACCOUNT_NAME, Authenticator.ACCOUNT_TYPE);
		boolean accountCreated = am.addAccountExplicitly(account, Authenticator.ACCOUNT_PASSWORD, null);
		ContentResolver.setIsSyncable(account, AppUsageProvider.AUTHORITY, 1);
    	ContentResolver.setSyncAutomatically(account, AppUsageProvider.AUTHORITY, true);
    	return accountCreated;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {
    	
    	if (mAccountManager.getAccountsByType(ACCOUNT_TYPE).length <= 0) {
    		createAccount(AccountManager.get(mContext));
    	}
        
        final Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, ACCOUNT_NAME);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
        Account account, Bundle options) {
        if (options != null && options.containsKey(AccountManager.KEY_PASSWORD)) {
            final String password = options.getString(AccountManager.KEY_PASSWORD);
            final boolean verified = ((account.name == ACCOUNT_NAME) && (password == ACCOUNT_PASSWORD));
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, verified);
            return result;
        }
        
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, account.name == ACCOUNT_NAME);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
        String accountType) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
        Account account, String authTokenType, Bundle loginOptions) {
        final Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, ACCOUNT_NAME);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        result.putString(AccountManager.KEY_AUTHTOKEN, ACCOUNT_PASSWORD);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (authTokenType.equals(AUTHTOKEN_TYPE)) {
            return mContext.getString(R.string.app_name);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
        Account account, String[] features) {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
        Account account, String authTokenType, Bundle loginOptions) {
    	throw new UnsupportedOperationException();
    }

}
