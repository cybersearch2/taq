# agriculture-report2.taq

$ java -jar taq.jar agriculture-report2 ^do_export

```
Running query do_export in initialize scope 
Parameters [agriculture-report2, ^do_export]
```

$ java -jar taq.jar agriculture-report2 ^more_agriculture

```
Running query more_agriculture in global scope 
Parameters [agriculture-report2, ^more_agriculture]
```

### Description

agriculture-report2.taq demonstrates a database resource provider which uses 
two entity classes to support a schema with two tables. The 'more_agriculture' 
query produces a list of countries which have increased the area under
agriculture by more than 1% over the twenty years between 1990 and 2010. 
The database information is revealed by a separate "show-agri20-year2" program 
using a custom SQL resource provider that merges the records from the two tables 
when displaying the results

$ java -jar taq.jar show-agri20-year2

```
Running query show_perfect_matches in global scope 

Country, area and percent area under agriculture data points:

1	Afghanistan,652230.0,58.3,58.3,58.3,57.9,58.1
2	Albania,28750.0,45.1,40.8,40.5,41.8,43.9
3	Algeria,2381740.0,18.6,18.4,16.3,16.7,17.4
...
207	Zambia,752610.0,26.6,26.6,28.1,30.1,31.5
208	Zimbabwe,390760.0,30.1,31.6,33.5,38.4,42.4

Country, area and increased surface area in square km:

1	Albania,986.1,
2	Algeria,25722.8,

...
64	Zambia,25212.4,
65	Zimbabwe,34777.6,
```

To see that there is actually two tables in the "agri-area-percent2" database requires using a suitable 
browsing tool or looking at the queries performed by the custom SQL resource provider. The point is that 
the entity class framework can be scaled up to support more than one database table.




