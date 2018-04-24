package net.soti.go.plugin.task.fetch.artifacts;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.soti.go.plugin.task.fetch.artifacts.models.ArtifactFile;
import net.soti.go.plugin.task.fetch.artifacts.models.JobRevision;
import net.soti.go.plugin.task.fetch.artifacts.models.Pipeline;
import net.soti.go.plugin.task.fetch.artifacts.models.PipelineRevision;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

/**
 * User: wsim
 * Date: 2018-04-24
 */
public class GoCdClient {
    private final CredentialsProvider provider = new BasicCredentialsProvider();
    private final SSLContext sslContext;
    private final String authHeader;
    private final String gocdApiHost;

    GoCdClient(Map<String, Map> requestBodyMap) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        this(requestBodyMap, Utils.getGoCdUser(requestBodyMap), Utils.getGoCdPassword(requestBodyMap));
    }

    public GoCdClient(Map<String, Map> requestBodyMap, String user, String password) throws KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        sslContext = new SSLContextBuilder().loadTrustMaterial(null, (x509CertChain, authType) -> true).build();

        String auth = String.format("%s:%s", user, password);
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("ISO-8859-1")));
        authHeader = "Basic " + new String(encodedAuth);
        gocdApiHost = Utils.getGoCdApiHost(requestBodyMap);
    }

    public void getFile(final ArtifactFile artifactFile, final String rootPath, final String jobname) throws IOException {
        try (CloseableHttpClient client = buildHttpClient()) {
            int retryCounter = 10;
            final String middlePath = jobname == null ? "" : String.format("/%s", jobname);
            final String fullPath = String.format("%s%s/%s", rootPath, middlePath, artifactFile.getPath());
            final Path path = Paths.get(fullPath);
            if (Files.exists(path))
            {
                throw new IOException(String.format("File \"%s\" exists already.", fullPath));
            }

            Files.createDirectories(path.getParent());
            File download = null;

            do {
                retryCounter--;
                HttpGet httpGet = new HttpGet(artifactFile.getUrl());
                httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
                try {
                    download = client.execute(httpGet, new FileDownloadResponseHandler(new File(fullPath)));

                } catch (IOException e) {
                    System.out.println(String.format("Failed to download file '%s', %s", artifactFile.getUrl(), e.toString()));
                }
            } while (retryCounter > 0 && download != null);

            if (download == null) {
                throw new IOException("Failed to download file");
            }
        }
    }

    public List<ArtifactFile> getAllArtifacts(final JobRevision revision) throws IOException {
        return getAllArtifacts(
                revision.getPipelineName(),
                revision.getPipelineCounter(),
                revision.getStageName(),
                revision.getStageCounter(),
                revision.getJobName());
    }

    public List<ArtifactFile> getAllArtifacts(
            final String pipelineName,
            final int pipelineCounter,
            final String stageName,
            final int stageCounter,
            final String jobName) throws IOException {
        try (CloseableHttpClient client = buildHttpClient()) {
            String url = String.format("%s/go/files/%s/%d/%s/%d/%s.json",
                    gocdApiHost,
                    pipelineName,
                    pipelineCounter,
                    stageName,
                    stageCounter,
                    jobName);

            int retryCounter = 10;
            HttpResult result = null;

            do {
                retryCounter--;
                HttpGet httpGet = new HttpGet(url);
                httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
                try (CloseableHttpResponse response = client.execute(httpGet)) {
                    result = HttpResult.fromResponse(response);
                } catch (IOException e) {
                    System.out.println(
                            String.format("Failed to get Artifacts list of /%s/%d/%s/%d/%s",
                                    pipelineName,
                                    pipelineCounter,
                                    stageName,
                                    stageCounter,
                                    jobName)
                    );
                }
            } while (retryCounter > 0 && !HttpResult.isSuccessResult(result));

            if (!HttpResult.isSuccessResult(result)) {
                return null;
            }

            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            Type artType = new TypeToken<List<ArtifactFile>>() {
            }.getType();
            List<ArtifactFile> arts = gson.fromJson(result.getData(), artType);
            List<ArtifactFile> files = new ArrayList<>();
            for (ArtifactFile art : arts) {
                if (art.isFile()) {
                    files.add(art);
                } else {
                    files.addAll(art.getChildrenRecursively());
                }
            }

            return files;
        }
    }

    public Pipeline getPipelineInstance(final PipelineRevision revision) throws IOException {
        return getPipelineInstance(revision.getName(), revision.getCounter());
    }

    public Pipeline getPipelineInstance(final String pipeline, final int counter) throws IOException {
        try (CloseableHttpClient client = buildHttpClient()) {
            String url = String.format("%s/go/api/pipelines/%s/instance/%d", gocdApiHost, pipeline, counter);
            int retryCounter = 10;
            HttpResult result = null;

            do {
                retryCounter--;
                HttpGet httpGet = new HttpGet(url);
                httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
                try (CloseableHttpResponse response = client.execute(httpGet)) {
                    result = HttpResult.fromResponse(response);
                } catch (IOException e) {
                    System.out.println(String.format("Failed to get pipeline instance for '%s/%d', %s", pipeline, counter, e.toString()));
                }
            } while (retryCounter > 0 && !HttpResult.isSuccessResult(result));

            if (!HttpResult.isSuccessResult(result)) {
                System.out.println("Failed to get pipeline instance 10 times.");
                return null;
            }

            return Pipeline.fromJson(result.getData());
        }
    }

    private CloseableHttpClient buildHttpClient() {
        return HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setSSLContext(sslContext)
                .setConnectionManager(
                        new PoolingHttpClientConnectionManager(
                                RegistryBuilder.<ConnectionSocketFactory>create()
                                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                                        .register("https", new SSLConnectionSocketFactory(sslContext,
                                                NoopHostnameVerifier.INSTANCE))
                                        .build()
                        ))
                .build();
    }

    static class FileDownloadResponseHandler implements ResponseHandler<File> {
        private final File target;

        public FileDownloadResponseHandler(File target) {
            this.target = target;
        }

        @Override
        public File handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, this.target);
            return this.target;
        }

    }
}
