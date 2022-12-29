package org.zfin.security;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;

public class MigratingPasswordEncoder implements PasswordEncoder {
    public static final int BCRYPT_ROUNDS = 15;
    public static final String SALT = "dedicated to George Streisinger";

    @Override
    public String encode(CharSequence plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword.toString(), BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    @Override
    public boolean matches(CharSequence plainTextPassword, String passwordInDatabase) {
        try {
            //is the password in the database md5?
            boolean isMd5 = !passwordInDatabase.contains("$2a$");
            if (isMd5 && !FeatureFlags.isFlagEnabled(FeatureFlagEnum.REQUIRE_MODERN_PASSWORD_HASH)) {
                return (new Md5PasswordEncoder()).isPasswordValid(passwordInDatabase, plainTextPassword.toString(), SALT);
            } else if (isMd5) {
                return false;
            } else {
                return BCrypt.checkpw(plainTextPassword.toString(), passwordInDatabase);
            }
        } catch (Exception e) {
            return false;
        }
    }
}