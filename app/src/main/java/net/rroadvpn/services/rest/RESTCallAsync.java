package net.rroadvpn.services.rest;

import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;

import com.google.gson.Gson;

import net.rroadvpn.exception.RESTException;
import net.rroadvpn.exception.RESTNotFoundException;
import net.rroadvpn.model.rest.RESTError;
import net.rroadvpn.model.rest.RESTResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class RESTCallAsync extends AsyncTask<Object, Void, RESTResponse> {

    private OkHttpClient client;
    private Gson gson;

    public RESTCallAsync(OkHttpClient client, Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    @Override
    protected RESTResponse doInBackground(Object... params) {
        String method = (String) params[0];
        String url = (String) params[1];

        switch (method) {
            case "GET":
                try {
                    return this.doGet(url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            case "POST": {
                RequestBody requestBody = (RequestBody) params[2];
                try {
                    return this.doPost(url, requestBody);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            case "PUT": {
                RequestBody requestBody = (RequestBody) params[2];
                try {
                    return this.doPut(url, requestBody);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            case "DELETE": {
                RequestBody requestBody = (RequestBody) params[2];
                try {
                    return this.doDelete(url, requestBody);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    private RESTResponse doGet(String url) throws JSONException, RESTException, RESTNotFoundException {
        Request request = new Request.Builder()
                .url(url)
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

        int responseCode = response.code();

        String string;
        try {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RESTException("Stub");
            } else {
                string = body.string();
            }
        } catch (IOException e) {
            throw new RESTException("Stub");
        }

        JSONObject jsonObj = new JSONObject(string);

        String status = (String) jsonObj.get("status");

        RESTResponse restResponse = new RESTResponse(status, responseCode);

        if (response.isSuccessful()) {
            restResponse.setOk(true);

            Object data = jsonObj.get("data");
            restResponse.setData(data);

            if (jsonObj.has("limit")) {
                Integer limit = (Integer) jsonObj.get("limit");
                restResponse.setLimit(limit);
            }

            if (jsonObj.has("offset")) {
                Integer offset = (Integer) jsonObj.get("offset");
                restResponse.setLimit(offset);
            }
        } else {
            if (responseCode == 404) throw new RESTNotFoundException("Not Found!");
            if (responseCode == 405) throw new RESTException("Method Not Allowed!");

            restResponse.setOk(false);

            List<RESTError> errors = new ArrayList<>();
            JSONArray errorsJson = jsonObj.getJSONArray("errors");
            for (int i = 0; i < errorsJson.length(); i++) {
                JSONObject errorJson = errorsJson.getJSONObject(i);
                errors.add(new RESTError(errorJson));
            }
            restResponse.setErrors(errors);
        }

        return restResponse;
    }

    private RESTResponse doPost(String url, RequestBody requestBody) throws JSONException {
        Request request = new Request.Builder()
                .url(url)
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

        int responseCode = response.code();

        String string;
        try {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RESTException("Stub");
            } else {
                string = body.string();
            }
        } catch (IOException e) {
            throw new RESTException("Stub");
        }

        JSONObject jsonObj = new JSONObject(string);

        String status = (String) jsonObj.get("status");

        RESTResponse restResponse = new RESTResponse(status, responseCode);

        if (response.isSuccessful()) {
            restResponse.setOk(true);

            JSONObject data = jsonObj.getJSONObject("data");
            restResponse.setData(data);

            if (jsonObj.has("limit")) {
                Integer limit = (Integer) jsonObj.get("limit");
                restResponse.setLimit(limit);
            }

            if (jsonObj.has("offset")) {
                Integer offset = (Integer) jsonObj.get("offset");
                restResponse.setLimit(offset);
            }
        } else {
            if (responseCode == 404) throw new RESTNotFoundException("Not Found");

            restResponse.setOk(false);

            List<RESTError> errors = new ArrayList<>();
            JSONArray errorsJson = jsonObj.getJSONArray("errors");
            for (int i = 0; i < errorsJson.length(); i++) {
                JSONObject errorJson = errorsJson.getJSONObject(i);
                errors.add(new RESTError(errorJson));
            }
            restResponse.setErrors(errors);
        }

        return restResponse;
    }

    private RESTResponse doPut(String url, RequestBody requestBody) throws JSONException {
        Request request = new Request.Builder()
                .url(url)
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

        int responseCode = response.code();

        String string;
        try {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RESTException("Stub");
            } else {
                string = body.string();
            }
        } catch (IOException e) {
            throw new RESTException("Stub");
        }

        JSONObject jsonObj = new JSONObject(string);

        String status = (String) jsonObj.get("status");

        RESTResponse restResponse = new RESTResponse(status, responseCode);

        if (response.isSuccessful()) {
            restResponse.setOk(true);

            JSONObject data = jsonObj.getJSONObject("data");
            restResponse.setData(data);

            if (jsonObj.has("limit")) {
                Integer limit = (Integer) jsonObj.get("limit");
                restResponse.setLimit(limit);
            }

            if (jsonObj.has("offset")) {
                Integer offset = (Integer) jsonObj.get("offset");
                restResponse.setLimit(offset);
            }
        } else {
            if (responseCode == 404) throw new RESTNotFoundException("Not Found");

            restResponse.setOk(false);

            List<RESTError> errors = new ArrayList<>();
            JSONArray errorsJson = jsonObj.getJSONArray("errors");
            for (int i = 0; i < errorsJson.length(); i++) {
                JSONObject errorJson = errorsJson.getJSONObject(i);
                errors.add(new RESTError(errorJson));
            }
            restResponse.setErrors(errors);
        }

        return restResponse;
    }

    private RESTResponse doDelete(String url, RequestBody requestBody) throws JSONException {
        Request request = new Request.Builder()
                .url(url)
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

        int responseCode = response.code();

        String string;
        try {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RESTException("Stub");
            } else {
                string = body.string();
            }
        } catch (IOException e) {
            throw new RESTException("Stub");
        }

        JSONObject jsonObj = new JSONObject(string);

        String status = (String) jsonObj.get("status");

        RESTResponse restResponse = new RESTResponse(status, responseCode);

        if (response.isSuccessful()) {
            restResponse.setOk(true);

            JSONObject data = jsonObj.getJSONObject("data");
            restResponse.setData(data);

            if (jsonObj.has("limit")) {
                Integer limit = (Integer) jsonObj.get("limit");
                restResponse.setLimit(limit);
            }

            if (jsonObj.has("offset")) {
                Integer offset = (Integer) jsonObj.get("offset");
                restResponse.setLimit(offset);
            }
        } else {
            if (responseCode == 404) throw new RESTNotFoundException("Not Found");

            restResponse.setOk(false);

            List<RESTError> errors = new ArrayList<>();
            JSONArray errorsJson = jsonObj.getJSONArray("errors");
            for (int i = 0; i < errorsJson.length(); i++) {
                JSONObject errorJson = errorsJson.getJSONObject(i);
                errors.add(new RESTError(errorJson));
            }
            restResponse.setErrors(errors);
        }

        return restResponse;
    }
}
