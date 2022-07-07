FROM gitpod/workspace-full:2022-05-08-14-31-53

RUN sudo install-packages ruby-full build-essential zlib1g-dev
RUN sudo gem install bundler webrick jekyll
