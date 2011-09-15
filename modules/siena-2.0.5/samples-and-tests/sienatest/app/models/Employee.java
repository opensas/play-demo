package models;

import java.util.Date;
import java.util.List;
import java.util.Map;

import play.data.validation.Password;
import siena.Column;
import siena.DateTime;
import siena.Entity;
import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Index;
import siena.Json;
import siena.Max;
import siena.Model;
import siena.NotNull;
import siena.Query;
import siena.Table;
import siena.core.batch.Batch;
import siena.embed.At;
import siena.embed.Embedded;
import siena.embed.EmbeddedList;
import siena.embed.EmbeddedMap;


@Table("employees")
public class Employee extends Model {
        
        @Id(Generator.AUTO_INCREMENT)
        public Long id;
        
        @Column("first_name")
        @Max(10) @NotNull
        //@play.data.validation.MaxSize(10) @play.data.validation.Required
        public String firstName;
                
        @Column("last_name")
        @Max(200) @NotNull
        public String lastName;
        
        @Password
        @Column("pwd")
        public String pwd;
        
        @Column("contact_info")
        public Json contactInfo;       
        
        @Column("hire_date")
        public Date hireDate;
        
        @Column("fire_date")
        @DateTime
        public Date fireDate;
        
        @Column("boss") @Index("boss_index")
        public Employee boss;
        
        @Filter("boss")
        public siena.Query<Employee> employees;
               
        @Embedded
        public Image profileImage;
        
        @Embedded
        public List<Image> otherImages;

        @Embedded
        public Map<String, Image> stillImages;
        
        @EmbeddedMap
        public class Image {
                public String filename;
                public String title;
                public int views;
                public MyEnum itemEnum;
        }
              
        @Embedded
        public List<UserBlabla> items;
        
        @EmbeddedList
        public class UserBlabla {
          @At(0) public String item;
          @At(1) public String item2;
          @At(2) public MyEnum itemEnum;
        }
        
        public MyEnum enumField;
        
        public static enum MyEnum{
        	VAL1,
        	VAL2,
        	VAL3
        };
        
        public String toString() {
        	return firstName + " " + lastName;
        }

        public static Query<Employee> all() {
        	return Model.all(Employee.class);
        }
        
        public static Batch<Employee> batch() {
        	return Model.batch(Employee.class);
        }
        
        public Employee(Employee emp){
        	this.firstName = emp.firstName;
        	this.lastName = emp.lastName;
        	this.pwd = emp.pwd;
        	this.contactInfo = new Json(emp.contactInfo);
        	this.hireDate = emp.hireDate;
        	this.fireDate = emp.fireDate;
        	this.boss = emp.boss;
        	this.enumField = emp.enumField;
        	// ...
        }
}