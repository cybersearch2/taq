# cities.taq

$ java -jar taq.jar cities

```
Running query cities in global scope 
city(city=bilene, altitude=1718)
city(city=addis ababa, altitude=8000)
city(city=denver, altitude=5280)
city(city=flagstaff, altitude=6970)
city(city=jacksonville, altitude=8)
city(city=leadville, altitude=10200)
city(city=madrid, altitude=1305)
city(city=richmond, altitude=19)
city(city=spokane, altitude=1909)
city(city=wichita, altitude=1305)
```
$ java -jar taq.jar show-cities

```
Imported cities:

1 bilene,1718
2 addis ababa,8000
3 denver,5280
4 flagstaff,6970
5 jacksonville,8
6 leadville,10200
7 madrid,1305
8 richmond,19
9 spokane,1909
10  wichita,1305
````

### Description

cities.taq creates the "cities" database required to run high-cities-sorted3.taq.
The "cities" resource has a data-consumer role to write city records to the "cities"
database, using a "City" entity class as an intermediatry.

