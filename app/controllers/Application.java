package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {
    public static void index(String name) {
	if (name==null) name = "unknown visitor";
        render(name);
    }
}
