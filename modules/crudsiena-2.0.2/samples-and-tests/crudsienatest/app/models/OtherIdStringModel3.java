package models;

import play.modules.siena.EnhancedModel;
import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Query;
import siena.Table;

@Table("otherid_string_models3")
public class OtherIdStringModel3 extends EnhancedModel{
    @Id(Generator.NONE)
    public String myId;
    
    public String 	alpha;
    public short	beta;
    
    @Filter("owner")
    public Query<OtherIdStringModel2> links;
    
    public String toString() {
    	return myId + " " + alpha + " " + beta;
    }
}
