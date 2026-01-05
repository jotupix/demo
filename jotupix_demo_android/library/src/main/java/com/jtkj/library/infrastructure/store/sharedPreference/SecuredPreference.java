/*
 * Copyright (C) 2015, Scott Alexander-Bown, Daniel Abraham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jtkj.library.infrastructure.store.sharedPreference;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.tozny.crypto.android.AesCbcWithIntegrity;
import com.jtkj.library.commom.logger.CLog;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * https://github.com/scottyab/secure-preferences
 */
public class SecuredPreference implements SharedPreferences {
	private static final String TAG = SecuredPreference.class.getSimpleName();

	private static final int ORIGINAL_ITERATION_COUNT = 10000;

	//the backing pref file
	private SharedPreferences sharedPreferences;

	//secret keys used for enc and dec
	private AesCbcWithIntegrity.SecretKeys keys;

	private static boolean sLoggingEnabled = false;

	//name of the currently loaded sharedPrefFile, can be null if default
	private String sharedPrefFilename;

	/**
	 * User password defaults to app generated password that's stores obfucated with the other preference values.
	 * Also this uses the Default shared pref file
	 *
	 * @param ctx should be ApplicationContext not Activity
	 */
	public SecuredPreference(Context ctx) {
		this(ctx, "", null);
	}

	/**
	 * @param ctx            should be ApplicationContext not Activity
	 * @param iterationCount The iteration count for the keys generation
	 */
	public SecuredPreference(Context ctx, int iterationCount) {
		this(ctx, "", null, iterationCount);
	}

	/**
	 * @param ctx                should be ApplicationContext not Activity
	 * @param password           user password/code used to generate encryption key.
	 * @param sharedPrefFilename name of the shared pref file. If null use the default shared prefs
	 */
	public SecuredPreference(Context ctx, final String password, final String sharedPrefFilename) {
		this(ctx, null, password, sharedPrefFilename, ORIGINAL_ITERATION_COUNT);
	}

	/**
	 * @param ctx            should be ApplicationContext not Activity
	 * @param iterationCount The iteration count for the keys generation
	 */
	public SecuredPreference(Context ctx, final String password, final String sharedPrefFilename, int iterationCount) {
		this(ctx, null, password, sharedPrefFilename, iterationCount);
	}

	/**
	 * @param ctx                should be ApplicationContext not Activity
	 * @param secretKey          that you've generated
	 * @param sharedPrefFilename name of the shared pref file. If null use the default shared prefs
	 */
	public SecuredPreference(Context ctx, final AesCbcWithIntegrity.SecretKeys secretKey, final String sharedPrefFilename) {
		this(ctx, secretKey, null, sharedPrefFilename, 0);
	}

	private SecuredPreference(Context ctx, final AesCbcWithIntegrity.SecretKeys secretKey,
							  final String password, final String sharedPrefFilename, int iterationCount) {
		CLog.i(TAG, "sharedPrefFilename=" + sharedPrefFilename);
		if (sharedPreferences == null) {
			sharedPreferences = getSharedPreferenceFile(ctx, sharedPrefFilename);
		}

		if (secretKey != null) {
			keys = secretKey;
		} else if (TextUtils.isEmpty(password)) {
			// Initialize or create encryption key
			try {
				final String key = generateAesKeyName(ctx, iterationCount);
				String keyAsString = sharedPreferences.getString(key, null);
				if (keyAsString == null) {
					keys = AesCbcWithIntegrity.generateKey();
					//saving new key
					boolean committed = sharedPreferences.edit().putString(key, keys.toString()).commit();
					if (!committed) {
						Log.w(TAG, "Key not committed to prefs");
					}
				} else {
					keys = AesCbcWithIntegrity.keys(keyAsString);
				}

				if (keys == null) {
					throw new GeneralSecurityException("Problem generating Key");
				}
			} catch (GeneralSecurityException e) {
				if (sLoggingEnabled) {
					Log.e(TAG, "Error init:" + e.getMessage());
				}
				throw new IllegalStateException(e);
			}
		} else {
			//use the password to generate the key
			try {
				final byte[] salt = getDeviceSerialNumber(ctx).getBytes();
				keys = AesCbcWithIntegrity.generateKeyFromPassword(password, salt, iterationCount);
				if (keys == null) {
					throw new GeneralSecurityException("Problem generating Key From Password");
				}
			} catch (GeneralSecurityException e) {
				if (sLoggingEnabled) {
					Log.e(TAG, "Error init using user password:" + e.getMessage());
				}
				throw new IllegalStateException(e);
			}
		}
	}

