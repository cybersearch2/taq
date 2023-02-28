$ foreign-colors2.taq

$ java -jar taq.jar ^german.color_query name="Wasser"

```
Running query color_query in german scope 
Parameters [foreign-colors2, ^german.color_query, name=Wasser]
color_query(name=Wasser, red=0, green=255, blue=255)
```

# Description

foreign-colors2.taq demonstrates a resource with a provider system name which is enclosed 
in quotes to allow any naming convention to be used. A system name is used here becuse 
the provider is named "xstream", a reference to the underlying technology which serializes 
objects to XML and back again, and there are two resources, both using the same provider 
- "german_colors" and "french_colors". 

The "color_query" queries take a foreign language color name and returns it's red-green-blue 
color components. There are two resources "german_colors" and "french_colors" which each
declare in a data source role qualifier, that the provider is an axiom source. The data 
is generated by foreign-lexicon.taq.