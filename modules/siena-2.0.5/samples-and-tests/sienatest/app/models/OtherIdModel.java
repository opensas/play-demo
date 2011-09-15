package models;

import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;
import siena.Table;

@Table("otherid_models")
public class OtherIdModel extends EnhancedModel{
    @Id(Generator.AUTO_INCREMENT)
    public Long myId;
    
    public String 	alpha;
    public short	beta;    

    public String toString() {
    	return myId + " " + alpha + " " + beta;
    }
}
