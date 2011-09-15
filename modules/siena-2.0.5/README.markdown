Siena support for Play! Framework v2.x
======================================

The siena module automatically enables [Siena](http://www.sienaproject.com) support for your application for GAE/MySQL/PostgreSQL/H2 databases.

Release notes
--------------------

* v2.x brings support for :
	+ GAE 
	+ MySQL
	+ Postgresql
	+ H2

* v2.x brings support for IDs of type Long (auto-generated or manual) and String (manual or auto-generated as UUID).
* v2.x brings a new class called __EnhancedModel__ which is a normal __siena.Model__ enhanced at runtime by Play to provide __all()__ function (and other functions also but it will be detailed later). Please see below for more details.
* v2.x  is a complete refactoring of siena module based on Siena v1.x. Yet, for those who used Siena before, it doesn't change anything as Siena v1.0.0 is 100% backward compatible (at least in theory).
* v2.x is compatible with Play version >1.2.1 and uses dependency management.

> **Note** v1.x only supported GAE but this is not the case anymore. Other NoSQL Databases will be added later.

> **Note** play-siena v2.0.4 embeds siena v1.0.0-b6: don't worry about the beta6, the code is quite stable! 
