package com.britesnow.samplesocial.service;

import java.util.Map;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

import com.britesnow.samplesocial.dao.SocialIdEntityDao;
import com.britesnow.samplesocial.entity.SocialIdEntity;
import com.britesnow.snow.web.binding.ApplicationProperties;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DropboxAuthService implements AuthService{

	@Inject
	private SocialIdEntityDao socialIdEntityDao;
	
	@Inject
	@ApplicationProperties
	private Map configMap;
	
	private static String DROPBOX = "dropbox";
	private static String REQUEST_TOKEN = "https://api.dropbox.com/1/oauth/request_token";
	private static String AUTHORIZE = "https://www.dropbox.com/1/oauth/authorize?";
    public DropboxAuthService() {
    }
    
	@Override
	public SocialIdEntity getSocialIdEntity(Long userId) {
		return null;
	}
	
    public String getAuthorizationUrl() {
    	OAuthRequest request = new OAuthRequest(Verb.POST,REQUEST_TOKEN);
    	StringBuffer requestHeader = new StringBuffer("OAuth ");
    	requestHeader.append("oauth_version=\"1.0\",")
    				 .append("oauth_signature_method=\"PLAINTEXT\",")
    	             .append("oauth_consumer_key=\"")
    	             .append(configMap.get(DROPBOX+".app_key"))
	             	 .append("\", oauth_signature=\"")
	             	 .append(configMap.get(DROPBOX+".app_secret"))
	             	 .append("&\"");
    	
    	request.addHeader("Authorization", 
    			requestHeader.toString());
    	String auth_token = request.send().getBody();
    	System.out.println(auth_token);
    	System.out.println(auth_token.substring(auth_token.indexOf("oauth_token=")));
        return AUTHORIZE+auth_token.substring(auth_token.indexOf("oauth_token="))+"&oauth_callback="+configMap.get(DROPBOX+".oauth_callback");
    }
}