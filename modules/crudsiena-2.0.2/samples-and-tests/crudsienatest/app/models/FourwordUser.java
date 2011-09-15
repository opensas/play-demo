package models;

import java.util.Date;
import java.util.List;

import play.Logger;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Join;
import siena.Model;
import siena.Query;

public class FourwordUser extends Model {
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	public Date birthdate;

	// the new JOIN annotation will retrieve automatically the fourword for you
	// when you fetch the FourwordUser. This consumes one more request but this
	// is what you need certainly. It only works for this OnetoOne relation and
	// not yet for a OneToMany Query<Fourword> for ex
	@Join
	public Fourword fourword;
	
	public String uid;

	public static Fourword getFourwords(String uid) {
		if (uid == null)
			return null;

		// apparently you get only the first element 
		// even if there are many items with the same uid
		FourwordUser fu = all().filter("uid", uid).get();
		if(fu == null) return null;
		Logger.debug("from uid %s , found FourwordUser, fuid = %s ", uid, fu.id);
		Logger.debug("fu = %s , fu.fourword = %s , fu.birthday=%s , fu.uid = %s",
					fu, fu.fourword, fu.birthdate, fu.uid);
		if(fu.fourword != null){
			Logger.debug("fu.fourword.id = %s", fu.fourword.id);
		}
		
		return fu.fourword;
	}

	public static FourwordUser getFourwordUser(Long id) {
		return all().getByKey(id);
	}
	
	public static Query<FourwordUser> all() {
		return Model.all(FourwordUser.class);
	}
}
