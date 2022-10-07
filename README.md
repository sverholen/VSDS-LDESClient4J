# LDES Client SDK

This module contains the LDES client SDK that replicates and synchronises an LDES and keeps (non-persisted) state for that process.

Wrappers can call the SDK to do the actual work of scheduling fragment fetching and extracting members.

- [Service](#service)
- [ModelConverter](#modelconverter)
- [SDK configuration](#sdk-configuration)
    - [Configurable options](#configurable-options)
    - [Accepted RDF formats](#accepted-rdf-formats)

## Service

### Instantiating

Call the [LdesClientImplFactory](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientImplFactory.java) to get an instance of the [LdesService](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/services/LdesServiceImpl.java).

```java
LdesClientImplFactory.getService();
```

This call can be made without arguments, as above, in which case values will be taken from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java). Arguments are also accepted:

```java
LdesClientImplFactory.getService(Lang dataSourceFormat);
LdesClientImplFactory.getService(Lang dataSourceFormat, Long expirationInterval);
```


Missing or invalid values will be replaced by values from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java).

### Processing LDES

Once an instance of the [LdesService](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/services/LdesServiceImpl.java) is obtained, queue the initial fragment and begin processing.

```java
String initialFragmentId = "http://localhost:10101/ldes-test-fragment";
LdesService ldesService = LdesClientImplFactory.getService();

ldesService.queueFragment(initialFragmentId);

while (ldesService.hasFragmentsToProcess) {
	LdesFragment fragment = ldesService.processNextFragment();
	
	...
}
```

This will fetch the initial fragment (most likely after a redirect), process it and follow all relations. The resulting fragment can then be split into members that are ready for ingestion by an LDES server.


## ModelConverter

### Models to Strings

A [ModelConverter](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/converters/ModelConverter.java) is available to convert the Jena models to Strings.

```java
ModelConverter.convertModelToString(Model model);
ModelConverter.convertModelToString(Model model, Lang dataDestinationFormat);
```

When called without specifying the `dataDestinationFormat`, the value is taken from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java).

**Default value**: n-quads

### Strings to Models

Converting a String to a Model is also provided:

```java
ModelConverter.convertStringToModel(String input);
ModelConverter.convertStringToModel(String input, Lang dataSourceFormat);
```

When called without specifying the `dataSourceFormat`, the value is taken from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java).

### SDK configuration


#### Configurable options

- `dataSourceFormat`
  
  The expected RDF input format as recognised by [org.apache.jena.riot.Lang](https://javadoc.io/doc/org.apache.jena/jena-arq/4.6.1/org.apache.jena.arq/org/apache/jena/riot/Lang.html).

  **Default value**: JSONLD11

- `dataDestinationFormat`
  
  The desired RDF output format as recognised by [org.apache.jena.riot.Lang](https://javadoc.io/doc/org.apache.jena/jena-arq/4.6.1/org.apache.jena.arq/org/apache/jena/riot/Lang.html).

  **Default value**: n-quads

- `expirationInterval`

  A number (in seconds) used in setting an expiration date for fragments with an unknown refresh interval.

  **Default value**: 604800


When [LdesClientImplFactory](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientImplFactory.java) is called without these arguments, then default values from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java) will be used.

#### Accepted RDF formats

An indicative, possibly non-exhaustive list of RDF formats recognised by Jena is provided below.
Please refer to the [Jena documentation](https://javadoc.io/doc/org.apache.jena/jena-arq/4.6.1/org.apache.jena.arq/org/apache/jena/riot/Lang.html) of the implemented Jena version for an authoritative list.

Also take into consideration that Jena accepts variants of RDF format names in many cases. It is best practice to use the formal names.

- **CSV**
- **JSONLD**
- **N3** (treat as Turtle)
- **NQUADS** or **NQ**
- **NTRIPLES** or **NT**
- **RDFJSON**
- **RDFNULL**
- **RDFTHRIFT**
- **RDFXML**
- **TRIG**
- **TRIX**
- **TSV**
- **TURTLE** or **TTL**
