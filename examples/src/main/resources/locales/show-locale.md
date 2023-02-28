# show-locale.taq

java -jar taq.jar show-locale

```
Running query global_locale in global scope 
global_locale(name=global, language=en, region=AU, locale=en_AU)

Running query lux_locale in global scope 
lux_locale(name=luxenberg, language=fr, region=LU, locale=fr_LU)
```

### Description

show-locale.taq shows the name and locale attributes of 2 separate scopes. The global 
locale is the computer system default, so results will vary according to where in the 
world the query is run. The above "blobal_locale" result is for Australia.

Here is how the scope attributes are obtained


> name = scope.name, \
> language = scope.language, \
> region = scope.region, \
> locale = scope.locale
