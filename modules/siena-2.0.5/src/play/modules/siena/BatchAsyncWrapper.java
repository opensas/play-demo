package play.modules.siena;

import siena.core.async.SienaFuture;
import siena.core.batch.Batch;
import siena.core.batch.BatchAsync;

@SuppressWarnings("rawtypes")
public class BatchAsyncWrapper {	
	
	BatchAsync batchAsync;
	
	public <T> BatchAsyncWrapper(BatchAsync batchAsync){
		this.batchAsync = batchAsync;
	}
	
	@SuppressWarnings("unchecked")
	public <T> BatchAsync<T> getWrappedBatch(){
		return (BatchAsync<T>)batchAsync;
	}
	
	@SuppressWarnings("unchecked")
	public <T> SienaFuture<T> delete(T... arg0) {
		return batchAsync.delete(arg0);
	}

	@SuppressWarnings("unchecked")
	public <T> SienaFuture<T> delete(Iterable<T> arg0) {
		return batchAsync.delete(arg0);
	}

	@SuppressWarnings("unchecked")
	public <T> SienaFuture<T> deleteByKeys(T... arg0) {
		return batchAsync.deleteByKeys(arg0);
	}

	@SuppressWarnings("unchecked")
	public <T> SienaFuture<T> deleteByKeys(Iterable<T> arg0) {
		return batchAsync.deleteByKeys(arg0);
	}

	@SuppressWarnings("unchecked")
	public <T> SienaFuture<T> insert(T... arg0) {
		return batchAsync.insert(arg0);
	}

	@SuppressWarnings("unchecked")
	public <T> SienaFuture<T> insert(Iterable<T> arg0) {
		return batchAsync.insert(arg0);
	}

	public <T> BatchWrapper sync() {
		return new BatchWrapper(batchAsync.sync());
	}

	@SuppressWarnings("unchecked")
	public <T> SienaFuture<T> update(T... arg0) {
		return batchAsync.update(arg0);
	}

	@SuppressWarnings("unchecked")
	public <T> SienaFuture<T> update(Iterable<T> arg0) {
		return batchAsync.update(arg0);
	}

}
