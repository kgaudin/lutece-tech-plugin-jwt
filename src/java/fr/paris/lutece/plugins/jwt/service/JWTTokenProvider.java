/*
 * Copyright (c) 2002-2019, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.jwt.service;

import java.security.Key;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fr.paris.lutece.plugins.jwt.util.constants.JWTConstants;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.time.DateUtils;

import fr.paris.lutece.portal.business.rbac.AdminRole;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.util.AppPathService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTTokenProvider {
	
	public String createJWT( HttpServletRequest request, String subject, int tokenValidityMin, Map<String, AdminRole> roles, AdminUser user) {
		  
	    // The JWT header identitfy algorithm use for the signature
	    Map<String, Object> header = new HashMap<String, Object>( );
	    header.put("alg", "HS256");
	    header.put( Header.TYPE, Header.JWT_TYPE );
	    Date date = new Date();
	    	
	    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
	    

	    //We will sign our JWT with our ApiKey secret
	    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary( JWTConstants.SECRET_KEY );
	    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	    //Let's set the JWT Claims
	    JwtBuilder builder = Jwts.builder( )
	            .setHeader( header )
	            .setIssuer( AppPathService.getWebAppPath( ) )
	            .setIssuedAt( new Timestamp( date.getTime() ) )
	            .setAudience( AppPathService.getBaseUrl( request ) )
	            .setSubject( String.valueOf( user.getUserId( ) ) )
	            .claim( "role", roles ) 	
	            .signWith( signatureAlgorithm, signingKey );
	  
	    //if it has been specified, let's add the expiration
	    if (tokenValidityMin > 0) {
	        date = DateUtils.addMinutes(date, tokenValidityMin);
	        builder.setExpiration(date);
	    }  
	  
	    //Builds the JWT and serializes it to a compact, URL-safe string
	    return builder.compact();
	}
	
	public static Claims decodeJWT(String jwt) {
	    //This line will throw an exception if it is not a signed JWS (as expected)
	    return  Jwts.parser()
	            .setSigningKey(DatatypeConverter.parseBase64Binary( JWTConstants.SECRET_KEY ))
	            .parseClaimsJws(jwt).getBody();
	}

}
