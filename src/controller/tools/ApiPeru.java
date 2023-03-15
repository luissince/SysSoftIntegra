package controller.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ApiPeru {

    private String URLAPI_SUNAT_ESTANDAR = "https://api.sunat.cloud/ruc/";

    private String URLAPI_RENIEC_ESTANDAR = "https://api.reniec.cloud/dni/";

    private String URLAPI_SUNAT_APISPERU = "https://dniruc.apisperu.com/api/v1/ruc/";

    private String URLAPI_RENIEC_APISPERU = "https://dniruc.apisperu.com/api/v1/dni/";

    private String jsonURL;

    public ApiPeru() {
    }

    public String getUrlSunatEstandar(String numDocument) {
        URLAPI_SUNAT_ESTANDAR = URLAPI_SUNAT_ESTANDAR + numDocument;
        return GetRequest(URLAPI_SUNAT_ESTANDAR);
    }

    public String getUrlReniecEstandar(String numDocument) {
        URLAPI_RENIEC_ESTANDAR = URLAPI_RENIEC_ESTANDAR + numDocument;
        return GetRequest(URLAPI_RENIEC_ESTANDAR);
    }

    public String getUrlSunatApisPeru(String numDocument) {
        URLAPI_SUNAT_APISPERU = URLAPI_SUNAT_APISPERU + numDocument + "?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImFsZXhhbmRlcl9keF8xMEBob3RtYWlsLmNvbSJ9.6TLycBwcRyW1d-f_hhCoWK1yOWG_HJvXo8b-EoS5MhE";
        return GetRequest(URLAPI_SUNAT_APISPERU);
    }

    public String getUrlReniecApisPeru(String numDocument) {
        URLAPI_RENIEC_APISPERU = URLAPI_RENIEC_APISPERU + numDocument + "?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImFsZXhhbmRlcl9keF8xMEBob3RtYWlsLmNvbSJ9.6TLycBwcRyW1d-f_hhCoWK1yOWG_HJvXo8b-EoS5MhE";
        return GetRequest(URLAPI_RENIEC_APISPERU);
    }

    private String GetRequest(String url) {
        if (!isValid(url)) {
            return "Error en el formato de la URL";
        }

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder buffer = new StringBuilder();
                    int read;
                    char[] chars = new char[1024];
                    while ((read = reader.read(chars)) != -1) {
                        buffer.append(chars, 0, read);
                    }
                    jsonURL = buffer.toString();
                    return "200";
                }
            }

            return "Error en buscar los datos:" + responseCode;
        } catch (IOException ex) {
            return "Error:" + ex.getLocalizedMessage();
        }

    }

    private boolean isValid(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    public String getJsonURL() {
        return jsonURL;
    }

}
