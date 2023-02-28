# dictionary2.taq

$ java -jar taq.jar dictionary2

Running query query_in_words in global scope 
    
```
Words starting with "in"
inadequate =   "j. not sufficient to meet a need"
incentive =   "n. a positive motivational influence"
incidence =   "n. the relative frequency of occurrence of something"
incident =   "n. a public disturbance"
...
Words starting with "im"
imaginary =   "j. not based on fact; unreal"
immense =   "j. unusually great in size or amount or degree or especially extent or scope"
immigrant =   "n. a person who comes to a country where they were not born in order to settle there"
imminent =   "j. close in time; about to occur"
```
    
### Description

dictionary2.taq demonstrates operations a resource can perform. It also shows that 
a cursor can be bound to a resource and keep in sync as the resource is opened and 
closed. The "lexicon" resource declares a data source which reads a file containing words 
starting with "i". The "query_in_words" query uses the resource to select words 
starting with "in" and then "im". 

The resource operations performed are

- lexicon.auto(false)
- lexicon.set(filter = search_for[i++])
- lexicon.open()\
- lexicon.close()

The auto operation inhibits the normal opening of the resource just before the query 
starts executing. The set operation adds/updates a resource property. In this case, 
it is setting the first characters of the i-words to match on. The open and close operations 
are self-explanatory. Controlling the resource opening and closing allows property 
updates to take effect. Note that a cursor bound to a resource is notified of open 
and close events so it can keep in sync with the state of the resource.