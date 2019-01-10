- [] finish reading ApplicationContext
# ClassPathXmlApplicationContext
```java
public class App {
    public static void main(String[] args){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("basicConfig.xml");
        SimpleBean bean = context.getBean(SimpleBean.class);
        bean.doSomething();
        context.close();
    }
}
```
## Class Diagram
![Diagram](./diagrams/ClassPathXmlApplicationContext.svg)
### ResourceLoader
This is the strategy interface (see [strategy pattern](https://en.wikipedia.org/wiki/Strategy_pattern)) for loading resources (e.g. class path or file system resources).An `org.springframework.context.ApplicationContext` is required to provide this functionality, plus extended `org.springframework.core.io.support.ResourcePatternResolver` support.

`DefaultResourceLoader` is a standalone implementation that is
usable outside an ApplicationContext, also used by `ResourceEditor`.

Bean properties of type Resource and Resource array can be populated
from Strings when running in an ApplicationContext, using the particular
context's resource loading strategy.

getResource:
```java
Resource getResource(String location);
```
- Must support fully qualified URLs, e.g. "file:C:/test.dat".
- Must support classpath pseudo-URLs, e.g. "classpath:test.dat".
- Should support relative file paths, e.g. "WEB-INF/test.dat".
### ResourcePatternResolver

 * Strategy interface for resolving a location pattern (for example,
 * an Ant-style path pattern) into Resource objects.

## Constructor
```java
public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, @Nullable ApplicationContext parent) throws BeansException { 
    super(parent); // parent default to null
    setConfigLocations(configLocations);
    if (refresh) { // default to true
        refresh();
    }
}
```
### Superclass constructor
The call to `super(parent)` goes up the inheritance chain until `AbstractApplicationContext`:
```java
public AbstractApplicationContext(@Nullable ApplicationContext parent) {
    this();
    setParent(parent);
}

public AbstractApplicationContext() {
    this.resourcePatternResolver = getResourcePatternResolver();
}
```
    
getResourcePatternResolver:
```java
protected ResourcePatternResolvergetResourcePatternResolver() {
    return new PathMatchingResourcePatternResolver(this);
}
```
supporting Ant-style location patterns.
## setConfigLocations
This method is actually from `AbastractRefreshableConfigApplicationContext.setConfigLocations`:
```java
public void setConfigLocations(@Nullable String... locations) {
    if (locations != null) {
        Assert.noNullElements(locations, "Config locations must not be null");
        this.configLocations = new String[locations.length];
        for (int i = 0; i < locations.length; i++) {
            this.configLocations[i] = resolvePath(locations[i]).trim();
        }
    }
    else {
        this.configLocations = null;
    }
}
```
resolvePath:

calling `AbastractRefreshableConfigApplicationContext.resolvePath`:
```java
protected String resolvePath(String path) {
	return getEnvironment().resolveRequiredPlaceholders(path);
}    
```
The purpose of the `resolvePath` method is to replace the placeholder with environment properties. 

`getEnvironment` is from the `ConfigurableApplicationContext` interface, and implemented in `AbstractApplicationContext`.  
```java
public ConfigurableEnvironment getEnvironment() {
    if (this.environment == null) {
	    this.environment = createEnvironment();
    }
    return this.environment;
}
```
if environement is not already assigned, a `StandartEnvironment` is created and assigned to it.


### Environment
![Environment](./diagrams/StandardEnvironment.svg)
Interface representing the environment in which the current application is running. Models two key aspects of the application: **profiles** and **properties**. The method related to property access are exposed via the `PropertyResolver` superinterface.

#### Profile
Profile is introduced in Spring 3.1. A profile is a named logical group of bean definitions. These bean definitions are to be registered with the container, if the given profile is active. Beans can be assigned to a profile via XML or via `@Profile` annotation.

The role of the `Environment` w.r.t the profile is in determining which profiles are currently active, and which profiles should be active by default.

#### Property
properties can come from the different sources: properties files, JVM system properties, system environment variables, JNDI, servlet context parameters, ad-hoc Properties objects, Maps...

The role of the `Environment` w.r.t properties is to provider the user with a service inteface to:
- configure property sources, and
- resolve property form sources.

### StandartEnvironment constructor
uses the constructor in superclass `AbstractEnvironment`
```java
private final MutablePropertySources propertySources = new MutablePropertySources(this.logger);
public AbstractEnvironment() {
    customizePropertySources(this.propertySources);
}
```
#### PropertySources
![PropertySources](./diagrams/PropertySources.svg)
The interface is a holder for one or more `PropertySource` objects. `MutablePropertySources` is the default implementation.

It stores the objects internally using a `CopyOnWriteArrayList`

customizePropertySources from `AbastractEnvironment` has an empty implementation, the overriden behavior in StandardEnvironment is:
```java
/** System environment property source name: {@value}. */
public static final String SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";

/** JVM system properties property source name: {@value}. */
public static final String SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";

@Override
protected void customizePropertySources(MutablePropertySources propertySources) {
    propertySources.addLast(new MapPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, getSystemProperties()));
    propertySources.addLast(new SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, getSystemEnvironment()));
}
```
getSystemProperties and getSystemEnvironment are from the `ConfigurableEnvironment` interface, the implementations in `AbstractEnvironment` are used.

`getSystemProperties` delegates to `System.getProperties` to get all system properties at once. If the SecurityManager is preventing read/write access, it fall back to getting single read-only property individually with `System.getProperty(key)`.

The same for `getSystemEnvironment`, except it delegates to `System.getEnv`.

#### PropertySource
![PropertySource](./diagrams/PropertySource.svg)

`TODO: make diagram.`

### resolvePath
the resolvePath method in setConfigLocations delegates to `AbstractEnvironment.resolveRequiredPlaceholder(path)`

```java
private final ConfigurablePropertyResolver propertyResolver = 
        new PropertySourcesPropertyResolver(this.propertySources);

public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
	return this.propertyResolver.resolveRequiredPlaceholders(text);
}
```

#### PropertyResolver
![PropertySourcesPropertyResolver](./diagrams/PropertySourcesPropertyResolver.svg)

PropertyResolver is the interface for resolving PropertyResource. We are actually calling `AbastractPropertyResolver.resolveRequiredPlaceholders`:

```java
public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
    if (this.strictHelper == null) {
        this.strictHelper = createPlaceholderHelper(false);
    }
    return doResolvePlaceholders(text, this.strictHelper);
}

private PropertyPlaceholderHelper createPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {
    //first 3 parameters are "${", "}" and ":"
    return new PropertyPlaceholderHelper(this.placeholderPrefix, this.placeholderSuffix, this.valueSeparator, ignoreUnresolvablePlaceholders);
}
```
```java
private String doResolvePlaceholders(String text, PropertyPlaceholderHelper helper) {
	return helper.replacePlaceholders(text, this::getPropertyAsRawString);
}
```
getPropertyAsRawString is implemented in PropertySourcesPropertyResolver:
```java
@Override
@Nullable
protected String getPropertyAsRawString(String key) {
    return getProperty(key, String.class, false);
}
```

# Conclusion
what setConfigLocations does is taking the locations passed into the constructor and replace the placeholders with correct system property/environment. results is config paths with placeholders replaced.

# Refresh