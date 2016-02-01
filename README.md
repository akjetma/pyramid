# pyramid

## Usage

- You'll need leiningen installed. If you're on a mac with homebrew, `brew install leiningen`
- `lein run [PATH] [WIDTH]` from the root of this project will split the image at `PATH` into `WIDTH` px wide tiles and place them alongside the image at `PATH` into `tile/[zoom]/[row]/[col]`, then start a server and open your browser to the viewing client.
- You can also just split an image into tiles with `lein run -m pyramid.sh-tile [PATH] [WIDTH]`