	public static SharedPreferences useAndroidSharedPreference(Context ctx, String prefFilename) {
		return ctx.getSharedPreferences(prefFilename, Context.MODE_PRIVATE);
	}

	/**
	 * if a prefFilename is not defined the getDefaultSharedPreferences is used.
	 *
	 * @param ctx should be ApplicationContext not Activity
	 */
	private SharedPreferences getSharedPreferenceFile(Context ctx, String prefFilename) {
		this.sharedPrefFilename = prefFilename;
		if (TextUtils.isEmpty(prefFilename)) {
			return PreferenceManager.getDefaultSharedPreferences(ctx);
		} else {
			return ctx.getSharedPreferences(prefFilename, Context.MODE_PRIVATE);
		}
	}

	/**
	 * nulls in memory keys
	 */
	public void destroyKeys() {
		keys = null;
	}

	/**
	 * Uses device and application values to generate the pref key for the encryption key
	 *
	 * @param ctx            should be ApplicationContext not Activity
	 * @param iterationCount The iteration count for the keys generation
	 * @return String to be used as the AESkey Pref key
	 * @throws GeneralSecurityException if something goes wrong in generation
	 */
	private String generateAesKeyName(Context ctx, int iterationCount) throws GeneralSecurityException {
		final String password = ctx.getPackageName();
		final byte[] salt = getDeviceSerialNumber(ctx).getBytes();
		AesCbcWithIntegrity.SecretKeys generatedKeyName = AesCbcWithIntegrity.generateKeyFromPassword(password, salt, iterationCount);
		return hashPrefKey(generatedKeyName.toString());
	}

	/**
	 * Gets the hardware serial number of this device.
	 *
	 * @return serial number or Settings.Secure.ANDROID_ID if not available.
	 */
	@SuppressLint("HardwareIds")
	private static String getDeviceSerialNumber(Context ctx) {
		// We're using the Reflection API because Build.SERIAL is only available
		// since API Level 9 (Gingerbread, Android 2.3).
		try {
			String deviceSerial = (String) Build.class.getField("SERIAL").get(null);
			if (TextUtils.isEmpty(deviceSerial)) {
				return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
			} else {
				return deviceSerial;
			}
		} catch (Exception ignored) {
			// Fall ic_back  to Android_ID
			return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
		}
	}

