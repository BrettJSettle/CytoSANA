package org.cytoscape.sana.sana_app.internal;
import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class SanaService {

    private static final String baseURL = "http://localhost:80"; //http://sana.cytoscape.io";

    public static String doAlignment(String cx, Map<String, String> args) throws IOException {
        HttpClient client = HttpClients.createDefault();
        int i = 0;
        String url = baseURL;
        for (String s : args.keySet()){
        	if (i == 0)
        		url += "?";
        	url += String.format("%s=%s", s, args.get(s));
        	if (i < args.size()-1)
        		url += "&";
        	i++;
        }
        System.out.println(url);
        HttpPost post = new HttpPost(url);
        StringEntity cxEntity = new StringEntity(cx);
        post.setEntity(cxEntity);
        post.setHeader("Content-type", "application/json");
        HttpResponse  response = client.execute(post);
        HttpEntity entity = response.getEntity();
        String output = entity != null ? EntityUtils.toString(entity) : null;
        System.out.println("SERVICE OUT: " + output);
        return output;
    }
}