package models;

import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;
import siena.Table;

@Table("otherid_string_models")
public class OtherIdStringModel extends EnhancedModel{
    @Id(Generator.NONE)
    public String myId;
    
    public String 	alpha;
    public short	beta;
    
    public OtherIdModel link;
    
    public String toString() {
    	return myId + " " + alpha + " " + beta + " " + link;
    }
}
