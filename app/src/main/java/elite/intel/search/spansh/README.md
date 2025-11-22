# Spansh integration.

#### NOTE: This package is undergoing heavy refactoring and active development.

----

Spansh client is located at

```java
   elite.intel.search.spansh.client.SpanshClient
```

Do not use this class directly. Instead extend it with your particular
client that will pass two urls to the super constructor.

example:

```java

public class FindFleetCarrierClient extends SpanshClient {

    private static FindFleetCarrierClient instance;

    private FindFleetCarrierClient() {
        super(
                "https://spansh.co.uk/api/stations/search/save", 
                "https://spansh.co.uk/api/stations/search/recall/"
        );
    }

    public static synchronized FindFleetCarrierClient getInstance() {
        if (instance == null) {
            instance = new FindFleetCarrierClient();
        }
        return instance;
    }
}

```

Create two data transfer objects. One for the request and one for the response.
see.

```java
package elite.intel.search.spansh.stellarobjects.*;
```
for inspiration.

To integrate with Spansh.
- Go to the website https://spansh.co.uk. 
- Select the search type at the top menu.
- Toggle the search options and enter your test search criteria.
- Open inspector and select the Network tab.
- Click on the search button.
- You will see the request and response in the network tab.
- Wrap your request and response DTOs to match the JSON structure.

For examples see:

```java 
elite.intel.search.spansh.stellarobjects.StellarObjectSearchRequestDto;
elite.intel.search.spansh.stellarobjects.StellarObjectSearchResponseDto;
```

Use `@SerializedName` annotation to match the JSON field names but provide camelCase 
names in your DTOs. Provide enums for the search fields where appropriate.

#### NOTE: Always use reference coordinates rather than star system name. Star system may not be known to Spansh.