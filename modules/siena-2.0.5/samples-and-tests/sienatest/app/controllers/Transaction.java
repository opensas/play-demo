package controllers;

import java.util.List;

import models.Employee;
import models.TransactionAccountFromModel;
import models.TransactionAccountToModel;
import play.cache.Cache;
import play.mvc.Controller;
import siena.Model;
import siena.SienaException;


public class Transaction extends Controller {
	public static void index() {
		TransactionAccountFromModel accFrom = new TransactionAccountFromModel(1000L);
		TransactionAccountToModel accTo = new TransactionAccountToModel(1000L);
		
		accFrom.insert();
		accTo.insert();
			
		Cache.set("accFrom", accFrom.id);
		Cache.set("accTo", accTo.id);
		renderTemplate("Transaction/index.html", accFrom, accTo);
	}

	public static void commit() {
		Object accFromId = (Long)Cache.get("accFrom");
		Object accToId = (Long)Cache.get("accTo");
		
		TransactionAccountFromModel accFrom = TransactionAccountFromModel.getByKey(TransactionAccountFromModel.class, accFromId);
		TransactionAccountToModel accTo = TransactionAccountToModel.getByKey(TransactionAccountToModel.class, accToId);

		try {
			accFrom.getPersistenceManager().beginTransaction();
			accFrom.amount-=100L;
			accFrom.save();
			accTo.amount+=100L;
			accTo.save();
			accFrom.getPersistenceManager().commitTransaction();
		}catch(SienaException e){
			accFrom.getPersistenceManager().rollbackTransaction();
		}finally{
			accFrom.getPersistenceManager().closeConnection();
		}

		// refetches it to be sure of sure
		accFrom = Model.getByKey(TransactionAccountFromModel.class, accFrom.id);
		accTo = Model.getByKey(TransactionAccountToModel.class, accTo.id);
		
		Cache.set("accFrom", accFrom.id);
		Cache.set("accTo", accTo.id);

		renderTemplate("Transaction/index.html", accFrom, accTo);
	}

	public static void failure() {
		Long accFromId = (Long)Cache.get("accFrom");
		Long accToId = (Long)Cache.get("accTo");
		
		TransactionAccountFromModel accFrom = Model.getByKey(TransactionAccountFromModel.class, accFromId);
		TransactionAccountToModel accTo = Model.getByKey(TransactionAccountToModel.class, accToId);

		try {
			accFrom.getPersistenceManager().beginTransaction();
			accFrom.amount-=100L;
			accFrom.save();
			accTo.amount+=100L;
			accTo.save();
			throw new SienaException("problem");
		}catch(SienaException e){
			accFrom.getPersistenceManager().rollbackTransaction();
		}finally{
			accFrom.getPersistenceManager().closeConnection();
		}
	
		// refetches it to be sure of sure
		accFrom = Model.getByKey(TransactionAccountFromModel.class, accFrom.id);
		accTo = Model.getByKey(TransactionAccountToModel.class, accTo.id);
		
		Cache.set("accFrom", accFrom.id);
		Cache.set("accTo", accTo.id);

		renderTemplate("Transaction/index.html", accFrom, accTo);
	}

}