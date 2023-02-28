# high-cities-sorted3.taq

NOTE: This program references the "cities" database created by cities.taq.

$ java -jar taq.jar high-cities-sorted3

```
Running query sort_cities in global scope`
```

$ java -jar taq.jar show-high-cities

```
Exported cities:

1	denver,5280
2	flagstaff,6970
3	addis ababa,8000
4	leadville,10200
```

### Description

high-cities-sorted3.taq demonstrates two database resources sharing an entity 
class used to define the database records. This program depends on running cities.taq first.

The "sort_cities" query takes a list of cities read from one database and writes to another 
database the high cities in order of ascending altitude. The database records are revealed 
by a separate show-high-cities.taq program using a custom SQL resource provider. 
Each record id is shown to verify the sort took place.

Note that the data source role qualifier has the "City" entity class and `->` right 
arrow placed in front of it.

> "entities_axioms.City" -\> 
>
>> **axiom** city(altitude, name)
