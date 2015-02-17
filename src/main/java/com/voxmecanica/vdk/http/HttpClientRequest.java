
package com.voxmecanica.vdk.http;

import com.voxmecanica.vdk.VoxException;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class that represents an http request.
 * @author Vladimir Vivien
 *
 */
public class HttpClientRequest {
    private String requestPath;
    private String requestMethod = "GET";
    private List<NameValuePair> params;
    private List<Header> headers;
    private URI pathUri;
    
    public HttpClientRequest(String method, String path) {
    	// check requestPath
    	if(isRequestPathValid(method,path)){
        	try {
        		//TODO - create() is called in isRequestPathValid() already, consider using once.
    			pathUri = URI.create(path);
        	} catch (IllegalArgumentException e) {
    			throw new VoxException(e);
    		}    		
    	}else{
    		throw new VoxException("Specified path [" + path + "] or HTTP method [" + method + "] is invalid.");
    	}

    	requestPath = path;
    	requestMethod = method;
    	params = new ArrayList<NameValuePair>();
    	headers = new ArrayList<Header>();
    }
    
    public HttpClientRequest(String path){
    	this("GET", path);
    }
    
    public HttpClientRequest(String method, URI uri){
    	this(method, uri.toASCIIString());
    }
    
    public HttpClientRequest(URI uri){
    	this("GET", uri.toASCIIString());
    }
    
    public String getRequestPath(){
    	return requestPath;
    }
    
    public String getRequestMethod(){
    	return requestMethod;
    }
    
    public HttpClientRequest addHeader(String name, String value){
    	this.headers.add(new BasicHeader(name, value));
    	return this;
    }
    
    public HttpClientRequest addHeaders(Map<String,String> hdrs){
    	if(hdrs != null && hdrs.size() > 0){
    		for(Map.Entry<String, String> h : hdrs.entrySet()){
    			this.headers.add(new BasicHeader(h.getKey(), h.getValue()));
    		}
    	}
    	return this;
    }

    public HttpClientRequest addParameter(String name, String value){
    	params.add(new BasicNameValuePair(name, value));
    	return this;
    }

    public HttpClientRequest addParameters(Map<String, String> params){
    	if(params != null && params.size() > 0){
    		for(Map.Entry<String, String> p : params.entrySet()){
    			this.params.add(new BasicNameValuePair(p.getKey(), p.getValue()));
    		}
    	}
    	return this;
    }
    
    private boolean isRequestPathValid(String method, String path) {
    	if(path == null) return false;
    	
    	boolean requestPathOK = path.trim().length() > 0;
		
		try {
			//TODO - consider calling create once.
			URI.create(path).toURL();
			requestPathOK = requestPathOK && true;
		} catch (Exception ex) {
			requestPathOK = requestPathOK && false;
		}
    	
    	return (
    		requestPathOK && 
    		method != null &&
    		(
    			method.toUpperCase().equals("GET") || 
    			method.toUpperCase().equals("POST")
    		)
    	); 
    }
    
    public URI getRequestUri() {
    	return pathUri;
    }
    
    public HttpUriRequest toHttpUriRequest() {

    	if(requestMethod.toUpperCase().equals("GET")){
    		return toHttpGet();
    	}else if (requestMethod.toUpperCase().equals("POST")){
    		return toHttpPost();
    	}
    	return toHttpGet();
    }
    
    private HttpGet toHttpGet(){
    	URI uri = null;
    	try{
	    	if(params.size() > 0){
	    		String encodedParams = URLEncodedUtils.format(params, HttpService.Config.DEFAULT_CHARCSET);
	    		if(requestPath.indexOf("?") > -1){
	    			uri = new URI(requestPath + "&" + encodedParams);
	    		}else{
	    			uri = new URI(requestPath + "?" + encodedParams);
	    		}
	    	}else{
	    		uri = new URI (requestPath);
	    	}
    	}catch(URISyntaxException ex){
    		throw new VoxException(ex);
    	}
    	HttpGet get = new HttpGet(uri);
    	get.setHeaders(headers.toArray(new Header[0]));
    	return get;
    }
    
    private HttpPost toHttpPost() {
    	HttpPost post = null;
    	try{
    		post = new HttpPost(new URI(requestPath));
    		post.setHeaders(headers.toArray(new Header[0]));
	    	if(params.size() > 0){
	    		UrlEncodedFormEntity formParams = new UrlEncodedFormEntity(params, HttpService.Config.DEFAULT_CHARCSET);
	    		post.setEntity(formParams);
	    	}
    	}catch(UnsupportedEncodingException ex){
    		new VoxException(ex);
    	}catch(URISyntaxException ex){
    		new VoxException(ex);    	
    	}
    	
    	return post;
    }
    
}