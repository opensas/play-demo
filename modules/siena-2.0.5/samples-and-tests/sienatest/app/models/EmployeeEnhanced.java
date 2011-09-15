package models;

import java.util.Date;
import java.util.List;
import java.util.Map;

import play.data.validation.Password;
import play.modules.siena.EnhancedModel;
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
import siena.embed.At;
import siena.embed.Embedded;
import siena.embed.EmbeddedList;
import siena.embed.EmbeddedMap;


@Table("employees_enhanced")
public class EmployeeEnhanced extends EnhancedModel {
        
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
        public EmployeeEnhanced boss;
        
        @Filter("boss")
        public siena.Query<EmployeeEnhanced> employees;
               
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

}