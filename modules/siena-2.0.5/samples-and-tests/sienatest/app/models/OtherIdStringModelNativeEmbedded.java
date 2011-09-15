package models;

import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.embed.Embedded;

@Table("otherid_string_models_embedded_native")
public class OtherIdStringModelNativeEmbedded extends EnhancedModel{
    @Id(Generator.NONE)
    public String myId;
    
    public String 	alpha;
    public short	beta;
    
    @Embedded(mode=Embedded.Mode.NATIVE)
    public OtherIdStringModel2 embed;
    
    public String toString() {
    	return myId + " alpha:" + alpha + " beta:" + beta + " embed:"+embed;
    }
}
