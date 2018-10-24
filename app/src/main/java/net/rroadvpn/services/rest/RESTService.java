package net.rroadvpn.services.rest;

import android.os.NetworkOnMainThreadException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.rroadvpn.exception.RESTException;
import net.rroadvpn.model.rest.RESTError;
import net.rroadvpn.model.rest.RESTResponse;
import net.rroadvpn.services.PreferencesService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RESTService implements RESTServiceI {

    protected PreferencesService preferencesService;
    private Map<String, String> headers = new HashMap<>();
    private String serviceURL;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;
    private Logger log;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm'Z'").create();

    public RESTService(PreferencesService preferencesService, String serviceURL) {
        this.preferencesService = preferencesService;
        this.serviceURL = serviceURL;
        this.log = LoggerFactory.getLogger("RESTService");


        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
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

        Headers headersTgt = Headers.of(this.headers);

        Request request = new Request.Builder()
                .url(url)
                .headers(headersTgt)
                .get()
                .build();

        Response response;
        try {
            response = this.client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        } catch (NetworkOnMainThreadException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        }

        String responseBodyString;
        try {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RESTException("Stub");
            } else {
                responseBodyString = body.string();
            }
        } catch (IOException e) {
            throw new RESTException("Stub");
        }

        try {
            return this.parseResponse(response.code(), response.isSuccessful(), responseBodyString, response.headers());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        }
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
        Headers headersTgt = Headers.of(this.headers);

        Request request = new Request.Builder()
                .url(url)
                .headers(headersTgt)
                .put(requestBody)
                .build();

        Response response;
        try {
            response = this.client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        } catch (NetworkOnMainThreadException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        }

        String responseBodyString;
        try {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RESTException("Stub");
            } else {
                responseBodyString = body.string();
            }
        } catch (IOException e) {
            throw new RESTException("Stub");
        }

        try {
            return this.parseResponse(response.code(), response.isSuccessful(), responseBodyString, response.headers());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        }
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
        Headers headersTgt = Headers.of(this.headers);

        Request request = new Request.Builder()
                .url(url)
                .headers(headersTgt)
                .post(requestBody)
                .build();

        Response response;
        try {
            response = this.client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        } catch (NetworkOnMainThreadException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        }

        String responseBodyString;
        try {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RESTException("Stub");
            } else {
                responseBodyString = body.string();
            }
        } catch (IOException e) {
            throw new RESTException("Stub");
        }

        try {
            return this.parseResponse(response.code(), response.isSuccessful(), responseBodyString,
                    response.headers());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        }
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
        Headers headersTgt = Headers.of(this.headers);

        Request request = new Request.Builder()
                .url(url)
                .headers(headersTgt)
                .delete(requestBody)
                .build();

        Response response;
        try {
            response = this.client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        } catch (NetworkOnMainThreadException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        }

        String responseBodyString;
        try {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RESTException("Stub");
            } else {
                responseBodyString = body.string();
            }
        } catch (IOException e) {
            throw new RESTException("Stub");
        }

        try {
            return this.parseResponse(response.code(), response.isSuccessful(), responseBodyString, response.headers());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RESTException("Stub");
        }
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

    private RESTResponse parseResponse(int responseCode, boolean isOk, String responseBodyString,
                                       Headers headers) throws JSONException {
        System.out.println("!\nthis is response:\n" + responseBodyString);
        log.debug("parseResponse started. This is responce before parsing:\n" + responseBodyString);
        JSONObject jsonObj = new JSONObject(responseBodyString);

        String status = (String) jsonObj.get("status");

        RESTResponse restResponse = new RESTResponse(status, responseCode);

        restResponse.setOk(isOk);

        if (isOk) {
            log.info("Response success");
            if (jsonObj.has("data")) {
                JSONObject data = jsonObj.getJSONObject("data");
                restResponse.setData(data);
            }

            if (jsonObj.has("limit")) {
                Integer limit = (Integer) jsonObj.get("limit");
                restResponse.setLimit(limit);
            }

            if (jsonObj.has("offset")) {
                Integer offset = (Integer) jsonObj.get("offset");
                restResponse.setLimit(offset);
            }
        } else if (jsonObj.has("errors")) {
            log.error("reponseBodyString has API error. HALT API ERROR:\n" + responseBodyString + "HALT API ERROR");
            List<RESTError> errors = new ArrayList<>();
            JSONArray errorsJson = jsonObj.getJSONArray("errors");
            for (int i = 0; i < errorsJson.length(); i++) {
                JSONObject errorJson = errorsJson.getJSONObject(i);
                errors.add(new RESTError(errorJson));
            }
            restResponse.setErrors(errors);
        }

        Map<String, List<String>> headersMap = headers.toMultimap();

        restResponse.setHeaders(headersMap);

        return restResponse;
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


