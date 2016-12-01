# Script Mappers

By design, NyQL is expecting all your script (query) files are accessible as files. At runtime, NyQL use a mapper to fetch relevant script content  from the given script id. These mappers provide below functionality to the NyQL.
  * __Script referencing:__ for a given script id, it returns corresponding script source loading from anywhere you like.
  * __Caching:__ you may use your own caching mechanism to load scripts, or even watch script changes and act accordingly.


By default NyQL provides three types of mapper implementations.
  * __QScriptsFolder__ : Reads a single directory and recursively load all scripts under that directory and cache them. Script Id would be the relative path from the root directory.
  * __QScriptFolders__ : Read several directories and load all scripts on those directories. Caches them too. Script Id would be the relative path from the root directory. Here if there are two scripts by the same id (relative paths are equal from the root dir), the loading will be failed.
  * __QResourceScripts__ : Read scripts dynamically at runtime which are under a single resource path. No caching is provided, since there is no way to enumerate all resources, unlike files, without using a custom third party tools.


### Defining a Custom Mapper
Say that you want to implement a mapper to load scripts from a network location at runtime. For that you need to implement a new mapper implementation and register it with NyQL through its configurations.

* Create a class implementing interface `QScriptMapper`.

```java
public class NetworkScripts implements QScriptMapper {
    // ...
}
```

* You have to implement three methods from that interface.
   * __map(id)__ : Returns a `QSource` instance for the given script id. If no such mapping is found for the id, you may throw `NyScriptNotFoundException`.
   * __allSources()__ : Returns all source instances which is possibly can return from this mapper instance. If the `canCacheAtStartup()` is true, then you must return all scripts from here. Otherwise you may return null or empty collection.
   * __canCacheAtStartup()__ : Returns the status of ability to cache sources returned by this mapper interface.

* Additionally you must implement a static method accepting an input Map as a parameter in the class having name of `createNew`. Here this method will act as your factory method and should return the same instance as its implemented class. The input parameter map contains all the configuration keys specified in the nyql.json file under `mapperArgs`. See below example. 

```java
public class NetworkScripts implements QScriptMapper {
    // ...

    public static NetworkScripts createNew(Map inputConf) throws NyException {
        // ...
    }
}
```

* Register the mapper along with the repository in `nyql.json` file.

```json
"repositories": [
    {
        "name": "default",
        "repo": "com.virtusa.gto.nyql.engine.repo.QRepositoryImpl",
        "mapper": "<full-class-name-to-your-new-mapper>",

        "mapperArgs": {
               ...
        }
    }
]
```

