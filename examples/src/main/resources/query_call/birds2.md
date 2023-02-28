# birds2.taq

$ java -jar taq.jar birds2

    Running query list_waterfowl in global scope 
    bird=whistling swan, family=swan, color=white, flight=ponderous, voice=muffled musical whistle
    bird=trumpeter swan, family=swan, color=white, flight=ponderous, voice=loud trumpeting
    bird=snow goose, family=goose, color=white, size=plump, flight=powerful, voice=honks
    bird=pintail, family=duck, flight=agile, voice=short whistle
    
    Running query waterfowl in global scope 
    waterfowl(bird=whistling swan, family=swan, color=white, flight=ponderous, voice=muffled musical whistle)
    waterfowl(bird=trumpeter swan, family=swan, color=white, flight=ponderous, voice=loud trumpeting)
    waterfowl(bird=snow goose, family=goose, color=white, size=plump, flight=powerful, voice=honks)
    waterfowl(bird=pintail, family=duck, flight=agile, voice=short whistle)

# Description

birds2.taq demonstrates a query made by a function call operation. 
The "waterfowl" query returns an axiom list containing distinguishing 
attributes of birds living in watery habitats. The "list_waterfowl" query calls the "waterfowl" query 
and converts each returned axiom into a list of strings skipping over blank terms representing attributes 
which are not relevant. The query function call has no parameters and the returned value is assigned directly to a cursor

> **cursor\<axiom\>** waterfowl_order = waterfowl()

Note the first query console result shown above is for comparison purposes 
only and is unrelated to the execution of the "waterfowl" query.