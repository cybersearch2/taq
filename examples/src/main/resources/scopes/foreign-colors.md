# foreign-colors.taq

$ java -jar taq.jar foreign-colors ^german.color_query shade=Wasser

```
Running query color_query in german scope 
Parameters [foreign-colors, ^german.color_query, shade=Wasser]
color(shade=Wasser, red=0, green=255, blue=255)
```

### Description

foreign-colors.taq demonstrates using a context list to translate a selection value. 
The context list is named "colors"

> $ **list\<term\>** colors

There are 2 foreign scopes "german" and "french", the names reflecting the languages 
to be translated.  The "color_query" query of each scope takes a foreign language color 
name and returns the red-green-blue color components. Each scope has a "colors" axiom 
with foreign color values and the terms named in English.

```
axiom german.colors 
  ( aqua,     black,     blue,   white )
  {"Wasser", "schwarz", "blau", "weiß" }
```

The "color" select declaration maps the color by name to the color components. The 
first name in it's header is "colors", which being the name of a context list, will 
cause the select to perform translation according to the current scope.

>   **select** color\ 
> ( colors,  Red, Green, Blue)

Now to try the same query in French

$ java -jar taq.jar foreign-colors ^french.color_query shade="bleu vert"

```
running query color_query in french scope 
Parameters [foreign-colors, ^french.color_query, shade=bleu vert]
color(shade=bleu vert, red=0, green=255, blue=255)
```
