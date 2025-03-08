package com.meusistema.keycloak.authenticator;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.credential.CredentialModel;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class PinResetPasswordResource {
    private final KeycloakSession session;
    private static final Logger logger = Logger.getLogger(PinResetPasswordResource.class);

    public PinResetPasswordResource(KeycloakSession session) {
        this.session = session;

        logger.info("PinResetPasswordResource constructor called");
    }

    @GET
    @Path("ping")
    public Response ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("response", "pong");

        return Response.ok(result).build();
    }

    @POST
    @Path("validate-pin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validatePin(Map<String, String> body) {
        String pin = body.get("pin");
        String email = body.get("email");

        logger.infof("Starting process to validate PIN for user", email);

        if (email == null || pin == null) {
            logger.info("Email and Pin fields are required");

            return this.customErrorResponse("fields_required");
        }

        UserModel user = session.users().getUserByEmail(session.getContext().getRealm(), email);
        if (user == null) {
            logger.info("User not found");

            return this.customErrorResponse("user_not_found");
        }

        logger.info("User exists, validating PIN");

        Map<String, Object> pinValidation = this.checkPin(user, pin);

        if (pinValidation.get("error") != null && (Boolean) pinValidation.get("error")) {
            String errorCode = (String) pinValidation.get("error_code");

            switch (errorCode) {
                case "pin_expired" -> {
                    return this.customErrorResponse("pin_expired");
                }

                case "pin_not_generated" -> {
                    return this.customErrorResponse("pin_not_generated");
                }

                case "pin_invalid" -> {
                    return this.customErrorResponse("pin_invalid");
                }
            }
        }

        logger.info("PIN is valid");

        user.setSingleAttribute("pin_validated", "true");

        Map<String, Object> result = new HashMap<>();
        result.put("success", "true");
        result.put("message", "pin_valid");

        return Response.ok(result).build();
    }

    @POST
    @Path("generate-pin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generatePin(Map<String, String> body) {
        String email = body.get("email");

        logger.infof("Starting process to generate PIN for user %s", email);

        UserModel user = session.users().getUserByEmail(session.getContext().getRealm(), email);
        if (user == null) {
            logger.info("User not found");

            return this.customErrorResponse("user_not_found");
        }

        logger.info("User exists, generating PIN");

        String pin = this.generatePin(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("pin", pin);

        logger.info("PIN has been generate successfully");

        return Response.ok(result).build();
    }

    @POST
    @Path("send-pin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendEmailPin(Map<String, String> body) {
        String email = body.get("email");
        var realm = session.getContext().getRealm();

        logger.infof("Starting process to generate PIN and send email for user %s", email);

        UserModel user = session.users().getUserByEmail(realm, email);
        if (user == null) {
            logger.info("User not found");

            return this.customErrorResponse("user_not_found");
        }

        logger.info("User exists, generating PIN");

        String pin = this.generatePin(user);

        try {
            EmailTemplateProvider emailProvider = session.getProvider(EmailTemplateProvider.class);
            emailProvider
                    .setRealm(realm)
                    .setUser(user);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("pin", pin);
            attributes.put("expiration_time", "10");

            logger.infof("Sending email to user %s", email);

            emailProvider.send("Reset Password", "send-pin.ftl", attributes);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);

            logger.info("PIN has been generate and email has been sent successfully");

            return Response.ok().entity(result).build();
        } catch (Exception e) {
            logger.error("Failed to send email: " + e.getMessage(), e);

            return this.customErrorResponse("email_error");
        }
    }

    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(Map<String, String> body) {
        String pin = body.get("pin");
        String email = body.get("email");
        String password = body.get("password");
        var realm = session.getContext().getRealm();

        logger.infof("Starting process to reset password for user %s", email);

        if (email == null || pin == null || password == null) {
            logger.info("Email/Pin/Password fields are required");

            return this.customErrorResponse("fields_required");
        }

        UserModel user = session.users().getUserByEmail(realm, email);
        if (user == null) {
            logger.info("User not found");

            return this.customErrorResponse("user_not_found");
        }

        logger.info("User exists, validating PIN");

        Map<String, Object> pinValidation = this.checkPin(user, pin);

        if (pinValidation.get("error") != null && (Boolean) pinValidation.get("error")) {
            String errorCode = (String) pinValidation.get("error_code");

            switch (errorCode) {
                case "pin_expired" -> {
                    return this.customErrorResponse("pin_expired");
                }

                case "pin_not_generated" -> {
                    return this.customErrorResponse("pin_not_generated");
                }

                case "pin_invalid" -> {
                    return this.customErrorResponse("pin_invalid");
                }
            }
        }

        logger.info("PIN is valid");

        String pinValidated = user.getFirstAttribute("pin_validated");
        if (pinValidated == null || !pinValidated.equals("true")) {
            logger.info("PIN is not validated previously");

            return this.customErrorResponse("pin_not_validated");
        }

        logger.info("PIN validated and resetting attributed from user");
        user.removeAttribute("pin_validated");
        user.removeAttribute("reset_pin");
        user.removeAttribute("pin_timestamp");

        logger.info("Removing stored credentials");
        user.credentialManager().removeStoredCredentialById(CredentialModel.PASSWORD);

        logger.info("Updating password");
        user.credentialManager().updateCredential(
                UserCredentialModel.password(password)
        );

        logger.info("Password updated successfully");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "success");

        return Response.ok(response).build();
    }

    private String generatePin(UserModel user) {
        logger.info("Starting process to generate PIN");

        String pin = String.format("%06d", new Random().nextInt(999999));

        //user [id, nome, email....., pin, pin_timestamp]
        user.setSingleAttribute("reset_pin", pin);
        user.setSingleAttribute("pin_timestamp", String.valueOf(System.currentTimeMillis()));

        return pin;
    }

    private Map<String, Object> checkPin(UserModel user, String pin) {
        logger.info("Starting process to validate PIN");

        int TEN_MINUTES_MILISECONDS = 600000;
        Map<String, Object> result = new HashMap<>();

        String storedPin = user.getFirstAttribute("reset_pin");
        String storedTimestamp = user.getFirstAttribute("pin_timestamp");

        if (storedTimestamp == null) {
            result.put("error", true);
            result.put("error_code", "pin_not_generated");

            logger.info("PIN has not been previously generated");

            return result;
        }

        if (storedPin == null || !storedPin.equals(pin)) {
            result.put("error", true);
            result.put("error_code", "pin_invalid");

            logger.info("PIN is null or invalid");

            return result;
        }

        Date pinTimestamp = new Date(Long.parseLong(storedTimestamp));

        if (new Date().getTime() - pinTimestamp.getTime() > TEN_MINUTES_MILISECONDS) {
            result.put("error", true);
            result.put("error_code", "pin_expired");

            logger.info("PIN has expired");

            return result;
        }

        logger.info("PIN check passed");

        result.put("error", false);
        return result;
    }

    private Response customErrorResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", true);
        result.put("error_code", message);

        return Response
                .status(Response.Status.BAD_REQUEST) //400
                .entity(result) //json ali em cima
                .build();
    }
}
