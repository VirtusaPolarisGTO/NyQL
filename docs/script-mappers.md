# Script Mappers

By design, NyQL is expecting all your script (query) files are accessible as files. At runtime, it uses a mapper to fetch relevant script content  from the given script id. These mappers provide below functionality to the NyQL.
  * __Script referencing:__ for a given script id, it returns corresponding script source loading from anywhere you like.
  * __Caching:__ you may use your own caching mechanism to load scripts, or even watch script changes and act accordingly.


By default, NyQL provides three types of mapper implementations. Detailed descriptions are shown below.

| Name | Details | Compilable-At-Startup | Reloadable |
|---|---| --- | --- |
|QScriptsFolder |  Recursively load all scripts under a directory. | yes | yes
|QScriptFolders |  Read several directories and load all scripts on those directories. | yes | yes
|QResourceScripts |  Read scripts dynamically at runtime which are under a single resource path (inside a jar). | no | no
 

#### Loading Scripts from a Single Directory
 * Use _QScriptsFolder_ mapper implementation. 
 * Impl class: _com.virtusa.gto.nyql.engine.repo.QScriptsFolder_
 * Script Id = relative path from the base directory.
 * Options:
    * _baseDir_ : Root directory containing all scripts. If the given path is relative, the directory should be
    resolved respective to the current working directory, or if not, to the location where _nyql.json_ config file exist.
    * _inclusions_ (_Optional_): Specify comma separated glob patterns to include ONLY subset of directories under base.
    * _exclusions_ (_Optional_): Specify comma separated glob patterns to exclude some directories from base.
    * When have specified both _inclusions_ and _exclusions_, inclusions will take priority.
 
 ```json
 {
  "mapper": "com.virtusa.gto.nyql.engine.repo.QScriptsFolder",
  "mapperArgs": {
    "baseDir": "./examples",
    
    "inclusions": "<comma-separated-glob-patterns>...",
    "exclusions": "<comma-separated-glob-patterns>..."
  }
}
```

#### Loading Scripts from Multiple Directories
 * Use _QScriptFolders_ mapper.
 * Impl class: _com.virtusa.gto.nyql.engine.repo.QScriptFolders_
 * Script Id = relative path from the correspondent base directory which script has loaded from.
 * Options:
    * _baseDirs_ : An array of directories which contains scripts. Each directory will be resolved as same as specified in _baseDir_ option in _QScriptsFolder_ impl.
 * __WARN__: If there are two scripts by the same id (relative paths are equal from correspondent base dirs), the loading will be failed.
 
```json
{
"mapper": "com.virtusa.gto.nyql.engine.repo.QScriptFolders",
"mapperArgs": {
  "baseDirs": ["./scripts-dir-1", "./scripts-dir-2"]
}
```

#### Loading Scripts from Classpath (inside a jar)
* Use _QResourceScripts_ mapper.
* Impl class: _com.virtusa.gto.nyql.engine.repo.QResourceScripts_
* Script Id = relative path from specified classpath base location
* Options:
  * _resourceRoot_ : Root classpath location to load scripts from.
* __WARN__: No initial compilation is provided by default, due to inability to enumerate all scripts in a classpath which is expensive and has to rely on a third-party implementation. 
Hence scripts will be compiled on demand whenever the script is being referenced for the very first time.

```json
{
"mapper": "com.virtusa.gto.nyql.engine.repo.QResourceScripts",
"mapperArgs": {
  "resourceRoot": "/com/nyql/scripts"
}
```

### Defining a Custom Mapper
You can also write your own mapper class and load scripts from desired location(s).

You are feel free to contribute to NyQL by writing such useful mappers.

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
   * __reload(id)__ : Returns the most recent version of given script loading again. If the implementation cannot or does not allow to reload, return the same already loaded `QScript` instance.
   
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

