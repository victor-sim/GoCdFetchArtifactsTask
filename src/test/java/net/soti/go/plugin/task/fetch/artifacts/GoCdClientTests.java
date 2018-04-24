package net.soti.go.plugin.task.fetch.artifacts;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import net.soti.go.plugin.task.fetch.artifacts.models.Pipeline;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * User: wsim
 * Date: 2018-04-24
 */
public class GoCdClientTests {

//    @Test
//    public void httpGetTest() throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
//        CredentialsProvider provider = new BasicCredentialsProvider();
//        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("user", "Welcome1234");
//        provider.setCredentials(AuthScope.ANY, credentials);
//
//        final SSLContext sslContext = new SSLContextBuilder()
//                .loadTrustMaterial(null, (x509CertChain, authType) -> true)
//                .build();
//
//        HttpClient client = HttpClientBuilder.create()
//                .setDefaultCredentialsProvider(provider)
//                .setSSLContext(sslContext)
//                .setConnectionManager(
//                        new PoolingHttpClientConnectionManager(
//                                RegistryBuilder.<ConnectionSocketFactory>create()
//                                        .register("http", PlainConnectionSocketFactory.INSTANCE)
//                                        .register("https", new SSLConnectionSocketFactory(sslContext,
//                                                NoopHostnameVerifier.INSTANCE))
//                                        .build()
//                        ))
//                .build();
//
//        HttpGet get = new HttpGet("https://cagomc100.corp.soti" +
//                ".net:8154/go/files/Coverage_v1414/11/Gather/4/BDD-W2012-SQL2012-runInstance-1.json");
//        String auth = "user:Welcome1234";
//        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("ISO-8859-1")));
//        String authHeader = "Basic " + new String(encodedAuth);
//        get.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
//        HttpResponse response = client.execute(get);
//        HttpResult result = HttpResult.fromResponse(response);
//        int statusCode = response.getStatusLine().getStatusCode();
//        String data = result.getData();
//
//
//        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
//        assertThat(data, notNullValue());
//
//        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
//
//        Type artType = new TypeToken<List<ArtifactFile>>() {        }.getType();
//        List<ArtifactFile> arts = gson.fromJson(data, artType);
//        assertThat(arts, notNullValue());
//
//        List<ArtifactFile> files = new ArrayList<>();
//        for (ArtifactFile art : arts) {
//            if (art.isFile()) {
//                files.add(art);
//            } else {
//                files.addAll(art.getChildrenRecursively());
//            }
//        }
//
//        assertThat(files, notNullValue());
//
//        Type fileList = new TypeToken<List<HashMap<String, Object>>>() {
//        }.getType();
//        List<HashMap<String, Object>> hashes = gson.fromJson(data, fileList);
//
//        assertThat(hashes, notNullValue());
//
//        for (ArtifactFile art : files) {
//            String url = art.getUrl();
//            String path = art.getPath();
//
//            assertThat(url, notNullValue());
//            assertThat(path, notNullValue());
//        }
//
//
//        Pipeline test = gson.fromJson(data, Pipeline.class);
//        assertThat(test, notNullValue());
//
//        response = client.execute(new HttpGet("https://cagomc100:8154/go/api/pipelines/Cpp_Projects_v1414/instance/70"));
//        result = HttpResult.fromResponse(response);
//        statusCode = response.getStatusLine().getStatusCode();
//        data = result.getData();
//
//        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
//        assertThat(data, notNullValue());
//    }
}
