# Include Feature
## Requirements
The user can include page title, abstract or the whole content in a given page. Headings and enumerations can be indented by a given include-level.

## Thoughts & Questions
* Which include syntax should be used?
  * page include can be definde alongsite of image includes - sth. like `#[indent-level](relative or absolute url)`
* Which kind of urls should we accept for page includes?
  * relative local urls (we will need some care to prohibit directory traversal ...)
  * absolute github / gitlab / gitblit urls without authentication.
* Which metadata can be used for title / abstract ?
  * MultiMarcdown-Metadata is supported by clj-markdown :-)
* How can we test page includes?
  * we will need a content resolver component for testing and at least one for production resolving.
