package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void fourword(){
    	Fourword fw = new Fourword();
    	fw.test = "alpha";
    	// the save function is a new function which means "SAVE OR UPDATE"
    	fw.save();
    	Logger.debug("Fourword = %s ", fw);
    	FourwordUser user = new FourwordUser();
    	user.birthdate = new Date();
    	user.uid = "1234";
    	user.fourword = fw;
    	// the save function is a new function which means "SAVE OR UPDATE"
    	user.save();
    	
    	Fourword fwu = FourwordUser.getFourwords("1234");
    	renderText(fwu);
    }
    
    public static void fourword2(){
    	
    	FourwordUser user = new FourwordUser();
    	user.birthdate = new Date();
    	user.uid = "1234";
    	user.insert();
    	user.fourword = Fourword.all().filter("test", "alpha").get();
    	// the save function is a new function which means "SAVE OR UPDATE"
    	user.save();
    	
    	Fourword fwu = FourwordUser.getFourwords("1234");
    	renderText(fwu);
    }
}