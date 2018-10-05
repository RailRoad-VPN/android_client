package net.rroadvpn.services.rest;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.rroadvpn.exception.RESTException;
import net.rroadvpn.model.rest.RESTResponse;
import net.rroadvpn.services.PreferencesService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class RESTService implements RESTServiceI {

    protected PreferencesService preferencesService;
    private Map<String, String> headers = new HashMap<>();
    private String serviceURL;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm'Z'").create();

    public RESTService(PreferencesService preferencesService, String serviceURL) {
        this.preferencesService = preferencesService;
        this.serviceURL = serviceURL;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        this.client = builder.build();
    }

    @Override
    public RESTResponse get(String url, Map<String, String> headers) {
        if (url == null) {
            url = this.serviceURL;
        }

        if (headers != null) {
            this.headers.putAll(headers);
        }

        RESTResponse apiResponse = null;
        try {
            apiResponse = new RESTCallAsync(this.client).execute("GET", url, this.headers).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RESTException("execution exception", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RESTException("interrupted exception", e);
        }
        return apiResponse;
    }

    @Override
    public RESTResponse put(String url, Map<String, Object> data, Map<String, String> headers) {
        if (url == null) {
            url = this.serviceURL;
        }

        if (headers != null) {
            this.headers.putAll(headers);
        }

        RequestBody requestBody = prepareRequestBody(data);
        RESTResponse apiResponse;
        try {
            RESTCallAsync restCallAsync = new RESTCallAsync(this.client);
            AsyncTask<Object, Void, RESTResponse> put = restCallAsync.execute("PUT", url, this.headers, requestBody);
            apiResponse = put.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RESTException("execution exception", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RESTException("interrupted exception", e);
        }
        return apiResponse;
    }

    @Override
    public RESTResponse post(String url, Map<String, Object> data, Map<String, String> headers) {
        if (url == null) {
            url = this.serviceURL;
        }

        if (headers != null) {
            this.headers.putAll(headers);
        }

        RequestBody requestBody = prepareRequestBody(data);
        RESTResponse apiResponse;
        try {
            apiResponse = new RESTCallAsync(this.client).execute("POST", url, this.headers, requestBody).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RESTException("execution exception", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RESTException("interrupted exception", e);
        }
        return apiResponse;
    }

    @Override
    public RESTResponse delete(String url, Map<String, Object> data, Map<String, String> headers) {
        if (url == null) {
            url = this.serviceURL;
        }
        if (headers != null) {
            this.headers.putAll(headers);
        }
        RequestBody requestBody = prepareRequestBody(data);
        RESTResponse apiResponse;
        try {
            apiResponse = new RESTCallAsync(this.client).execute("DELETE", url, this.headers, requestBody).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RESTException("execution exception", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RESTException("interrupted exception", e);
        }
        return apiResponse;
    }

    private RequestBody prepareRequestBody(Map<String, Object> data) {
        String dataJson = this.gson.toJson(data);

        return RequestBody.create(JSON, dataJson);
    }

    private FormBody prepareFormBody(Map<String, Object> data) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();

        for (Map.Entry pair : data.entrySet()) {
            String key = (String) pair.getKey();
            Object valueObj = pair.getValue();
            String value;
            if (valueObj instanceof Date) {
                TimeZone tz = TimeZone.getTimeZone("UTC");
                // Quoted "Z" to indicate UTC, no timezone offset
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.ENGLISH);
                df.setTimeZone(tz);
                value = df.format(new Date());
            } else {
                value = String.valueOf(valueObj);
            }
            formBodyBuilder.add(key, value);
        }

        return formBodyBuilder.build();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getServiceURL() {
        return serviceURL;
    }
}


