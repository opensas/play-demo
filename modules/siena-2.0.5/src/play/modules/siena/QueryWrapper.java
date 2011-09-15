package play.modules.siena;

import java.util.List;
import java.util.Map;

import siena.PersistenceManager;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryJoin;
import siena.QueryOrder;
import siena.core.options.QueryOption;

public class QueryWrapper{
	@SuppressWarnings("rawtypes")
	siena.Query query;
	
	@SuppressWarnings("unchecked")
	public <T> siena.Query<T> getWrappedQuery(){
		return (siena.Query<T>)query;
	}
	
	@SuppressWarnings("rawtypes")
	public <T> QueryWrapper(siena.Query query){
		this.query = query;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Class<T> getQueriedClass(){
		return (Class<T>)query.getQueriedClass();		
	}
	
	@SuppressWarnings("unchecked")
	public List<QueryFilter> getFilters() {
		return query.getFilters();
	}

	@SuppressWarnings("unchecked")
	public List<QueryOrder> getOrders() {
		return query.getOrders();
	}

	@SuppressWarnings("unchecked")
	public List<QueryFilterSearch> getSearches() {
		return query.getSearches();
	}

	@SuppressWarnings("unchecked")
	public List<QueryJoin> getJoins() {
		return query.getJoins();
	}
	
	public QueryOption option(int option) {
		return query.option(option);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, QueryOption> options() {
		return query.options();
	}

	public <T> QueryWrapper filter(String fieldName, Object value) {
		query.filter(fieldName, value);
		return this;
	}

	public <T> QueryWrapper order(String fieldName) {
		query.order(fieldName);
		return this;
	}

	public <T> QueryWrapper join(String field, String... sortFields) {
		query.join(field, sortFields);
		return this;
	}

	public <T> QueryWrapper search(String match, String... fields) {
		query.search(match, fields);
		return this;
	}

	public <T> QueryWrapper search(String match, QueryOption opt, String... fields) {
		query.search(match, opt, fields);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> T get() {
		return (T)query.get();
	}

	public <T> int delete() {
		return query.delete();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> int update(Map fieldValues) {
		return query.update(fieldValues);
	}

	public <T> int count() {
		return query.count();
	}

	@SuppressWarnings("unchecked")
	public <T> T getByKey(Object key) {
		return (T)query.getByKey(key);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> fetch() {
		return (List<T>)query.fetch();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> fetch(int limit) {
		return (List<T>)query.fetch(limit);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> fetch(int limit, Object offset) {
		return (List<T>)query.fetch(limit, offset);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> fetchKeys() {
		return (List<T>)query.fetchKeys();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> fetchKeys(int limit) {
		return (List<T>)query.fetchKeys(limit);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> fetchKeys(int limit, Object offset) {
		return (List<T>)query.fetchKeys(limit, offset);
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> iter() {
		return (Iterable<T>)query.iter();
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> iter(int limit) {
		return (Iterable<T>)query.iter(limit);
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> iter(int limit, Object offset) {
		return (Iterable<T>)query.iter(limit, offset);
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> iterPerPage(int limit) {
		return query.iterPerPage(limit);
	}

	public <T> QueryWrapper limit(int limit) {
		query.limit(limit);
		return this;
	}

	public <T> QueryWrapper offset(Object offset) {
		query.offset(offset);
		return this;
	}

	public <T> QueryWrapper paginate(int size) {
		query.paginate(size);
		return this;
	}

	public <T> QueryWrapper nextPage() {
		query.nextPage();
		return this;
	}

	public <T> QueryWrapper previousPage() {
		query.previousPage();
		return this;
	}

	public <T> QueryWrapper customize(QueryOption... options) {
		query.customize(options);
		return this;
	}

	public <T> QueryWrapper stateful() {
		query.stateful();
		return this;
	}

	public <T> QueryWrapper stateless() {
		query.stateless();
		return this;
	}

	public <T> QueryWrapper release() {
		query.release();
		return this;
	}

	public <T> QueryWrapper resetData() {
		query.resetData();
		return this;
	}

	public <T> QueryWrapper dump() {
		query.dump();
		return this;
	}

	public <T> QueryWrapper restore(String dump) {
		query.restore(dump);
		return this;
	}

	public <T> QueryAsyncWrapper async() {
		return new QueryAsyncWrapper(query.async());
	}

	public PersistenceManager getPersistenceManager() {
		return query.getPersistenceManager();
	}


}
