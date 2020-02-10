## The Gallery

This page holds an example Photoswipe gallery.

```pswp
{
  slides: [
    { src: 'content/uploads/g1.jpg', w: 2592, h:1944,
      title: 'Frost on a gate, Laurieston' },
    { src: 'content/uploads/g2.jpg', w: 2560, h:1920,
      title: 'Feathered crystals on snow surface, Taliesin' },
    { src: 'content/uploads/g3.jpg', w: 2560, h:1920,
      title: 'Feathered snow on log, Taliesin' },
    { src: 'content/uploads/g4.jpg', w: 2560, h:1920,
      title: 'Crystaline growth on seed head, Taliesin' }],
	options: {
      timeToIdle: 100
			},
	openImmediately: true
}

```

## How this works

The specification for this gallery is as follows:

```
{
  slides: [
    { src: 'content/uploads/g1.jpg', w: 2592, h:1944,
      title: 'Frost on a gate, Laurieston' },
    { src: 'content/uploads/g2.jpg', w: 2560, h:1920,
      title: 'Feathered crystals on snow surface, Taliesin' },
    { src: 'content/uploads/g3.jpg', w: 2560, h:1920,
      title: 'Feathered snow on log, Taliesin' },
    { src: 'content/uploads/g4.jpg', w: 2560, h:1920,
      title: 'Crystaline growth on seed head, Taliesin' }],
	options: {
      timeToIdle: 100
			},
	openImmediately: true
}

```

The format of the specification is [JSON](https://www.json.org/json-en.html); there are (at present) three keys, as follows

### slides

Most be present. The value of `slides` is a list delimited by square brackets of slide objects. For more information, see the [authoritative documentation](https://photoswipe.com/documentation/getting-started.html) under the sub heading **'Creating an Array of Slide Objects'**.

### options

Optional. The value of `options` is a JSON object [as documented here](https://photoswipe.com/documentation/options.html).

### openImmediately

Optional. If the value of `openImmediately` is `true`, the gallery will open immediately, covering the whole page. If false, only a button with the label 'Open the gallery' will be shown. Selecting this button will cause the gallery to open.
