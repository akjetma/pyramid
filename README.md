# pyramid

## Usage

- You'll need leiningen installed. If you're on a mac with homebrew, `brew install leiningen`
- `lein run [PATH] [WIDTH]` from the root of this project will split the image at `PATH` into `WIDTH` px wide tiles and place them alongside the image at `PATH` into `tile/[zoom]/[row]/[col]`, then start a server and open your browser to the viewing client.
- You can also just split an image into tiles with `lein run -m pyramid.sh-tile [PATH] [WIDTH]`

## Random Notes

- The images comprise a quadtree reaching a depth where the combined tiles create an image of equal or greater magnification of the original image.
- The client is structured in a way so that the UI is derived from a single map describing the app state that looks like this:

```clojure
{:zoom 2 
 :viewport [1280 960]
 :coords [0 0]
 :levels {:0 {:rows 1 :cols 1}
          :1 {:rows 2 :cols 2}
          :2 {:rows 4 :cols 4}}
 :tiles [[2 1 1] [2 1 2] [2 2 1] [2 2 2]]
 :tile-size [1024 78]}
```

- `:zoom` - Current zoom level
- `:viewport` - Browser width/height
- `:coords` - Position of the upper left corner of the viewport with regard to the dimensions of the compiled image at the current zoom level
- `:levels` - Info from the server about the tiles at each zoom level
- `:tiles` - Currently visible tiles in the form of `[zoom row col]`
- `:tile-size` - The dimensions of a full-size tile
- `:anchor` - The `:coords` where the last mouseDown event happened

- I had to do a little fudgery to get the tile dimensions at the start of the client. I chose to do it this way rather than hardcode any values or assumptions about the tile sizes and aspect ratios
- The mouse and keyboard tracking system takes arbitrary callbacks to be performed when certain events happen. 
- The mouseMove callback is passed the difference in pixels from where the last mouseDown happened.
- The keyDown callback is passed the keyCode rather than the event. 

- The problem became a lot simpler when I realized that the meat of the problem involved the translation of points between three coordinate systems: the tile grid, the viewport with respect to the current zoom level, and the mouse position with respect to the viewport.
