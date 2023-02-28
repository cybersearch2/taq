# personalities2.taq

$ java -jar taq.jar personalities2 search_name=John

    Running query person_search in global scope 
    Parameters [personalities2, search_name=John]
    name_match(name=John, age=23, starsign=Gemini, personality=gentle, affectionate, curious)

### Description

personalities2.taq introduces **cursor** which steps through a list
going forward from the start or working backwards from the end. 
Here a cursor is bound to a list simply by declaring it with the same name as the list

> **cursor\<string\>** traits

Here the name "traits" coincides with that of the 5th column of the "person" axiom 
list. The cursor is used to format the "personality" term of the "name_match" template

> **string** personality = traits`++` `+` ", " `+` traits`++` `+` ", " `+` traits

