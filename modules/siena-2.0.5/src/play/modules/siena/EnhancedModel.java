/**
 * 
 */
package play.modules.siena;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import play.data.validation.Validation;
import play.mvc.Scope.Params;
import siena.ClassInfo;
import siena.core.batch.Batch;

/**
 * Bridge between siena.Model and play.db.Model for people who don't want to
 * implement all/batch/... functions
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 * 
 */
public class EnhancedModel extends siena.Model implements Serializable,
		play.db.Model {
	private static final long serialVersionUID = -6970990524633907131L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see play.db.Model#_save()
	 */
	@Override
	public void _save() {
		this.save();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see play.db.Model#_delete()
	 */
	@Override
	public void _delete() {
		this.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see play.db.Model#_key()
	 */
	@Override
	public Object _key() {
		return siena.Util.readField(this, ClassInfo.getClassInfo(getClass())
				.getIdField());
	}

	// validates and inserts the entity
	public boolean validateAndSave() {
		if (Validation.current().valid(this).ok) {
			this.insert();
			return true;
		}
		return false;
	}

	// functions to enhance
	// we don't return a siena.Query because the strict generic typed
	// siena.Query gives compilation
	// errors when you create a class inheriting from EnhancedModel and calling
	// all().filter().fetch() for ex
	public static QueryWrapper all() {
		throw new UnsupportedOperationException(
			"Class not enhanced correctly.");
	}

	public static BatchWrapper batch() {
		throw new UnsupportedOperationException(
			"Class not enhanced correctly.");
	}

	public static <T extends EnhancedModel> T getByKey(Object key) {
		throw new UnsupportedOperationException(
			"Class not enhanced correctly.");
	}

	public static <T extends EnhancedModel> T create(String name, Params params) {
		throw new UnsupportedOperationException(
			"Class not enhanced correctly.");
	}

	public static long count() {
		throw new UnsupportedOperationException(
			"Class not enhanced correctly.");
	}

	public static <T extends EnhancedModel> List<T> findAll() {
		throw new UnsupportedOperationException(
			"Class not enhanced correctly.");
	}

	public static long deleteAll() {
		throw new UnsupportedOperationException(
			"Class not enhanced correctly.");
	}

	public static <T extends EnhancedModel> T findById(Object id) {
		throw new UnsupportedOperationException(
			"Class not enhanced correctly.");
	}

	public static <T extends EnhancedModel> T create(Class<T> type,
			String name, Map<String, String[]> params) {
		return (T) SienaModelUtils.create(type, name, params);
	}
}
