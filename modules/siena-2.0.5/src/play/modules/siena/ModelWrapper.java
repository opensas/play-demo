/**
 * 
 */
package play.modules.siena;

import java.io.Serializable;

/**
 * Bridge between siena.Model and play.db.Model
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class ModelWrapper implements Serializable, play.db.Model {
	private static final long serialVersionUID = 949918995355310821L;

	private Object model;
	
	public ModelWrapper(Object model){
		this.model = model;
	}
	
	/* (non-Javadoc)
	 * @see play.db.Model#_save()
	 */
	@Override
	public void _save() {
		SienaPlugin.pm().save(model);
	}

	/* (non-Javadoc)
	 * @see play.db.Model#_delete()
	 */
	@Override
	public void _delete() {
		SienaPlugin.pm().delete(model);
	}

	/* (non-Javadoc)
	 * @see play.db.Model#_key()
	 */
	@Override
	public Object _key() {
		return SienaModelUtils.keyField(model.getClass());
	}


}
