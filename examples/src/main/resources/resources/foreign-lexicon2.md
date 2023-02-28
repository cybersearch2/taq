# foreign-lexicon2.taq

$ java -jar taq.jar foreign-lexicon2

*List contents of* workspace/xstream\
*On Linux*:

$ ls workspace/xstream

DE  DE.xml  FR  FR.xml

### Description

foreign-lexicon2.taq demonstrates a resource with a provider system name which is enclosed 
in quotes to allow any naming convention to be used. A system name is used here because 
the provider is named "xstream", a reference to the underlying technology which serializes 
objects to XML and back again, and there are two resources, both using the same provider 
- "german_colors" and "french_colors". 

The "color_query" queries do not write to the console and instead creates the 4 files 
as shown in the above directory list. These files are in turn the data source for 
foreign-colors2.taq. From scopes "french" and "german, there are 4 color names exported
in the language of the scope. These color names are imported into foreign-colors2.taq 
to be used for selection translation. For example a query for German "Wasser" is converted 
to one for "aqua".

Notice that both the resource data consumer declaration and the query statement both 
reference non-existent templates named "german.colors" and "french.colors". These 
templates are created automatically in this case, modelled on the paired axiom source 
in the query statement. The french.colors template archetype is copied from 

> **axiom** french.colors (aqua, black, blue, white)