	/**
	 * The Pref keys must be same each time so we're using a hash to obscure the stored value
	 *
	 * @param prefKey the key of preference
	 * @return SHA-256 Hash of the preference key
	 */
	public static String hashPrefKey(String prefKey) {
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = prefKey.getBytes("UTF-8");
			digest.update(bytes, 0, bytes.length);
			return Base64.encodeToString(digest.digest(), AesCbcWithIntegrity.BASE64_FLAGS);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			if (sLoggingEnabled) {
				Log.w(TAG, "Problem generating hash", e);
			}
		}
		return null;
	}

	private String encrypt(String cleartext) {
		if (TextUtils.isEmpty(cleartext)) {
			return cleartext;
		}
		try {
			return AesCbcWithIntegrity.encrypt(cleartext, keys).toString();
		} catch (GeneralSecurityException e) {
			if (sLoggingEnabled) {
				Log.w(TAG, "encrypt", e);
			}
			return null;
		} catch (UnsupportedEncodingException e) {
			if (sLoggingEnabled) {
				Log.w(TAG, "encrypt", e);
			}
		}
		return null;
	}

	/**
	 * @return decrypted plain text, unless decryption fails, in which case null
	 */
	private String decrypt(final String cipherText) {
		if (TextUtils.isEmpty(cipherText)) {
			return cipherText;
		}
		try {
			AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(cipherText);
			return AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			if (sLoggingEnabled) {
				Log.w(TAG, "decrypt", e);
			}
		}
		return null;
	}

	/**
	 * @return map of with decrypted values (excluding the key if present)
	 */
	@Override
	public Map<String, String> getAll() {
		//wont be null as per http://androidxref.com/5.1.0_r1/xref/frameworks/base/core/java/android/app/SharedPreferencesImpl.java
		final Map<String, ?> encryptedMap = sharedPreferences.getAll();
		final Map<String, String> decryptedMap = new HashMap<>(encryptedMap.size());
		for (Entry<String, ?> entry : encryptedMap.entrySet()) {
			try {
				Object cipherText = entry.getValue();
				//don't include the key
				if (cipherText != null && !cipherText.equals(keys.toString())) {
					//the prefs should all be strings
					decryptedMap.put(entry.getKey(), decrypt(cipherText.toString()));
				}
			} catch (Exception e) {
				if (sLoggingEnabled) {
					Log.w(TAG, "error during getAll", e);
				}
				// Ignore issues that unencrypted values and use instead raw cipher text string
				decryptedMap.put(entry.getKey(), entry.getValue().toString());
			}
		}
		return decryptedMap;
	}

	@Override
	public String getString(String key, String defaultValue) {
		final String encryptedValue = sharedPreferences.getString(SecuredPreference.hashPrefKey(key), null);
		return (encryptedValue != null) ? decrypt(encryptedValue) : defaultValue;
	}

	/**
	 * Added to get a values as as it can be useful to store values that are
	 * already encrypted and encoded
	 *
	 * @param key pref key
	 * @return Encrypted value of the key or the defaultValue if no value exists
	 */
	public String getEncryptedString(String key, String defaultValue) {
		final String encryptedValue = sharedPreferences.getString(SecuredPreference.hashPrefKey(key), null);
		return (encryptedValue != null) ? encryptedValue : defaultValue;
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Set<String> getStringSet(String key, Set<String> defaultValues) {
		final Set<String> encryptedSet = sharedPreferences.getStringSet(SecuredPreference.hashPrefKey(key), null);
		if (encryptedSet == null) {
			return defaultValues;
		}
		final Set<String> decryptedSet = new HashSet<>(encryptedSet.size());
		for (String encryptedValue : encryptedSet) {
			decryptedSet.add(decrypt(encryptedValue));
		}
		return decryptedSet;
	}

	@Override
	public int getInt(String key, int defaultValue) {
		final String encryptedValue = sharedPreferences.getString(SecuredPreference.hashPrefKey(key), null);
		if (encryptedValue == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(decrypt(encryptedValue));
		} catch (NumberFormatException e) {
			throw new ClassCastException(e.getMessage());
		}
	}

	@Override
	public long getLong(String key, long defaultValue) {
		final String encryptedValue = sharedPreferences.getString(SecuredPreference.hashPrefKey(key), null);
		if (encryptedValue == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(decrypt(encryptedValue));
		} catch (NumberFormatException e) {
			throw new ClassCastException(e.getMessage());
		}
	}

	@Override
	public float getFloat(String key, float defaultValue) {
		final String encryptedValue = sharedPreferences.getString(SecuredPreference.hashPrefKey(key), null);
		if (encryptedValue == null) {
			return defaultValue;
		}
		try {
			return Float.parseFloat(decrypt(encryptedValue));
		} catch (NumberFormatException e) {
			throw new ClassCastException(e.getMessage());
		}
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		final String encryptedValue = sharedPreferences.getString(SecuredPreference.hashPrefKey(key), null);
		if (encryptedValue == null) {
			return defaultValue;
		}
		try {
			return Boolean.parseBoolean(decrypt(encryptedValue));
		} catch (NumberFormatException e) {
			throw new ClassCastException(e.getMessage());
		}
	}

	@Override
	public boolean contains(String key) {
		return sharedPreferences.contains(SecuredPreference.hashPrefKey(key));
	}

	/**
	 * Cycle through the unEncrypt all the current prefs to mem cache, clear, then encypt with key generated from new password.
	 * This method can be used if switching from the generated key to a key derived from user password
	 * <p>
	 * Note: the pref keys will remain the same as they are SHA256 hashes.
	 *
	 * @param ctx            should be ApplicationContext not Activity
	 * @param iterationCount The iteration count for the keys generation
	 */
	@SuppressLint("CommitPrefEdits")
	public void handlePasswordChange(String newPassword, Context ctx, int iterationCount) throws GeneralSecurityException {

		final byte[] salt = getDeviceSerialNumber(ctx).getBytes();
		AesCbcWithIntegrity.SecretKeys newKey = AesCbcWithIntegrity.generateKeyFromPassword(newPassword, salt, iterationCount);

		Map<String, ?> allOfThePrefs = sharedPreferences.getAll();
		Map<String, String> unencryptedPrefs = new HashMap<>(allOfThePrefs.size());
		//iterate through the current prefs unEncrypting each one
		for (String prefKey : allOfThePrefs.keySet()) {
			Object prefValue = allOfThePrefs.get(prefKey);
			if (prefValue instanceof String) {
				//all the encrypted values will be Strings
				final String prefValueString = (String) prefValue;
				final String plainTextPrefValue = decrypt(prefValueString);
				unencryptedPrefs.put(prefKey, plainTextPrefValue);
			}
		}

		//destroy and clear the current pref file
		destroyKeys();

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();

		//refresh the sharedPreferences object ref: I found it was retaining old ref/values
		sharedPreferences = null;
		sharedPreferences = getSharedPreferenceFile(ctx, sharedPrefFilename);

		//assign new key
		this.keys = newKey;

		SharedPreferences.Editor updatedEditor = sharedPreferences.edit();

		//iterate through the unencryptedPrefs encrypting each one with new key
		Iterator<String> unencryptedPrefsKeys = unencryptedPrefs.keySet().iterator();
		while (unencryptedPrefsKeys.hasNext()) {
			String prefKey = unencryptedPrefsKeys.next();
			String prefPlainText = unencryptedPrefs.get(prefKey);
			updatedEditor.putString(prefKey, encrypt(prefPlainText));
		}
		updatedEditor.commit();
	}

	public void handlePasswordChange(String newPassword, Context ctx) throws GeneralSecurityException {
		handlePasswordChange(newPassword, ctx, ORIGINAL_ITERATION_COUNT);
	}

	@Override
	public Editor edit() {
		return new Editor();
	}

	/**
	 * Wrapper for Android's {@link SharedPreferences.Editor}.
	 * <p>
	 * Used for modifying values in a {@link SecuredPreference} object. All
	 * changes you make in an editor are batched, and not copied ic_back to the
	 * original {@link SecuredPreference} until you call {@link #commit()} or
	 * {@link #apply()}.
	 */
	public final class Editor implements SharedPreferences.Editor {
		private SharedPreferences.Editor mEditor;

		/**
		 * Constructor.
		 */
		private Editor() {
			mEditor = sharedPreferences.edit();
		}

		@Override
		public SharedPreferences.Editor putString(String key, String value) {
			mEditor.putString(SecuredPreference.hashPrefKey(key), encrypt(value));
			return this;
		}

		/**
		 * This is useful for storing values that have be encrypted by something
		 * else or for testing
		 *
		 * @param key   - encrypted as usual
		 * @param value will not be encrypted
		 * @return
		 */
		public SharedPreferences.Editor putUnencryptedString(String key, String value) {
			mEditor.putString(SecuredPreference.hashPrefKey(key), value);
			return this;
		}

		@Override
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
			final Set<String> encryptedValues = new HashSet<>(values.size());
			for (String value : values) {
				encryptedValues.add(encrypt(value));
			}
			mEditor.putStringSet(SecuredPreference.hashPrefKey(key), encryptedValues);
			return this;
		}

		@Override
		public SharedPreferences.Editor putInt(String key, int value) {
			mEditor.putString(SecuredPreference.hashPrefKey(key), encrypt(Integer.toString(value)));
			return this;
		}

		@Override
		public SharedPreferences.Editor putLong(String key, long value) {
			mEditor.putString(SecuredPreference.hashPrefKey(key), encrypt(Long.toString(value)));
			return this;
		}

		@Override
		public SharedPreferences.Editor putFloat(String key, float value) {
			mEditor.putString(SecuredPreference.hashPrefKey(key), encrypt(Float.toString(value)));
			return this;
		}

		@Override
		public SharedPreferences.Editor putBoolean(String key, boolean value) {
			mEditor.putString(SecuredPreference.hashPrefKey(key), encrypt(Boolean.toString(value)));
			return this;
		}

		@Override
		public SharedPreferences.Editor remove(String key) {
			mEditor.remove(SecuredPreference.hashPrefKey(key));
			return this;
		}

		@Override
		public SharedPreferences.Editor clear() {
			mEditor.clear();
			return this;
		}

		@Override
		public boolean commit() {
			return mEditor.commit();
		}

		@Override
		public void apply() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				mEditor.apply();
			} else {
				commit();
			}
		}
	}

	public static boolean isLoggingEnabled() {
		return sLoggingEnabled;
	}

	public static void setLoggingEnabled(boolean loggingEnabled) {
		sLoggingEnabled = loggingEnabled;
	}

	@Override
	public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	}

	/**
	 * @param listener    OnSharedPreferenceChangeListener
	 * @param decryptKeys Callbacks receive the "key" parameter decrypted
	 */
	public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener, boolean decryptKeys) {
		if (!decryptKeys) {
			registerOnSharedPreferenceChangeListener(listener);
		}
	}

	@Override
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
	}
}
