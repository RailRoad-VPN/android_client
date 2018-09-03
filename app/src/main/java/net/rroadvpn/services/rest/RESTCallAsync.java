package net.rroadvpn.services.rest;

import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;

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

    public RESTCallAsync(OkHttpClient client) {
        this.client = client;
    }

    @Override
    protected RESTResponse doInBackground(Object... params) {
        String method = (String) params[0];
        String url = (String) params[1];
        RequestBody requestBody;
        switch (method) {
            case "GET":
                try {
                    return this.doGet(url);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                } catch (RESTNotFoundException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                } catch (RESTException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                }
            case "POST":
                requestBody = (RequestBody) params[2];
                try {
                    return this.doPost(url, requestBody);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                } catch (RESTNotFoundException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                } catch (RESTException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                }
            case "PUT":
                requestBody = (RequestBody) params[2];
                try {
                    return this.doPut(url, requestBody);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                } catch (RESTNotFoundException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                } catch (RESTException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                }
            case "DELETE":
                requestBody = (RequestBody) params[2];
                try {
                    return this.doDelete(url, requestBody);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                } catch (RESTNotFoundException e) {
                    e.printStackTrace();
                    throw new RESTException("");
                } catch (RESTException e) {
                    e.printStackTrace();
                    throw new RESTException("");
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

        return this.parseResponse(response.code(), response.isSuccessful(), responseBodyString);
    }

    private RESTResponse doPost(String url, RequestBody requestBody) throws JSONException, RESTException, RESTNotFoundException {
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

        return this.parseResponse(response.code(), response.isSuccessful(), responseBodyString);
    }

    private RESTResponse doPut(String url, RequestBody requestBody) throws JSONException, RESTException, RESTNotFoundException {
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

        return this.parseResponse(response.code(), response.isSuccessful(), responseBodyString);
    }

    private RESTResponse doDelete(String url, RequestBody requestBody) throws JSONException, RESTException, RESTNotFoundException {
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

        return this.parseResponse(response.code(), response.isSuccessful(), responseBodyString);
    }

    private RESTResponse parseResponse(int responseCode, boolean isOk, String responseBodyString) throws JSONException, RESTException, RESTNotFoundException {
        JSONObject jsonObj = new JSONObject(responseBodyString);

        String status = (String) jsonObj.get("status");

        RESTResponse restResponse = new RESTResponse(status, responseCode);

        restResponse.setOk(isOk);

        if (isOk) {
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
