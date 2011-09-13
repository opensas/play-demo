package controllers;

import models.User;
import play.Logger;
import play.Play;
import play.libs.OAuth;
import play.libs.WS;
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

	public static void oauthTwitter() {
		
		if (params.get("denied")!=null) {
			// user has not authorized access
			login();
		}
	
		if (OAuth.isVerifierResponse()) {
			// user authorized access
			final String userId = session.get("userId");
			
			if(userId==null) login();
			
			session.remove("userId");
			
			final User user = User.findById(Long.valueOf(userId));
			final OAuth.Response response = OAuth.service(TWITTER).retrieveAccessToken(user.token, user.secret);
			
			if (response.error==null) {
				//replace old tokens and secret with new ones
				user.token = response.token;
				user.secret = response.secret;
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
		
		// first time the request comes here
		// the user has just pushed the "sign in with twitter button"
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
		
	}
	
	public static void login() {
		render();
	}
	
	public static void logout() {
		session.clear();
		login();
	}
	
}
