package org.sugar.media.sipserver.utils;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import gov.nist.javax.sip.header.ims.AuthorizationHeaderIms;

import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.message.Request;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Date:2024/12/09 16:23:37
 * Author：Tobin
 * Description:
 */
public class helper extends DigestServerAuthenticationHelper {

    private MessageDigest messageDigest;

    /**
     * Default constructor.
     *
     * @throws NoSuchAlgorithmException
     */
    public helper() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
    }


    public boolean doAuthenticatePassword(Request request, String pass) {
        // 重写方法以使用不同的 Authorization Header
        AuthorizationHeaderIms authHeader = (AuthorizationHeaderIms) request.getHeader(AuthorizationHeader.NAME);
        if (authHeader == null) {
            return false;
        }

        String realm = authHeader.getRealm();
        String username = authHeader.getUsername();
        if (username == null || realm == null) {
            return false;
        }

        String nonce = authHeader.getNonce();
        URI uri = authHeader.getURI();
        if (uri == null) {
            return false;
        }

        String A1 = username + ":" + realm + ":" + pass;
        String A2 = request.getMethod().toUpperCase() + ":" + uri.toString();
        byte[] mdbytes = messageDigest.digest(A1.getBytes());
        String HA1 = toHexString(mdbytes);

        mdbytes = messageDigest.digest(A2.getBytes());
        String HA2 = toHexString(mdbytes);

        String cnonce = authHeader.getCNonce();
        String KD = HA1 + ":" + nonce;
        if (cnonce != null) {
            KD += ":" + cnonce;
        }
        KD += ":" + HA2;
        mdbytes = messageDigest.digest(KD.getBytes());
        String mdString = toHexString(mdbytes);
        String response = authHeader.getResponse();

        return mdString.equals(response);
    }

}
