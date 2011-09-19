package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity()
@Table(name="\"User\"")
//@Table(name="tableUser")
public class User extends Model {
    
    public String name;
    public String avatarUrl;

    // Twitter
    public String token;
    public String secret;

    // Facebook
    public String accessToken;
}
