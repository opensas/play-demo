package models;

import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;

public class User extends EnhancedModel {
    
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
    public String name;
    public String avatarUrl;

    // Twitter
    public String token;
    public String secret;

    // Facebook
    public String accessToken;
    
    public static User findById(Long id) {
    	return all().filter("id", id).get();
    }
    
}
