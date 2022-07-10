## Getting started

Add your content to the `book.html` file. 

Chapters are identified with a `<h1><a id="intro">Welcome</a></h1>` element, where the `id` attribute defines the chapter code.

Chapters then need to be added to the `toc.html` and `toc.ncx` files.

## Build process

There are two build scripts in the `scripts` directory: `buildk8s.sh` and `build-k8skdp.sh`. Each script has an associated Jekyll config file.

`build.sh` is used to build the epub distributed though GitHub and Kindle.

`build-kdp.sh` is used to build the epub that can be used as the basis for the print book. It resizes the images so they print correctly.

## KDP

For Octopus staff, open https://kdp.amazon.com/en_US/bookshelf and login with devops@octopus.com. Credentials are in the password manager.
