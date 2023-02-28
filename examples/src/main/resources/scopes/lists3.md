# liss3.taq

$ java -jar taq.jar lists3

```
london_dice(0=2, 1=5, 2=1, size=3)
london_dimensions(0=12.54, 1=6.98, 2=9.12, size=3)
london_flags(0=true, 1=false, size=2)
london_fruit(0=strawberry, 1=cherry, 2=peach, size=3)
london_movies(movie_1=greatest(The Godfather), 
              movie_2=greatest(The Shawshank Redemption), 
              movie_3=greatest(Schindler's List), size=3)
london_roaches(7,372,036,854,775,530, size=1)
london_stars(0=Sirius, 1=Canopus, 2=Rigil Kentaurus, size=3)

new_york_dice(0=6, 1=6, 2=6, size=3)
new_york_dimensions(0=16.84, 1=9.08, 2=11.77, size=3)
new_york_flags(0=false, 1=true, size=2)
new_york_fruit(0=apple, 1=pear, 2=orange, size=3)
new_york_movies(movie_1=greatest(Star Wars), 
                movie_2=greatest(Gone With The Wind), 
                movie_3=greatest(Spider Man), size=3)
new_york_roaches(35,223,372,036,854,775,691, size=1)
new_york_stars(0=Polarus, 1=Betelgeuse, 2=Vega, size=3)
```

### Description

liss3.taq displays the contents and size of 7 context lists implemented in 2 different 
scopes  named "london" and "new york". This differs from list2.taq only in that a cursor 
is used to access the each list. 

> **cursor** item(fruit)

Each list is navigated using `item++` which simpler than using an index to reference 
the list directly.

The console out shown above is aggregated from 
each of 14 queries to show the entire list collection.
