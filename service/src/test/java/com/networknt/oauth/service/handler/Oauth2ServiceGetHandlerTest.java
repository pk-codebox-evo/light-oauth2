package com.networknt.oauth.service.handler;

import com.networknt.client.Client;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
* Generated by swagger-codegen
*/
public class Oauth2ServiceGetHandlerTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(Oauth2ServiceGetHandlerTest.class);

    @Test
    public void testPageMissing() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpGet httpGet = new HttpGet("http://localhost:6883/oauth2/service");
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(400, statusCode);
            if(statusCode == 400) {
                Status status = Config.getInstance().getMapper().readValue(body, Status.class);
                Assert.assertNotNull(status);
                Assert.assertEquals("ERR11000", status.getCode());
                Assert.assertEquals("VALIDATOR_REQUEST_PARAMETER_QUERY_MISSING", status.getMessage()); // page is missing
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOauth2ServiceGetHandler() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpGet httpGet = new HttpGet("http://localhost:6883/oauth2/service?page=1");
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(200, statusCode);
            if(statusCode == 200) {
                // make sure that there are two services in the result.
                List<Service> services = Config.getInstance().getMapper().readValue(body, List.class);
                Assert.assertTrue(services.size()>= 2 && services.size() <= 3);
                // make sure that the first is AACT0001
                Service service = services.get(0);
                Assert.assertEquals("AACT0001", service.getServiceId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEmptyPage2() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpGet httpGet = new HttpGet("http://localhost:6883/oauth2/service?page=2");
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(200, statusCode);
            if(statusCode == 200) {
                // make sure that there are two services in the result.
                List<Service> services = Config.getInstance().getMapper().readValue(body, List.class);
                Assert.assertEquals(0, services.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
