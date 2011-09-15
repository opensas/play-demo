package models;

import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;
import siena.Table;

@Table("otherid_string_models2")
public class OtherIdStringModel2 extends EnhancedModel{
    @Id(Generator.NONE)
    public String myId;
    
    public String 	alpha;
    public short	beta;
    
    public OtherIdStringModel link;
    
    public OtherIdStringModel3	owner;

    public String toString() {
    	return myId + " " + alpha + " " + beta + " " + link;
    }
}
