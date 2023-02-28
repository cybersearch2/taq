# dictionary.taq

$ java -jar taq.jar dictionary

```
Running query query_in_words in global scope 
in_words(inadequate,  "j. not sufficient to meet a need)
in_words(incentive,  "n. a positive motivational influence)
in_words(incidence,  "n. the relative frequency of occurrence of something)
in_words(incident,  "n. a public disturbance)
...
```
    
### Description

dictionary.taq demonstrates a bi-directional resource. The data source reads a file 
containing words starting with "i" and the data consumer sends the query result to 
the console. The "query_in_words" query uses a regular expression to select words 
starting with "in". 

The "lexicon" resource is declared at the top of dictionary.taq and this is a requirement 
as it is referenced everywhere only by this name. The declaration of this resource 
includes a property to specify the resource provider 

> **resource** lexicon(provider="lexicon.LexiconProvider")

The provider property is not oblgatory as the provider can also be specified by the application 
executing the query. The property is used here because the application inside taq.jar is generic. 
The order of the two role qualifiers in the bi-directional resource declaration is required 
to be data source followed by data consumer. Note there is no comma delimiter between 
the two.

Data source declared as an axiom source (resource provider itself)

> **axiom** dictionary(word, definition)

Data consumer declared as a template (the "in_words" template)

> **template** in_words