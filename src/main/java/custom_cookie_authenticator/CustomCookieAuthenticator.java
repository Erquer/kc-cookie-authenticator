package custom_cookie_authenticator;

import org.json.JSONObject;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.util.CookieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CustomCookieAuthenticator implements Authenticator {
    private static final Logger logger = LoggerFactory.getLogger(CustomCookieAuthenticator.class);


    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        logger.info("Authenticating, sending request");
        String response = "";
        try {
                // Set up the URL for the API call
                String apiUrl = "https://swapi.dev/api/people/1/";
                URL url = new URL(apiUrl);

                // Set up the connection to the API endpoint
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                // Read the response from the API endpoint
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String output;
                StringBuilder responseBuilder = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    responseBuilder.append(output);
                }
                conn.disconnect();

                // Parse the response as a JSON object
                JSONObject jsonObject = new JSONObject(responseBuilder.toString());
                response = jsonObject.getString("name");
        } catch (Exception e) {
            logger.error("Error with api request: " + e.getMessage());
        }

        logger.info("Adding cookie");
        CookieHelper.addCookie(
                "CUSTOM_COOKIE",
                response,
                "/",
                null,
                null,
                3500,
                false,
                true,
                authenticationFlowContext.getSession()
        );

        logger.info("Adding header");
        authenticationFlowContext
                .getSession()
                .getContext()
                .getHttpResponse()
                .addHeader("X_CUSTOM_HEADER", "CUSTOM HEADER VALUE");

        authenticationFlowContext.success();
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        authenticationFlowContext.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }
}
