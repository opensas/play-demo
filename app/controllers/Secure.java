package controllers;

import models.User;
import play.Logger;
import play.Play;
import play.libs.OAuth;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Controller;

import com.google.gson.JsonObject;

public class Secure extends Controller {

	private final static String USER_COOKIE = "user";
	private final static OAuth.ServiceInfo TWITTER = new OAuth.ServiceInfo(
		Play.configuration.getProperty("twitter.requestTokenURL"),
		Play.configuration.getProperty("twitter.accessTokenURL"),
		Play.configuration.getProperty("twitter.authorizationURL"),
		Play.configuration.getProperty("twitter.consumerKey"),
		Play.configuration.getProperty("twitter.consumerSecret")
	);

	
	@Before(only={"Application.form", "Application.save", "Application.delete"})
	static void checkAccess() {
		final String userId = session.get(USER_COOKIE);
		
		if (userId==null) {
			login();
		} else {
			final User user = User.findById(Long.valueOf(userId));
			// user had the cookie but was deleted from db
			if (user==null) {
				session.clear();
				login();
			}
			//leave user for being used by the templates
			renderArgs.put("user", user);
		}
	}
	
	static void loadUser() {
		final String userId = session.get(USER_COOKIE);
		if (userId!=null) {
			final User user = User.findById(Long.valueOf(userId));
			// user had the cookie but was deleted from db
			if (user!=null) {
				//leave user for being used by the templates
				renderArgs.put("user", user);
			}
		}
	}
	
	public static void oauthTwitter() {

		// first time the request comes here
		// the user has just pushed the "sign in with twitter button"
		if (!OAuth.isVerifierResponse()) {
			final OAuth twitter = OAuth.service(TWITTER);
			final OAuth.Response response = twitter.retrieveRequestToken();
			if (response.error==null) {
				final User user = new User();
				user.token = response.token;
				user.secret = response.secret;
				user.save();
				session.put("userId", user.id);
				redirect(twitter.redirectUrl(response.token));
			} else {
	            Logger.error("Error contacting twitter: " + response.error);
	            login();
	        }
			
		// the user has been redirected by twitter
		// OAuth.isVerifierResponse() == true
		} else {		
			// user has not authorized access
			if (params.get("denied")!=null) login();

			// user authorized access
			final String userId = session.get("userId");
			if(userId==null) login();
			session.remove("userId");
			
			final User user = User.findById(Long.valueOf(userId));
			final OAuth.Response response = OAuth.service(TWITTER).retrieveAccessToken(user.token, user.secret);
			
			if (response.error==null) {
				// replace old token and secret with new ones
				user.token = response.token;
				user.secret = response.secret;
				
				// get user info
				JsonObject twitterUser = 
						WS.url("http://api.twitter.com/1/account/verify_credentials.json")
						.oauth(TWITTER, user.token, user.secret).get().getJson().getAsJsonObject();
				
				if (twitterUser.get("error") != null) {
                    // error fetching user info, probably the token has expired 
                    Logger.error("Twitter authentication error: %s", twitterUser.get("error"));
                    login();
				}
				user.name = twitterUser.get("name").getAsString();
				user.avatarUrl = twitterUser.get("profile_image_url").getAsString();
				user.save();
				session.put(USER_COOKIE, user.id);
			}
			Application.list();
		}
	}
	
	public static void login() {
		render();
	}
	
	public static void logout() {
		session.clear();
		Application.list();
	}
	
}
