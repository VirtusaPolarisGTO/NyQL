# Script Mappers

By design, NyQL is expecting all your script (query) files are accessible as files. 
At runtime, it uses a mapper to fetch relevant script content  from the given script id. These mappers provide below functionality to the NyQL.
  * __Script referencing:__ for a given script id, it returns corresponding script source loading from anywhere you like.
  * __Caching:__ you may use your own caching mechanism to load scripts, or even watch script changes and act accordingly.


By default, NyQL provides three types of mapper implementations. 
Since v2, rather than specifying the full qualified classnames, it uses an unique id
to access a mapper implementation. Detailed descriptions are shown below.

| id | Name | Details | Compilable-At-Startup | Reloadable |
|---| ---|---| --- | --- |
|folder|QScriptsFolder |  Recursively load all scripts under a directory. | yes | yes
|folders|QScriptFolders |  Read several directories and load all scripts on those directories. | yes | yes
|resources|QResourceScripts |  Read scripts dynamically at runtime which are under a single resource path (inside a jar). | no | no
 

#### Loading Scripts from a Single Directory
 * Use _QScriptsFolder_ mapper implementation. 
 * Impl id: ___folder___
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
  "mapper": "folder",
  "mapperArgs": {
    "baseDir": "./examples",
    
    "inclusions": "<comma-separated-glob-patterns>...",
    "exclusions": "<comma-separated-glob-patterns>..."
  }
}
```

#### Loading Scripts from Multiple Directories
 * Use _QScriptFolders_ mapper.
 * Impl id: ___folders___
 * Impl class: _com.virtusa.gto.nyql.engine.repo.QScriptFolders_
 * Script Id = relative path from the correspondent base directory which script has loaded from.
 * Options:
    * _baseDirs_ : An array of directories which contains scripts. Each directory will be resolved as same as specified in _baseDir_ option in _QScriptsFolder_ impl.
 * __WARN__: If there are two scripts by the same id (relative paths are equal from correspondent base dirs), the loading will be failed.
 
```json
{
"mapper": "folders",
"mapperArgs": {
  "baseDirs": ["./scripts-dir-1", "./scripts-dir-2"]
}
```

Or, if you want to specify inclusions/exclusions for each folder, you may use the format as in 
loading from single directory with same properties. See below example.

```json
{
"mapper": "folders",
"mapperArgs": {
  "baseDirs": [ "./scripts-dir-1", 
               {
                "baseDir": "./scripts-dir-2",
                "exclusions": "<comma-separated-glob-patterns>..."
               }
              ]
}
```

As you can see you may use directory name, or, directory name + exclusions interchangeably within the same array.


#### Loading Scripts from Classpath
* Use _QResourceScripts_ mapper.
* Impl id: ___resources___
* Impl class: _com.virtusa.gto.nyql.engine.repo.QResourceScripts_
* Script Id = relative path from specified classpath base location
* Options:
  * _resourceRoot_ : Root classpath location to load scripts from.
* __WARN__: No initial compilation is provided by default, due to inability to enumerate all scripts in a classpath which is expensive and has to rely on a third-party implementation. 
Hence scripts will be compiled on demand whenever the script is being referenced for the very first time.

```json
{
"mapper": "resources",
"mapperArgs": {
  "resourceRoot": "/com/nyql/scripts"
}
```

### Defining a Custom Mapper
You can also write your own mapper class and load scripts from desired location(s).

You are feel free to contribute to NyQL by writing such useful mappers.

Say that you want to implement a mapper to load scripts from a network location at runtime. 
For that you need to implement a new mapper implementation and register it with NyQL through its configurations.

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
   
* Also you must create a factory class `QMapperFactory` to provide instances of your mapper class.
In the factory implementation, you should override two methods, namely, `supportedMappers` and `create`.
  * `supportedMappers` will return unique ids of supporting implementation of mappers from this factory.
  * `create` will have the specified mapper id (one of supported mapper ids), the argument map as specified
  in `mapperArgs` in _nyql.json_, and the configuration instance as inputs. This method should return a new instance
  of mapper implementation by using received parameters.
  
```java
public class NetworkScriptsMapperFactory implements QMapperFactory {
    // ...

    public String[] supportedMappers() {
        return new String[] { "network" };
    }

    public QScriptMapper create(String implName, Map args, Configurations configurations) throws NyException {
        // ... create instance of NetworkScripts here
    }
}
```

* Then create a service file inside `META-INF/services` directory having filename as the base service name, 
i.e. `com.virtusa.gto.nyql.model.QMapperFactory`. And inside the file place a line of fully qualified classname of 
the actual mapper factory implementation class.

_META-INF/services/com.virtusa.gto.nyql.model.QMapperFactory_
```text
com.xxx.xxx.xxx.NetworkScriptsMapperFactory
```

Replace _xxx_ s with your package name. Then your factory class will be automatically pickedup when it is in the classpath.

* Register the mapper factory along with the repository in `nyql.json` file.

```json
"repository": [
    {
        "mapper": "network",
        "mapperArgs": {
               ...
        }
    }
]
```

