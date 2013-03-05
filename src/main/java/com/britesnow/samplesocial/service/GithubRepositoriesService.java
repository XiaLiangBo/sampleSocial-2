package com.britesnow.samplesocial.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.util.EncodingUtils;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import com.britesnow.samplesocial.entity.User;
import com.britesnow.snow.util.JsonUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GithubRepositoriesService {
	
	private static String PREFIX = "https://api.github.com";
	
	@Inject
	private YaoGithubAuthService githubAuthService;
	@Inject
	private GithubUserService githubUserService;
	
	public List<Repository> getRepositories(User user) throws IOException {
		RepositoryService repositoryService = new RepositoryService(githubAuthService.createClient(user));
	    return repositoryService.getRepositories();
	}
	
	public Repository createRepository(User user,Repository repo) throws IOException {
		RepositoryService repositoryService = new RepositoryService(githubAuthService.createClient(user));
	    return repositoryService.createRepository(repo);
	}
	
	public Repository editRepository(User user,Repository repo) throws IOException {
		RepositoryService repositoryService = new RepositoryService(githubAuthService.createClient(user));
	    return repositoryService.editRepository(repo);
	}
	
	public Map getReadme(User user,String repo) throws IOException{
		OAuthRequest request = githubAuthService.createRequest(Verb.GET,
				PREFIX+"/repos/"+githubUserService.getGithubUser(user).getLogin()+"/"+repo+
				"/readme"+"?access_token="+githubAuthService.getToken(user).getToken());
		Response response = request.send();
		Map m = JsonUtil.toMapAndList(response.getBody());
		String content = (String) m.get("content");
		if(content!=null)
			m.put("content", new String(EncodingUtils.fromBase64(content)));
		return m;
	}
	
	//list files or get file content
	public Object getContents(User user,String repo,String path) throws IOException{
		if(path==null)
			path="";
		if(path.startsWith("/"))
			path = "/"+path;
		OAuthRequest request = githubAuthService.createRequest(Verb.GET,
				PREFIX+"/repos/"+githubUserService.getGithubUser(user).getLogin()+"/"+repo+
				"/contents"+path+"?access_token="+githubAuthService.getToken(user).getToken());
		Response response = request.send();
		String result = response.getBody();
		if(result.startsWith("["))
			return result;
		else{
			Map m = JsonUtil.toMapAndList(result);
			m.put("content", new String(EncodingUtils.fromBase64((String)m.get("content"))));
			return m;
		}
	}
}
