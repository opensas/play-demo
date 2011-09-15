package models;

import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;
import siena.Table;

@Table("uuid_models")
public class UUIDModel extends EnhancedModel{
    @Id(Generator.UUID)
    public String id;
    
    public String 	alpha;
    public short	beta;
    
    public String toString() {
    	return id + " " + alpha + " " + beta;
    }
}
