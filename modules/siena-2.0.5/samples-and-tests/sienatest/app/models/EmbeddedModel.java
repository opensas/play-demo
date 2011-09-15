package models;

import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Table;
import siena.embed.EmbeddedMap;

@Table("embedded_models")
@EmbeddedMap
public class EmbeddedModel extends EnhancedModel{
    @Id(Generator.NONE)
    public String id;
    
    public String 	alpha;
    public short	beta;
    
    public String toString() {
    	return id + " " + alpha + " " + beta;
    }
}
